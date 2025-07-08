package org.radarbase.appserver.jersey.auth.commons

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.basicAuth
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual.equalTo
import org.radarbase.ktor.auth.OAuth2AccessToken
import org.radarbase.ktor.auth.bearer

class MpOAuthSupport {
    private lateinit var httpClient: HttpClient

    fun init() {
        httpClient = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                    },
                )
            }
            defaultRequest {
                url("${MANAGEMENTPORTAL_URL}/")
            }
        }
    }

    suspend fun requestAccessToken(): String {
        val response = httpClient.submitForm(
            url = "oauth/token",
            formParameters = Parameters.build {
                append("username", ADMIN_USER)
                append("password", ADMIN_PASSWORD)
                append("grant_type", "password")
            },
        ) {
            basicAuth(username = MP_CLIENT, password = "")
        }
        assertThat(response.status, equalTo(HttpStatusCode.OK))
        val token = response.body<OAuth2AccessToken>()

        val tokenUrl = httpClient.get("api/oauth-clients/pair") {
            url {
                parameters.append("clientId", REST_CLIENT)
                parameters.append("login", "sub-1")
                parameters.append("persistent", "false")
            }
            bearer(requireNotNull(token.accessToken))
        }.body<MPPairResponse>().tokenUrl

        println("Requesting refresh token")
        val refreshToken = httpClient.get(tokenUrl).body<MPMetaToken>().refreshToken

        return requireNotNull(
            httpClient.submitForm(
                url = "oauth/token",
                formParameters = Parameters.build {
                    append("grant_type", "refresh_token")
                    append("refresh_token", refreshToken)
                },
            ) {
                basicAuth(REST_CLIENT, "")
            }.body<OAuth2AccessToken>().accessToken,
        )
    }

    companion object {
        private const val MANAGEMENTPORTAL_URL = "http://localhost:8080/managementportal"
        const val MP_CLIENT = "ManagementPortalapp"
        const val REST_CLIENT = "pRMT"
        const val ADMIN_USER = "admin"
        const val ADMIN_PASSWORD = "admin"
    }

}
