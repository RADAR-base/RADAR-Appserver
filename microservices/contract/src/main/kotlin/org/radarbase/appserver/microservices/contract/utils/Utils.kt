package org.radarbase.appserver.microservices.contract.utils

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import org.radarbase.appserver.microservices.contract.response.ProxyResponse

object Utils {
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

    fun getEndpointFromParts(baseUrl: String, path: String, prefix: String?): String {
        val base: String = normalizedUri(baseUrl)
        val prefixSegment: String = normalizedPath(prefix)
        val servicePath: String = normalizedPath(path)

        return (base + prefixSegment + servicePath).removeSuffix("/")
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

}
