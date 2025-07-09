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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.radarbase.appserver.jersey.auth.commons.MpOAuthSupport
import org.radarbase.appserver.jersey.dto.ProjectDto
import org.radarbase.appserver.jersey.dto.fcm.FcmUserDto
import java.time.Instant

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class UserEndpointAuthTest {
    val fcmUserDto = FcmUserDto(
        projectId = "radar",
        language = "en",
        enrolmentDate = Instant.now(),
        fcmToken = "xxx",
        subjectId = "sub-1",
        timezone = "Europe/London",
    )

    @BeforeEach
    fun createProject(): Unit = runBlocking {
        val project = ProjectDto(projectId = DEFAULT_PROJECT)

        httpClient.post {
            header(HttpHeaders.Authorization, AUTH_HEADERS[HttpHeaders.Authorization])
            contentType(ContentType.Application.Json)
            setBody(project)
        }
    }

    @Test
    fun unauthorizedViewSingleUser(): Unit = runBlocking {
        val response = httpClient.get("$PROJECT_PATH/$DEFAULT_PROJECT/$USER_PATH/sub-1") {
            accept(ContentType.Application.Json)
        }

        assertEquals(response.status, HttpStatusCode.Unauthorized)
    }

    @Test
    fun unAuthorizedCreateUser(): Unit = runBlocking {
        val response = httpClient.post("$PROJECT_PATH/$DEFAULT_PROJECT/$USER_PATH") {
            contentType(ContentType.Application.Json)
            setBody(fcmUserDto)
        }

        assertEquals(response.status, HttpStatusCode.Unauthorized)
    }

    @Test
    @Order(1)
    fun createUser(): Unit = runBlocking {
        val response = httpClient.post("$PROJECT_PATH/$DEFAULT_PROJECT/$USER_PATH") {
            header(HttpHeaders.Authorization, AUTH_HEADERS[HttpHeaders.Authorization])
            contentType(ContentType.Application.Json)
            setBody(fcmUserDto)
        }

        if (response.status == HttpStatusCode.ExpectationFailed) {
            // The auth was successful but expectation failed if the user already exits.
            // Since this is just an auth test we can return.
            return@runBlocking
        }

        assertEquals(response.status, HttpStatusCode.Created)
    }

    @Test
    @Order(2)
    fun viewUser(): Unit = runBlocking {
        val response = httpClient.get("$PROJECT_PATH/$DEFAULT_PROJECT/$USER_PATH/sub-1") {
            header(HttpHeaders.Authorization, AUTH_HEADERS[HttpHeaders.Authorization])
            accept(ContentType.Application.Json)
        }

        assertEquals(response.status, HttpStatusCode.OK)
    }

    @Test
    @Order(3)
    fun viewUsersInProject(): Unit = runBlocking {
        val response = httpClient.get("$PROJECT_PATH/$DEFAULT_PROJECT/$USER_PATH") {
            accept(ContentType.Application.Json)
            header(HttpHeaders.Authorization, AUTH_HEADERS[HttpHeaders.Authorization])
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    @Order(4)
    fun forbiddenViewUsersInOtherProject(): Unit = runBlocking {
        val response = httpClient.get("$PROJECT_PATH/other-project/$USER_PATH") {
            accept(ContentType.Application.Json)
            header(HttpHeaders.Authorization, AUTH_HEADERS[HttpHeaders.Authorization])
        }

        assertEquals(response.status, HttpStatusCode.Forbidden)
    }

    @Test
    @Order(5)
    fun viewAllUsers() = runBlocking {
        val response = httpClient.get(USER_PATH) {
            accept(ContentType.Application.Json)
            header(HttpHeaders.Authorization, AUTH_HEADERS[HttpHeaders.Authorization])
        }

        // Should return a filtered list of users for which the token has access.
        assertEquals(response.status, HttpStatusCode.OK)
    }

    companion object {
        private const val APPSERVER_URL = "http://localhost:8080"
        private const val PROJECT_PATH = "projects"
        private const val USER_PATH = "users"
        private const val DEFAULT_PROJECT = "radar"
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
