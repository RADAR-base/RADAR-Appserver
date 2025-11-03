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
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import org.radarbase.appserver.microservices.contract.client.ClientsContract
import org.radarbase.appserver.microservices.contract.response.ProxyResponse
import org.radarbase.appserver.microservices.contract.utils.Utils.createProxyFromResponse
import org.radarbase.appserver.microservices.contract.utils.Utils.normalizedUri
import org.radarbase.appserver.microservices.contract.utils.Utils.tryProxyRequest
import org.radarbase.appserver.microservices.core.utils.Paths.PROTOCOLS_PATH

@Suppress("unused")
object ProtocolServiceContract {
    private const val PROTOCOL_SERVICE = "protocol-service"
    private val client: HttpClient = ClientsContract.retrieveClientForService(PROTOCOL_SERVICE)

    suspend fun getProtocols(
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("ProtocolClient::getProtocol") {
            client.get(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments(PROTOCOLS_PATH)
                }
                accept(ContentType.Application.Json)
            }.let { response -> createProxyFromResponse(response) }
        }

    }

    suspend fun getProtocolForSubject(
        projectId: String,
        subjectId: String,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("ProtocolClient::getProtocolForSubject") {
            client.get(normalizedUri(baseUrl)) {
                url {
                    appendPathSegments("projects", projectId, "users", subjectId, "protocols")
                }
                accept(ContentType.Application.Json)
            }.let { response -> createProxyFromResponse(response) }
        }
    }

    suspend fun getProtocolsForProject(
        projectId: String,
        baseUrl: String,
    ): ProxyResponse {
        return tryProxyRequest("ProtocolClient::getProtocolsForProject") {
            client.get(baseUrl) {
                url {
                    appendPathSegments("projects", projectId, "protocols")
                }
                accept(ContentType.Application.Json)
            }.let { response -> createProxyFromResponse(response) }
        }
    }

    fun close() {
        ClientsContract.closeServiceClient(PROTOCOL_SERVICE)
    }
}
