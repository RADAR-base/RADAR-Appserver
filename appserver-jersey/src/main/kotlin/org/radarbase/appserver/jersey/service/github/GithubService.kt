package org.radarbase.appserver.jersey.service.github

import io.ktor.utils.io.errors.IOException
import jakarta.inject.Inject
import jakarta.ws.rs.WebApplicationException
import org.radarbase.appserver.jersey.config.AppserverConfig
import org.radarbase.appserver.jersey.utils.cache.CachedFunction
import java.time.Duration

/**
 * A service component responsible for wrapping GitHub content-fetching functionality
 * with caching capabilities. This service allows efficient fetching of GitHub content,
 * reducing redundant calls to the GitHub API by using a local cache for frequently accessed content.
 *
 * Configuration for caching behavior can be customized via application properties.
 *
 * @property githubClient The [GithubClient] used for API interaction.
 * @property cacheTime The duration, in seconds, for which the content should be cached.
 * @property retryTime The duration, in seconds, for which retries are attempted in case of errors.
 * @property maxSize The maximum size of the cache.
 */
class GithubService @Inject constructor(
    private val githubClient: GithubClient,
    config: AppserverConfig
) {
    private val cacheTime: Long
    private val retryTime: Long
    private val maxSize: Int

    init {
        config.github.cache.let {  githubCacheConfig ->
            cacheTime = checkNotNull(githubCacheConfig.cacheDuration) { "Github cache duration cannot be null in config" }
            retryTime = checkNotNull(githubCacheConfig.retryDuration) { "Github retry duration cannot be null in config" }
            maxSize = githubCacheConfig.maxCacheSize
        }
    }

    private val cachedGithubContent: CachedFunction<String, String> = CachedFunction(
        githubClient::getGithubContent,
        Duration.ofSeconds(cacheTime),
        Duration.ofSeconds(retryTime),
        maxSize,
    )

    /**
     * Retrieves the content from a given GitHub URL. The result is cached to optimize repeated calls.
     *
     * @param url The URL of the GitHub resource to retrieve the content from.
     * @return The content retrieved from the specified GitHub URL as a string.
     * @throws IOException If an I/O error occurs during the request.
     * @throws WebApplicationException If an HTTP response indicates a failure.
     * @throws IllegalStateException If an unknown exception occurs during execution.
     */
    @Throws(IOException::class)
    suspend fun getGithubContent(url: String): String = try {
        this.cachedGithubContent.applyWithException(url)
    } catch (ex: IOException) {
        throw ex
    } catch (ex: WebApplicationException) {
        throw ex
    } catch (ex: Exception) {
        throw IllegalStateException("Unknown exception $ex", ex)
    }

    /**
     * Fetches the content of the given GitHub URL directly without the need of the caching mechanism.
     *
     * @param url The URL of the GitHub resource to be fetched.
     * @return The content of the GitHub resource as a string.
     * @throws IOException If an error occurs during the request.
     */
    @Throws(IOException::class)
    suspend fun getGithubContentWithoutCache(url: String): String {
        return githubClient.getGithubContent(url)
    }
}
