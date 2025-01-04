/*
 *
 *  *
 *  *  * Copyright 2018 King's College London
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *  *
 *  *
 *
 */
package org.radarbase.appserver.service.questionnaire.protocol

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.radarbase.appserver.dto.protocol.GithubContent
import org.radarbase.appserver.dto.protocol.Protocol
import org.radarbase.appserver.dto.protocol.ProtocolCacheEntry
import org.radarbase.appserver.entity.User
import org.radarbase.appserver.repository.ProjectRepository
import org.radarbase.appserver.repository.UserRepository
import org.radarbase.appserver.service.GithubService
import org.radarbase.appserver.util.CachedMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.io.IOException
import java.net.URI
import java.time.Duration
import java.util.stream.Collectors

/**
 * A strategy for fetching protocols stored in a GitHub repository. This implementation interacts
 * with a GitHub repository to retrieve protocol files for users and projects. It includes caching
 * and utilities for constructing file paths and parsing data from GitHub responses.
 *
 *
 * @property protocolRepo The configured GitHub repository path where protocols are stored.
 * @property protocolFileName The name of the protocol file used to identify relevant files in the repository.
 * @property protocolBranch The branch of the repository from which protocols should be retrieved.
 * @property objectMapper A JSON utility for serialization and deserialization of data.
 * @property userRepository Repository for accessing User data from the database.
 * @property projectRepository Repository for accessing Project data from the database.
 * @property githubService A service for interacting with the GitHub API.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class GithubProtocolFetcherStrategy(
    @Value("\${radar.questionnaire.protocol.github.repo.path}")
    @field:Transient private val protocolRepo: String?,

    @Value("\${radar.questionnaire.protocol.github.file.name}")
    @field:Transient
    private val protocolFileName: String?,

    @Value("\${radar.questionnaire.protocol.github.branch}")
    @field:Transient
    private val protocolBranch: String?,

    @field:Transient
    private val objectMapper: ObjectMapper,
    @field:Transient
    private val userRepository: UserRepository,
    @field:Transient
    private val projectRepository: ProjectRepository,
    @field:Transient
    private val githubService: GithubService
) : ProtocolFetcherStrategy {

    @Transient
    private val localMapper: ObjectMapper = objectMapper.copy().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }

    /**
     * [CachedMap] that associates each project with its corresponding GitHub protocol URI.
     */
// Keeps a cache of gitHub URI's associated with protocol for each project
    @Transient
    private val projectProtocolUriMap =
        CachedMap(::retrieveProtocolDirectories, Duration.ofHours(3), Duration.ofMinutes(4))


    init {
        require(protocolRepo.isNullOrBlank() && protocolFileName.isNullOrBlank() && protocolBranch.isNullOrBlank()) {
            "Protocol Repo and File name must be configured."
        }
    }

    /**
     * Fetches protocol configurations for all users stored in the user repository and associates each
     * user with their respective protocol based on a set of predefined paths.
     *
     * @return A map where the keys are user IDs and the values are the corresponding protocol objects.
     */
    @Synchronized
    override fun fetchProtocols(): Map<String, Protocol> {
        val users = userRepository.findAll()
        val protocolPaths = getProtocolPaths()

        return users.parallelStream()
            .map { fetchProtocolForSingleUser(it, it.project!!.projectId!!, protocolPaths) }
            .filter { it.protocol != null }
            .distinct()
            .collect(Collectors.toMap({ it.id }, { it.protocol!! }))
            .also { logger.debug("Fetched Protocols from Github") }
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
    private fun fetchProtocolForSingleUser(
        user: User,
        projectId: String,
        protocolPaths: Set<String>
    ): ProtocolCacheEntry {
        val attributes: Map<String?, String?>? = user.attributes ?: emptyMap()

        val pathMap: Map<String, String> = protocolPaths.filter {
            it.contains(projectId)
        }.map {
            convertPathToAttributeMap(it, projectId).filter { entry ->
                attributes?.get(entry.key) == entry.value
            }
        }.maxByOrNull { it.size } ?: emptyMap()

        return try {
            val attributePath = convertAttributeMapToPath(pathMap, projectId)
            projectProtocolUriMap.get()[attributePath]?.let {
                ProtocolCacheEntry(user.subjectId!!, getProtocolFromUrl(it))
            } ?: ProtocolCacheEntry(user.subjectId!!, null)
        } catch (_: Exception) {
            ProtocolCacheEntry(user.subjectId!!, null)
        }
    }

    /**
     * Fetches and maps protocols for each project based on their identifiers.
     *
     * @return a map where the key is the project ID (String) and the value is
     * the associated Protocol object. If a project does not have a corresponding
     * protocol, it will not be included in the map.
     */
    override fun fetchProtocolsPerProject(): Map<String, Protocol> {
        val protocolPaths = getProtocolPaths()

        return projectRepository.findAll().parallelStream()
            .map { project ->
                val projectId = project.projectId!!
                val protocol = protocolPaths.firstOrNull { it.contains(projectId) }?.let { path ->
                    try {
                        getProtocolFromUrl(projectProtocolUriMap.get()[path] ?: return@let null)
                    } catch (_: Exception) {
                        null
                    }
                }
                ProtocolCacheEntry(projectId, protocol)
            }
            .collect(Collectors.toMap({ it.id }, { it.protocol!! }))
            .also { logger.debug("Refreshed Protocols from Github") }

    }

    /**
     * Retrieves a set of protocol paths available from the underlying project protocol URI map.
     * If an error occurs while fetching the map, a cached version is used instead.
     *
     * @return a set of strings representing protocol paths
     */
    private fun getProtocolPaths(): Set<String> {
        var uriMap: Map<String, URI> = try {
            projectProtocolUriMap.get()
        } catch (_: IOException) {
            // Failed to get the Uri Map. try using the cached value
            projectProtocolUriMap.getCachedMap()
        }
        return uriMap.keys
    }

    /**
     * Converts a path string into a map of attributes based on key-value pairs extracted from the path.
     * Keys and values are determined by splitting the path and pairing the elements.
     * The `projectId` and `protocolFileName` are excluded from the results.
     *
     * @param path The input path string to be converted into a map of attributes.
     * @param projectId The project identifier used to remove matching segments from the path.
     * @return A map containing key-value pairs derived from the path.
     */
    fun convertPathToAttributeMap(path: String, projectId: String): Map<String, String> {
        val pathMap = mutableMapOf<String, String>()
        val pathParts: List<String> = path.split("/")

        return pathMap.apply {
            pathParts.filter { it != projectId && it != protocolFileName }
                .chunked(2) { attr ->
                    if (attr.size == 2) this[attr[0]] = attr[1]
                }
        }
    }

    /**
     * Converts a map of attributes into a path string by appending each key-value pair, prefixed
     * by the given project ID and ending with a specified protocol file name.
     *
     * @param pathMap A map containing attribute keys and their corresponding values to be converted into a path.
     * @param projectId The ID of the project to which the generated path is associated.
     * @return A string representing the constructed path using the given map and project ID.
     */
    fun convertAttributeMapToPath(pathMap: Map<String, String>, projectId: String): String = buildString {
        append(projectId).append("/")
        pathMap.forEach { (k, v) ->
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
    @Synchronized
    private fun retrieveProtocolDirectories(): Map<String, URI> {
        val protocolUriMap = mutableMapOf<String, URI>()

        try {
            val treeContent = githubService.getGithubContentWithoutCache(
                "$GITHUB_API_URI$protocolRepo/branches/$protocolBranch"
            ).run {
                this@GithubProtocolFetcherStrategy.getArrayNode(this)
            }.run {
                this.findValue("tree").findValue("sha").asText()
            }.let { treeSha ->
                githubService.getGithubContent("$GITHUB_API_URI$protocolRepo/git/trees/$treeSha?recursive=true")
            }

            val tree = getArrayNode(treeContent).get("tree")
            for (jsonNode in tree) {
                val path = jsonNode.get("path").asText()
                if (path.contains(this.protocolFileName!!)) {
                    protocolUriMap[path] = URI.create(jsonNode.get("url").asText())
                }
            }
        } catch (e: ResponseStatusException) {
            throw IOException("Failed to retrieve protocols URIs from github", e)
        } catch (e: Exception) {
            throw IOException("Exception when retrieving protocol uri map info", e)
        }
        return protocolUriMap
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
    private fun getProtocolFromUrl(uri: URI): Protocol {
        val contentString = githubService.getGithubContent(uri.toString())
        val content = localMapper.readValue(contentString, GithubContent::class.java)
        return localMapper.readValue(content.content, Protocol::class.java)
    }

    /**
     * Parses the given JSON string into an ObjectNode.
     *
     * @param json The JSON string to be parsed.
     * @return The parsed ObjectNode representation of the JSON string.
     */
    private fun getArrayNode(json: String): ObjectNode {
        objectMapper.factory.createParser(json).use { parserProtocol ->
            return objectMapper.readTree<ObjectNode>(parserProtocol)
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(GithubProtocolFetcherStrategy::class.java)

        private const val GITHUB_API_URI = "https://api.github.com/repos/"
    }
}
