package org.radarbase.appserver.jersey.service.github.protocol.impl

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.ktor.utils.io.errors.IOException
import jakarta.inject.Inject
import jakarta.ws.rs.WebApplicationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import org.radarbase.appserver.jersey.config.AppserverConfig
import org.radarbase.appserver.jersey.dto.protocol.GithubContent
import org.radarbase.appserver.jersey.dto.protocol.Protocol
import org.radarbase.appserver.jersey.dto.protocol.ProtocolCacheEntry
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.repository.ProjectRepository
import org.radarbase.appserver.jersey.repository.UserRepository
import org.radarbase.appserver.jersey.service.github.GithubService
import org.radarbase.appserver.jersey.service.github.protocol.ProtocolFetcherStrategy
import org.radarbase.appserver.jersey.utils.cache.CachedMap
import org.radarbase.appserver.jersey.utils.mapParallel
import org.radarbase.appserver.jersey.utils.withReentrantLock
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
 * @property objectMapper A JSON utility for serialization and deserialization of data.
 * @property userRepository Repository for accessing User data from the database.
 * @property projectRepository Repository for accessing Project data from the database.
 * @property githubService A service for interacting with the GitHub API.
 */
class GithubProtocolFetcherStrategy @Inject constructor(
    config: AppserverConfig,
    private val objectMapper: ObjectMapper,
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val githubService: GithubService,
) : ProtocolFetcherStrategy {

    private val protocolRepo: String
    private val protocolFileName: String
    private val protocolBranch: String

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
    private val localMapper: ObjectMapper = objectMapper.copy().apply {
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
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
        val users: List<User> = userRepository.findAll()
        val protocolPaths: Set<String> = getProtocolPaths()

        users.mapParallel(Dispatchers.Default) {
            fetchProtocolForSingleUser(it, it.project!!.projectId!!, protocolPaths)
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
        user: User,
        projectId: String,
        protocolPaths: Set<String>,
    ): ProtocolCacheEntry {
        val attributes: Map<String?, String?>? = user.attributes ?: emptyMap()

        val attributeMap: Map<String, String> = protocolPaths.filter {
            it.contains(projectId)
        }.map {
            convertPathToAttributeMap(it, projectId).filter { entry ->
                attributes?.get(entry.key) == entry.value
            }
        }.maxByOrNull { it.size } ?: emptyMap()

        return try {
            val attributePath = convertAttributeMapToPath(attributeMap, projectId)
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
    override suspend fun fetchProtocolsPerProject(): Map<String, Protocol> {
        val protocolPaths = getProtocolPaths()

        return projectRepository.findAll().mapParallel(Dispatchers.Default) { project ->
            val projectId = project.projectId!!
            val protocol = protocolPaths.lastOrNull { it.contains(projectId) }?.let { path ->
                try {
                    val uri = projectProtocolUriMap.get()[path] ?: return@let null
                    getProtocolFromUrl(uri)
                } catch (_: Exception) {
                    null
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
            val treeContent = githubService.getGithubContentWithoutCache(
                "$GITHUB_API_URI$protocolRepo/branches/$protocolBranch",
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
                if (path.contains(this.protocolFileName)) {
                    protocolUriMap[path] = URI.create(jsonNode.get("url").asText())
                }
            }
        } catch (e: WebApplicationException) {
            throw io.ktor.utils.io.errors.IOException("Failed to retrieve protocols URIs from github", e)
        } catch (e: Exception) {
            throw io.ktor.utils.io.errors.IOException("Exception when retrieving protocol uri map info", e)
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
            return objectMapper.readTree(parserProtocol)
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(GithubProtocolFetcherStrategy::class.java)

        private const val GITHUB_API_URI = "https://api.github.com/repos/"
    }
}
