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

package org.radarbase.appserver.service

import org.radarbase.appserver.util.CachedFunction
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import java.io.IOException
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
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class GithubService(
    @field:Transient private val githubClient: GithubClient,

    @Value("\${security.github.cache.duration:3600}") cacheTime: Int,
    @Value("\${security.github.cache.retryDuration:60}") retryTime: Int,
    @Value("\${security.github.cache.size:10000}") maxSize: Int,
) {

    @Transient
    private val cachedGetContent: CachedFunction<String, String> = CachedFunction<String, String>(
        githubClient::getGithubContent,
        Duration.ofSeconds(cacheTime.toLong()),
        Duration.ofSeconds(retryTime.toLong()),
        maxSize,
    )

    /**
     * Retrieves the content from a given GitHub URL. The result is cached to optimize repeated calls.
     *
     * @param url The URL of the GitHub resource to retrieve the content from.
     * @return The content retrieved from the specified GitHub URL as a string.
     * @throws IOException If an I/O error occurs during the request.
     * @throws ResponseStatusException If an HTTP response indicates a failure.
     * @throws IllegalStateException If an unknown exception occurs during execution.
     */
    @Throws(IOException::class)
    fun getGithubContent(url: String): String = try {
        this.cachedGetContent.applyWithException(url)
    } catch (ex: IOException) {
        throw ex
    } catch (ex: ResponseStatusException) {
        throw ex
    } catch (ex: Exception) {
        throw IllegalStateException("Unknown exception $ex", ex)
    }

    /**
     * Fetches the content of the given GitHub URL directly without utilizing the caching mechanism.
     *
     * @param url The URL of the GitHub resource to be fetched.
     * @return The content of the GitHub resource as a string.
     * @throws IOException If an error occurs during the request.
     */
    @Throws(IOException::class)
    fun getGithubContentWithoutCache(url: String): String {
        return githubClient.getGithubContent(url)
    }
}
