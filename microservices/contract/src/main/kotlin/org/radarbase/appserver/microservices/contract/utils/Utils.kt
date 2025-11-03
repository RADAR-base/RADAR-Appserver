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

package org.radarbase.appserver.microservices.contract.utils

import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.CancellationException
import kotlinx.serialization.json.Json
import org.radarbase.appserver.microservices.contract.response.ProxyResponse
import org.radarbase.jersey.exception.HttpNotFoundException
import org.slf4j.LoggerFactory
import java.net.ConnectException

object Utils {
    private val logger = LoggerFactory.getLogger(Utils::class.java)
    val jsonUtils = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    fun normalizedPath(path: String?): String {
        val trimmed = path?.trim().orEmpty()
        if (trimmed.isBlank()) return ""
        return "/" + trimmed.trim('/').trim()
    }

    fun normalizedUri(uri: String): String {
        val trimmed = uri.trim()
        return if (trimmed.endsWith("/")) trimmed.removeSuffix("/") else trimmed
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

    inline fun <reified T : Any> deserializeDtoFromContract(
        proxyResponse: ProxyResponse,
        exceptionMessageProvider: () -> String,
    ): T {
        val proxyResponseBody = proxyResponse.body
        val content = proxyResponseBody?.decodeToString()

        if (content != null && proxyResponse.status in 200..299) {
            return jsonUtils.decodeFromString<T>(content)
        } else if (proxyResponse.status == HttpStatusCode.NotFound.value) {
            val (code, message) = exceptionMessageProvider().split(";")
            throw HttpNotFoundException(code.trim(), message.trim())
        } else {
            throw RuntimeException(content)
        }
    }

    suspend fun tryProxyRequest(caller: String, request: suspend () -> ProxyResponse): ProxyResponse {
        return try {
            request()
        } catch (t: Throwable) {
            logger.error("Proxy request failed for caller ({}) -> {} : {}", caller, t::class.simpleName, t)

            when (t) {
                is CancellationException -> throw t

                is HttpRequestTimeoutException, is SocketTimeoutException -> {
                    ProxyResponse(
                        status = 504,
                        contentType = "application/json",
                        body = """{"error":"gateway_timeout","message":"upstream timed out"}""".toByteArray(),
                    )
                }

                is ConnectException -> {
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
}
