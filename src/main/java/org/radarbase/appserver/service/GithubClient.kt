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

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.util.UriComponentsBuilder
import java.io.IOException
import java.net.MalformedURLException
import java.net.URI

/**
 * A singleton component responsible for interacting with the GitHub API.
 * Provides utility methods for authorized and unauthorized access to GitHub content.
 * The configuration for this client can be customized through application properties.
 *
 * @property httpTimeout The timeout in seconds for HTTP requests.
 * @property githubToken The personal access token for GitHub API authentication.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class GithubClient(
    @Value("\${security.github.client.maxContentLength:1000000}")
    @Transient
    private val maxContentLength: Int,

    @Value("\${security.github.client.timeout:10}") httpTimeout: Int,
    @Value("\${security.github.client.token:}") githubToken: String
) {

    @Transient
    private val authorizationHeader: String =
        if (githubToken.isEmpty()) "" else "Bearer " + githubToken.trim { it <= ' ' }

    /**
     * An HTTP client configured to interact with the GitHub API using the CIO engine.
     * This client sets default request headers, specifies request timeout, and
     * enables automatic redirection following.
     */
    @Transient
    private val client = HttpClient(CIO) {
        defaultRequest {
            header(HttpHeaders.Accept, GITHUB_API_ACCEPT_HEADER)
        }
        engine {
            requestTimeout = httpTimeout * 1000L
        }
        followRedirects = true
    }

    /**
     * Retrieves the content from a specified GitHub URL. This method attempts to make an authenticated request
     * if authentication is enabled. If unauthorized access occurs, it retries without authentication.
     *
     * @param url The URL of the GitHub resource to fetch content from.
     * @param authenticated Indicates whether the request should include an authentication header (default is true).
     * @return The content retrieved from the specified GitHub URL as a String.
     * @throws ResponseStatusException If the response indicates an error or the content size is too large.
     */
    fun getGithubContent(url: String, authenticated: Boolean = true): String = runBlocking {
        val request = client.prepareGet(getValidGithubUri(url)) {
            if (authenticated && authorizationHeader.isNotEmpty()) {
                header(HttpHeaders.Authorization, authorizationHeader)
            }
        }
        val response = request.execute()
        if (response.status.value in 200..299) {
            val contentLengthHeader = response.headers[HttpHeaders.ContentLength]?.toIntOrNull()
            contentLengthHeader?.let { checkContentLength(it) }

            val contentStream: ByteArray = response.body<ByteArray>()
            checkContentLength(contentStream.size)
            return@runBlocking String(contentStream)
        } else if (response.status.value == 401 && authenticated) {
            logger.warn("Unauthorized access to Github content from URL {}, retrying..", url)
            return@runBlocking getGithubContent(url, false)
        } else {
            logger.error("Error getting Github content from URL {} : {}", url, response)
            throw ResponseStatusException(
                HttpStatus.valueOf(response.status.value), "Github content could not be retrieved"
            )
        }
    }

    fun getGithubContent(url: String): String = getGithubContent(url, true)

    /**
     * Validates the given content length against a predefined maximum content length.
     * Throws a `ResponseStatusException` if the content length exceeds the allowed maximum.
     *
     * @param contentLength the length of the content to be validated
     */
    private fun checkContentLength(contentLength: Int) {
        if (contentLength > maxContentLength) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST, "Github content is too large"
            )
        }
    }

    /**
     * Validates and converts a given GitHub API URI into a standardized format.
     *
     * @param uri The GitHub API URI to validate and format.
     * @return The validated and properly formatted GitHub API URI.
     * @throws IOException If the provided URI is invalid or does not meet the required GitHub API standards.
     */
    @Throws(IOException::class)
    fun getValidGithubUri(uri: String): String {
        val parsedUri = URI(uri)
        if (parsedUri.host.equals(GITHUB_API_URI, ignoreCase = true) &&
            parsedUri.scheme.equals("https", ignoreCase = true) &&
            (parsedUri.port == -1 || parsedUri.port == 443)
        ) {
            return UriComponentsBuilder.newInstance()
                .scheme("https").host(parsedUri.host).path(parsedUri.path).query(parsedUri.query).build().toString()
        } else {
            throw MalformedURLException("Invalid Github URL: $uri")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(GithubClient::class.java)

        private const val GITHUB_API_URI = "api.github.com"
        private const val GITHUB_API_ACCEPT_HEADER = "application/vnd.github.v3+json"
    }
}
