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
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.utils.EmptyContent.contentType
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.radarbase.appserver.microservices.contract.response.ProxyResponse
import org.radarbase.appserver.microservices.contract.utils.Utils.normalizedPath
import org.radarbase.appserver.microservices.contract.utils.Utils.normalizedUri
import org.radarbase.appserver.microservices.core.dto.ProjectDto
import kotlin.time.Duration.Companion.seconds

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
        val base: String = normalizedUri(baseUrl)
        val prefixSegment: String = normalizedPath(prefix)
        val servicePath: String = normalizedPath(path)

        val endpoint: String = (base + prefixSegment + servicePath).removeSuffix("/")

        return tryProxyRequest {
            return@tryProxyRequest client.post(endpoint) {
                setBody(project)
                contentType(ContentType.Application.Json)
            }.let { response -> createProxyFromResponse(response) }
        }
    }

    suspend fun createProxyFromResponse(resp: HttpResponse): ProxyResponse {
        val bodyBytes = resp.body<ByteArray>()
        val contentType = resp.headers["Content-Type"]
        val location = resp.headers["Location"]

        return ProxyResponse(
            status = resp.status.value,
            contentType = contentType,
            location = location,
            body = bodyBytes,
        )
    }

    suspend fun tryProxyRequest(request: suspend () -> ProxyResponse): ProxyResponse {
        return try {
            request()
        } catch (t: Throwable) {
            when (t) {
                is io.ktor.client.plugins.HttpRequestTimeoutException,
                is kotlinx.coroutines.TimeoutCancellationException,
                is java.net.SocketTimeoutException,
                    -> {
                    ProxyResponse(
                        status = 504,
                        contentType = "application/json",
                        body = """{"error":"gateway_timeout","message":"upstream timed out"}""".toByteArray(),
                    )
                }

                is java.net.ConnectException -> {
                    ProxyResponse(
                        status = 502,
                        contentType = "application/json",
                        body = """{"error":"bad_gateway","message":"cannot reach upstream"}""".toByteArray(),
                    )
                }

                is io.ktor.client.plugins.ResponseException -> {
                    try {
                        val er = t.response
                        val b = er.body<ByteArray>()
                        ProxyResponse(status = er.status.value, contentType = er.headers["Content-Type"], body = b)
                    } catch (_: Throwable) {
                        ProxyResponse(502, "application/json", body = """{"error":"bad_gateway"}""".toByteArray())
                    }
                }

                else -> ProxyResponse(502, "application/json", body = """{"error":"bad_gateway"}""".toByteArray())
            }
        }
    }

    fun close() {
        client.close()
    }

}
