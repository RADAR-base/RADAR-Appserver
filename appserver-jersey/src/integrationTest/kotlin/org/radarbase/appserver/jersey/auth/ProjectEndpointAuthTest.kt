package org.radarbase.appserver.jersey.auth

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.radarbase.appserver.jersey.auth.commons.MpOAuthSupport
import org.radarbase.appserver.jersey.dto.ProjectDto

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ProjectEndpointAuthTest {

    @Test
    fun unAuthorizedCreatedProject(): Unit = runBlocking {
        val project = ProjectDto(projectId = "radar")
        val response = httpClient.post(PROJECT_PATH) {
            contentType(ContentType.Application.Json)
            setBody(project)
        }
        assertEquals(response.status, HttpStatusCode.Unauthorized)
    }

    @Test
    fun unAuthorizedViewProjects(): Unit = runBlocking {
        val response = httpClient.get(PROJECT_PATH) {
            accept(ContentType.Application.Json)
        }
        assertEquals(response.status, HttpStatusCode.Unauthorized)
    }

    @Test
    fun unAuthorizedViewSingleProject(): Unit = runBlocking {
        val response = httpClient.get("$PROJECT_PATH/radar") {
            accept(ContentType.Application.Json)
        }
        assertEquals(response.status, HttpStatusCode.Unauthorized)
    }

    @Test
    fun forbiddenViewProjects(): Unit = runBlocking {
        val response = httpClient.get(PROJECT_PATH) {
            accept(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${AUTH_HEADERS[HttpHeaders.Authorization]}")
        }
        // Only Admins Can View List Of All Projects
        assertEquals(response.status, HttpStatusCode.Forbidden)
    }

    @Test
    @Order(1)
    fun createSingleProjectWithAuth() = runBlocking {
        val project = ProjectDto(projectId = "radar")
        val response = httpClient.post(PROJECT_PATH) {
            contentType(ContentType.Application.Json)
            setBody(project)
            header(HttpHeaders.Authorization, "Bearer ${AUTH_HEADERS[HttpHeaders.Authorization]}")
        }

        if (response.status == HttpStatusCode.ExpectationFailed) {
            return@runBlocking
        }
        assertEquals(HttpStatusCode.Created, response.status)
    }

    @Test
    @Order(2)
    fun getSingleProjectWithAuth() = runBlocking {
        val response = httpClient.get("$PROJECT_PATH/radar") {
            accept(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${AUTH_HEADERS[HttpHeaders.Authorization]}")
        }
        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    @Order(3)
    fun getForbiddenProjectWithAuth() = runBlocking {
        val response = httpClient.get("$PROJECT_PATH/test") {
            accept(ContentType.Application.Json)
            header(HttpHeaders.Authorization, "Bearer ${AUTH_HEADERS[HttpHeaders.Authorization]}")
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }


    companion object {
        private const val APPSERVER_URL = "http://localhost:8080"
        private const val PROJECT_PATH = "projects"
        private lateinit var AUTH_HEADERS: Headers
        private lateinit var httpClient: HttpClient

        @BeforeAll
        @JvmStatic
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
                    url("${APPSERVER_URL}/")
                }
            }

            val oAuthSupport = MpOAuthSupport().apply {
                init()
            }

            AUTH_HEADERS = runBlocking {
                headersOf(
                    HttpHeaders.Authorization,
                    "Bearer ${oAuthSupport.requestAccessToken()}",
                )
            }
        }
    }
}
