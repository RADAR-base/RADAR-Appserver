/*
 * Copyright 2025 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.appserver.microservices.protocol.service

import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.errors.IOException
import jakarta.inject.Inject
import jakarta.ws.rs.WebApplicationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.radarbase.appserver.microservices.contract.calls.GithubServiceContract
import org.radarbase.appserver.microservices.contract.calls.ProjectServiceContract
import org.radarbase.appserver.microservices.contract.calls.UserServiceContract
import org.radarbase.appserver.microservices.contract.response.ProxyResponse
import org.radarbase.appserver.microservices.contract.utils.Utils.deserializeDtoFromContract
import org.radarbase.appserver.microservices.core.dto.ProjectDto
import org.radarbase.appserver.microservices.core.dto.ProjectDtos
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUserDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUsers
import org.radarbase.appserver.microservices.core.dto.protocol.GithubContent
import org.radarbase.appserver.microservices.core.dto.protocol.Protocol
import org.radarbase.appserver.microservices.core.dto.protocol.ProtocolCacheEntry
import org.radarbase.appserver.microservices.core.entity.Project
import org.radarbase.appserver.microservices.core.mapper.Mapper
import org.radarbase.appserver.microservices.core.service.github.protocol.ProtocolFetcherStrategy
import org.radarbase.appserver.microservices.core.utils.cache.CachedMap
import org.radarbase.appserver.microservices.core.utils.mapParallel
import org.radarbase.appserver.microservices.core.utils.requireNotNullField
import org.radarbase.appserver.microservices.core.utils.withReentrantLock
import org.radarbase.appserver.microservices.protocol.config.ProtocolServiceConfig
import org.radarbase.jersey.exception.HttpNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URI
import java.time.Duration

/**
 * A strategy for fetching protocols stored in a GitHub repository. This implementation interacts
 * with a GitHub repository to retrieve protocol files for users and projects. It includes caching
 * and utilities for constructing file paths and parsing data from GitHub responses.
 *
 *
 * @property protocolRepo The configured GitHub repository path where protocols are stored.
 * @property protocolFileName The name of the protocol file used to identify relevant files in the repository.
 * @property protocolBranch The branch of the repository from which protocols should be retrieved.
 */
class GithubProtocolFetcherStrategy @Inject constructor(
    private val projectMapper: Mapper<ProjectDto, Project>,
    config: ProtocolServiceConfig,
) : ProtocolFetcherStrategy {

    private val protocolRepo: String
    private val protocolFileName: String
    private val protocolBranch: String

    private val projectServiceUrl = config.contract.project
    private val userServiceUrl = config.contract.user
    private val githubServiceUrl = config.contract.github

    init {
        config.protocol.also { questionnaireProtocolConfig ->
            protocolRepo = checkNotNull(questionnaireProtocolConfig.githubProtocolRepo) {
                "Github protocol repository is null in protocol fetcher"
            }
            protocolFileName = checkNotNull(questionnaireProtocolConfig.protocolFileName) {
                "Github protocol file name is null in protocol fetcher"
            }
            protocolBranch = checkNotNull(questionnaireProtocolConfig.githubBranch) {
                "Github repository branch is null in protocol fetcher"
            }
        }
    }

    private val fetchLock = Mutex()

    @OptIn(ExperimentalSerializationApi::class)
    private val localJson = Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        encodeDefaults = true
        prettyPrint = false
    }

    /**
     * A [CachedMap] that associates each project with its corresponding GitHub protocol URI.
     */
    @Transient
    private val projectProtocolUriMap: CachedMap<String, URI> = CachedMap(
        ::retrieveProtocolDirectories,
        Duration.ofHours(3),
        Duration.ofMinutes(4),
    )

    /**
     * Fetches protocol configurations for all users stored in the user repository and associates each
     * user with their respective protocol based on a set of predefined paths.
     *
     * @return A map where the keys are user IDs and the values are the corresponding protocol objects.
     */
    override suspend fun fetchProtocols(): Map<String, Protocol> = fetchLock.withReentrantLock {
        val users: List<FcmUserDto> = UserServiceContract.getAllUsers(userServiceUrl).let { pr ->
            deserializeDtoFromContract<FcmUsers>(
                pr,
            ) {
                "user_not_found ; No users found"
            }.users
        }

        val protocolPaths: Set<String> = getProtocolPaths()

        users.mapParallel(Dispatchers.Default) {
            val projectId = requireNotNullField(it.projectId, "User's project")
            fetchProtocolForSingleUser(it, requireNotNullField(projectId, "Project Id"), protocolPaths)
        }.filter { it.protocol != null }.associate { it.id to it.protocol!! }.also {
            logger.debug("Fetched Protocols from Github")
        }
    }

    /**
     * Fetches a protocol for a single user based on the user's attributes, a specific project ID,
     * and a set of protocol paths.
     *
     * @param user The user whose protocol is to be fetched. The user's attributes are used to find a matching protocol.
     * @param projectId The ID of the project associated with the requested protocol.
     * @param protocolPaths A set of potential protocol paths to be filtered and matched based on the user's attributes and project ID.
     * @return A [ProtocolCacheEntry] containing the user's ID and the fetched protocol, or null if no matching protocol is found.
     */
    private suspend fun fetchProtocolForSingleUser(
        user: FcmUserDto,
        projectId: String,
        protocolPaths: Set<String>,
    ): ProtocolCacheEntry {
        val attributes: Map<String?, String?> = user.attributes ?: emptyMap()
        val subjectId: String = requireNotNullField(user.subjectId, "User subject ID")

        val attributeMap: Map<String, String> = protocolPaths.filter {
            it.contains(projectId)
        }.map {
            convertPathToAttributeMap(it, projectId).filter { entry ->
                attributes[entry.key] == entry.value
            }
        }.maxByOrNull { it.size } ?: emptyMap()

        return try {
            val attributePath = convertAttributeMapToPath(attributeMap, projectId)
            projectProtocolUriMap.get()[attributePath]?.let {
                ProtocolCacheEntry(subjectId, getProtocolFromUrl(it))
            } ?: ProtocolCacheEntry(subjectId, null)
        } catch (_: Exception) {
            ProtocolCacheEntry(subjectId, null)
        }
    }

    /**
     * Fetches and maps protocols for each project based on their identifiers.
     *
     * @return a map where the key is the project ID (String) and the value is
     * the associated Protocol object. If a project does not have a corresponding
     * protocol, it will not be included in the map.
     */
    override suspend fun fetchProtocolsPerProject(): Map<String, Protocol> {
        val protocolPaths = getProtocolPaths()
        if (protocolPaths.isEmpty()) return emptyMap()
        val projects: List<Project> = ProjectServiceContract.getAllProjects(projectServiceUrl).let { pr ->
            deserializeDtoFromContract<ProjectDtos>(
                pr,
            ) {
                "projects_not_found ; No projects found"
            }.let {
                projectMapper.dtosToEntities(it.projects)
            }
        }

        return projects.mapParallel(Dispatchers.Default) { project ->
            val projectId = requireNotNullField(project.projectId, "Project Id")
            val protocol = protocolPaths.lastOrNull { it.contains(projectId) }?.let { path ->
                try {
                    val uri = projectProtocolUriMap.get()[path] ?: return@let null
                    getProtocolFromUrl(uri)
                } catch (e: Exception) {
//                    null
                    throw e
                }
            }
            ProtocolCacheEntry(projectId, protocol)
        }.filter { it.protocol != null }.associate { it.id to it.protocol!! }
            .also { logger.debug("Refreshed Protocols from Github") }
    }

    /**
     * Retrieves a set of protocol paths available from the underlying project protocol URI map.
     * If an error occurs while fetching the map, a cached version is used instead.
     *
     * @return a set of strings representing protocol paths
     */
    private suspend fun getProtocolPaths(): Set<String> {
        val uriMap: Map<String, URI> = try {
            projectProtocolUriMap.get()
        } catch (_: IOException) {
            // Failed to get the Uri Map. Using the cached values
            projectProtocolUriMap.getCachedMap()
        }
        return uriMap.keys
    }

    /**
     * Converts a path string into a map of attributes based on key-value pairs extracted from the path.
     * Keys and values are determined by splitting the path and pairing the elements.
     * The `projectId` and `protocolFileName` are excluded from the results.
     *
     * @param path The input path strings to be converted into a map of attributes.
     * @param projectId The project identifier used to remove matching segments from the path.
     * @return A map containing key-value pairs derived from the path.
     */
    fun convertPathToAttributeMap(path: String, projectId: String): Map<String, String> {
        val pathMap = mutableMapOf<String, String>()
        val pathParts: List<String> = path.split("/")

        return pathMap.apply {
            pathParts.filter { it != projectId && it != protocolFileName }.chunked(2) { attr ->
                if (attr.size == 2) this[attr[0]] = attr[1]
            }
        }
    }

    /**
     * Converts a map of attributes into a path string by appending each key-value pair, prefixed
     * by the given project ID and ending with a specified protocol file name.
     *
     * @param attributeMap A map containing attribute keys and their corresponding values to be converted into a path.
     * @param projectId The ID of the project with which the generated path is associated.
     * @return A string representing the constructed path using the given map and project ID.
     */
    fun convertAttributeMapToPath(attributeMap: Map<String, String>, projectId: String): String = buildString {
        append(projectId).append("/")
        attributeMap.forEach { (k, v) ->
            append(k).append("/").append(v).append("/")
        }
        append(protocolFileName)
    }

    /**
     * Retrieves a map containing protocol file paths as keys and their corresponding URLs as values
     * from a specified GitHub repository.
     *
     * @throws IOException if there is an issue in fetching or processing the content from GitHub.
     * @return a map where keys are the paths of protocol files and values are their respective URIs.
     */
    @Throws(IOException::class)
    private suspend fun retrieveProtocolDirectories(): Map<String, URI> = fetchLock.withReentrantLock {
        val protocolUriMap = mutableMapOf<String, URI>()

        try {
            val branchJson = GithubServiceContract.getGithubContent(
                githubServiceUrl,
                "$GITHUB_API_URI$protocolRepo/branches/$protocolBranch",
            ).also {
                checkInvalidStatus(it)
            }.body?.run {
                decodeToString()
            } ?: run {
                throw IOException(
                    "Could not decode github response from $protocolRepo/branches/$protocolBranch: received null response",
                )
            }

            val branchElement = Json.parseToJsonElement(branchJson).jsonObject
            val treeSha = branchElement["commit"]
                ?.jsonObject?.get("commit")
                ?.jsonObject?.get("tree")
                ?.jsonObject?.get("sha")
                ?.jsonPrimitive?.content
                ?: throw IOException("Missing tree sha in branch JSON")

            val treeJson = GithubServiceContract.getGithubContent(
                githubServiceUrl,
                "$GITHUB_API_URI$protocolRepo/git/trees/$treeSha?recursive=true",
            ).also {
                checkInvalidStatus(it)
            }.body?.run {
                decodeToString()
            } ?: run {
                throw IOException("Could not decode github response: received null response")
            }
            val treeElement = Json.parseToJsonElement(treeJson).jsonObject

            val treeArray = treeElement["tree"]?.jsonArray
                ?: throw IOException("Missing tree array in tree JSON")

            for (node in treeArray) {
                val obj = node.jsonObject
                val path = obj["path"]?.jsonPrimitive?.content ?: continue
                if (path.contains(this.protocolFileName)) {
                    val url = obj["url"]?.jsonPrimitive?.content ?: continue
                    protocolUriMap[path] = URI.create(url)
                }
            }
        } catch (e: WebApplicationException) {
            throw IOException("Failed to retrieve protocols URIs from github", e)
        } catch (ex: HttpNotFoundException) {
            throw ex
        } catch (e: Exception) {
            throw IOException("Exception when retrieving protocol uri map info", e)
        }
        protocolUriMap
    }

    /**
     * Fetches a `Protocol` object from a specified URI. The method retrieves the content from the given URI,
     * parses it into a `GithubContent` object, and extracts the `Protocol` data from it.
     *
     * @param uri The URI from which the protocol data will be fetched.
     * @return The [Protocol] object retrieved and deserialized from the URI content.
     * @throws IOException If an error occurs while fetching or parsing the content from the URI.
     */
    @Throws(IOException::class)
    private suspend fun getProtocolFromUrl(uri: URI): Protocol {
        val contentString = GithubServiceContract.getGithubContent(
            githubServiceUrl,
            uri.toString(),
        ).also {
            checkInvalidStatus(it)
        }.body?.run {
            decodeToString()
        } ?: run {
            throw IOException("Could not process protocol files from github response: received null response")
        }
        val protocol = localJson.decodeFromString<GithubContent>(contentString).content
            ?: throw IOException("Protocol content is null")
        return localJson.decodeFromString<Protocol>(protocol)
    }

    fun checkInvalidStatus(res: ProxyResponse) {
        if (res.status == HttpStatusCode.NotFound.value) {
            throw HttpNotFoundException("github_endpoint_not_found", res.body?.decodeToString() ?: "")
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(GithubProtocolFetcherStrategy::class.java)

        private const val GITHUB_API_URI = "https://api.github.com/repos/"
    }
}
