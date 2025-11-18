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

package org.radarbase.appserver.microservices.contract.calls

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.client.request.accept
import io.ktor.http.appendPathSegments
import io.ktor.http.path
import org.radarbase.appserver.microservices.contract.client.ClientsContract
import org.radarbase.appserver.microservices.contract.response.ProxyResponse
import org.radarbase.appserver.microservices.contract.utils.Utils.createProxyFromResponse
import org.radarbase.appserver.microservices.contract.utils.Utils.tryProxyRequest
import org.radarbase.appserver.microservices.core.utils.Paths.GITHUB_CONTENT_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.GITHUB_PATH

@Suppress("unused")
object GithubServiceContract {
    private const val GITHUB_SERVICE = "github-service"
    private val client: HttpClient = ClientsContract.retrieveClientForService(GITHUB_SERVICE)

    suspend fun getGithubContent(baseUrl: String, githubContentUrl: String): ProxyResponse {
        return tryProxyRequest("GithubClient::getGithubContent") {
            return@tryProxyRequest client.get(baseUrl) {
                url {
                    appendPathSegments(GITHUB_PATH, GITHUB_CONTENT_PATH)
                    parameters.append("url", githubContentUrl)
                }
                accept(ContentType.Text.Plain)
            }.let { response -> createProxyFromResponse(response) }
        }
    }

    fun close() {
        client.close()
    }
}
