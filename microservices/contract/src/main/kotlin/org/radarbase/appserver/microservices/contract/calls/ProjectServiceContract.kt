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
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import org.radarbase.appserver.microservices.contract.client.ClientsContract
import org.radarbase.appserver.microservices.contract.response.ProxyResponse
import org.radarbase.appserver.microservices.contract.utils.Utils.createProxyFromResponse
import org.radarbase.appserver.microservices.contract.utils.Utils.normalizedUri
import org.radarbase.appserver.microservices.contract.utils.Utils.tryProxyRequest
import org.radarbase.appserver.microservices.core.dto.ProjectDto

@Suppress("unused")
object ProjectServiceContract {
    private const val PROJECT_SERVICE = "project-service"
    private val client: HttpClient = ClientsContract.retrieveClientForService(PROJECT_SERVICE)

    suspend fun addProject(
        project: ProjectDto,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("ProjectClient::addProject") {
            client.post(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments("projects")
                }
                setBody(project)
                contentType(ContentType.Application.Json)
            }.let { response -> createProxyFromResponse(response) }
        }
    }

    suspend fun updateProject(
        projectId: String,
        project: ProjectDto,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("ProjectClient::updateProject") {
            client.put(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments("projects", projectId)
                }
                setBody(project)
                contentType(ContentType.Application.Json)
            }.let { response -> createProxyFromResponse(response) }
        }
    }

    suspend fun getAllProjects(
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("ProjectClient::getAllProject") {
            client.get(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments("projects")
                }
            }.let { response -> createProxyFromResponse(response) }
        }

    }

    suspend fun getProjectUsingId(
        id: Long,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("ProjectClient::getProjectUsingId") {
            client.get(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments("projects", "project")
                    parameters.append("id", id.toString())
                }
            }.let { response -> createProxyFromResponse(response) }
        }
    }

    suspend fun getProjectUsingProjectId(
        projectId: String,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("ProjectClient::getProjectUsingProjectId") {
            return@tryProxyRequest client.get(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments("projects", projectId)
                }
            }.let { response -> createProxyFromResponse(response) }
        }
    }

    fun close() {
        ClientsContract.closeServiceClient(PROJECT_SERVICE)
    }
}
