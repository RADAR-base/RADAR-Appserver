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
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.radarbase.appserver.microservices.contract.response.ProxyResponse
import org.radarbase.appserver.microservices.contract.utils.Utils.createProxyFromResponse
import org.radarbase.appserver.microservices.contract.utils.Utils.getEndpointFromParts
import org.radarbase.appserver.microservices.contract.utils.Utils.tryProxyRequest
import org.radarbase.appserver.microservices.core.dto.ProjectDto
import kotlin.time.Duration.Companion.seconds

@Suppress("unused")
object ProjectServiceContract {
    val client: HttpClient = HttpClient(CIO) {
        expectSuccess = true

        install(HttpTimeout) {
            connectTimeoutMillis = 15.seconds.inWholeMilliseconds
            socketTimeoutMillis = 15.seconds.inWholeMilliseconds
            requestTimeoutMillis = 15.seconds.inWholeMilliseconds
        }

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                },
            )
        }
    }

    suspend fun addProject(
        project: ProjectDto,
        baseUrl: String,
        path: String,
        prefix: String? = null,
    ): ProxyResponse {
        return getEndpointFromParts(baseUrl, path, prefix).let { endpoint ->
            tryProxyRequest {
                return@tryProxyRequest client.post(endpoint) {
                    setBody(project)
                    contentType(ContentType.Application.Json)
                }.let { response -> createProxyFromResponse(response) }
            }
        }
    }

    suspend fun updateProject(
        project: ProjectDto,
        baseUrl: String,
        path: String,
        prefix: String? = null,
    ): ProxyResponse {
        return getEndpointFromParts(baseUrl, path, prefix).let { endpoint ->
            tryProxyRequest {
                return@tryProxyRequest client.put(endpoint) {
                    setBody(project)
                    contentType(ContentType.Application.Json)
                }.let { response -> createProxyFromResponse(response) }
            }
        }
    }

    suspend fun getAllProjects(
        baseUrl: String,
        path: String,
        prefix: String? = null,
    ): ProxyResponse {
        return getEndpointFromParts(baseUrl, path, prefix).let { endpoint ->
            tryProxyRequest {
                return@tryProxyRequest createProxyFromResponse(client.get(endpoint))
            }
        }
    }

    suspend fun getProjectUsingId(
        id: Long,
        baseUrl: String,
        path: String,
        prefix: String? = null,
    ): ProxyResponse {
        return getEndpointFromParts(baseUrl, path, prefix).let { endpoint ->
            tryProxyRequest {
                return@tryProxyRequest client.get(endpoint) {
                    url {
                        appendPathSegments("project")
                        parameters.append("id", id.toString())
                    }
                }.let { response -> createProxyFromResponse(response) }
            }
        }
    }

    suspend fun getProjectUsingProjectId(
        projectId: String,
        baseUrl: String,
        path: String,
        prefix: String? = null,
    ): ProxyResponse {
        return getEndpointFromParts(baseUrl, path, prefix).let { endpoint ->
            tryProxyRequest {
                return@tryProxyRequest client.get(endpoint) {
                    url {
                        appendPathSegments("project", projectId)
                    }
                }.let { response -> createProxyFromResponse(response) }
            }
        }
    }

    fun close() {
        client.close()
    }

}
