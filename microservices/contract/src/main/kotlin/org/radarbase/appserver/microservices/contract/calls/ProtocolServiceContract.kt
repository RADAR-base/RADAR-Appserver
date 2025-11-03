package org.radarbase.appserver.microservices.contract.calls

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.http.appendPathSegments
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.radarbase.appserver.microservices.contract.response.ProxyResponse
import org.radarbase.appserver.microservices.contract.utils.Utils.createProxyFromResponse
import org.radarbase.appserver.microservices.contract.utils.Utils.getEndpointFromParts
import org.radarbase.appserver.microservices.contract.utils.Utils.tryProxyRequest
import kotlin.time.Duration.Companion.seconds

@Suppress("unused")
object ProtocolServiceContract {
    private val client: HttpClient = HttpClient(CIO) {
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

    suspend fun getProtocols(
        baseUrl: String,
        path: String,
        prefix: String? = null,
    ): ProxyResponse {
        return getEndpointFromParts(baseUrl, path, prefix).let { endpoint ->
            tryProxyRequest {
                return@tryProxyRequest client.get(endpoint) {
                    url {
                        appendPathSegments("protocols")
                    }
                }.let { response -> createProxyFromResponse(response) }
            }
        }
    }

    suspend fun getProtocolForSubject(
        projectId: String,
        subjectId: String,
        baseUrl: String,
        path: String,
        prefix: String? = null,
    ): ProxyResponse {
        return getEndpointFromParts(baseUrl, path, prefix).let { endpoint ->
            tryProxyRequest {
                return@tryProxyRequest client.get(endpoint) {
                    url {
                        appendPathSegments("projects", projectId, "users", subjectId, "protocols")
                    }
                }.let { response -> createProxyFromResponse(response) }
            }
        }
    }

    suspend fun getProtocolsForProject(
        projectId: String,
        baseUrl: String,
        path: String,
        prefix: String? = null,
    ): ProxyResponse {
        return getEndpointFromParts(baseUrl, path, prefix).let { endpoint ->
            tryProxyRequest {
                return@tryProxyRequest client.get(endpoint) {
                    url {
                        appendPathSegments("projects", projectId, "protocols")
                    }
                }.let { response -> createProxyFromResponse(response) }
            }
        }
    }

    fun close() {
        client.close()
    }
}
