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
import org.radarbase.appserver.jersey.dto.fcm.FcmNotificationDto
import org.radarbase.appserver.jersey.dto.fcm.FcmNotifications
import org.radarbase.appserver.jersey.dto.fcm.FcmUserDto
import java.time.Duration
import java.time.Instant

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class NotificationEndpointAuthTest {

    val notification = FcmNotificationDto().apply {
        scheduledTime = Instant.now().plus(Duration.ofSeconds(100))
        body = "Test Body"
        sourceId = "test-source"
        title = "Test Title"
        ttlSeconds = 86400
        fcmMessageId = "123455"
        additionalData = mutableMapOf()
        appPackage = "armt"
        sourceType = "armt"
        type = "ESM"
    }

    @BeforeEach
    fun createUserAndProject(): Unit = runBlocking {
        val project = ProjectDto(projectId = "radar")

        httpClient.post(PROJECT_PATH) {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, AUTH_HEADERS[HttpHeaders.Authorization])
            setBody(project)
        }

        val fcmUserDto = FcmUserDto(
            projectId = "radar",
            language = "en",
            enrolmentDate = Instant.now(),
            fcmToken = "xxx",
            subjectId = "sub-1",
            timezone = "Europe/London",
        )

        httpClient.post("$PROJECT_PATH/$DEFAULT_PROJECT/$USER_PATH") {
            contentType(ContentType.Application.Json)
            header(HttpHeaders.Authorization, AUTH_HEADERS[HttpHeaders.Authorization])
            setBody(fcmUserDto)
        }
    }

    @Test
    fun unAuthorizedViewNotificationsForUser(): Unit = runBlocking {
        val response = httpClient.get(
            "$PROJECT_PATH/$DEFAULT_PROJECT/$USER_PATH/$DEFAULT_USER/$NOTIFICATION_PATH",
        ) {
            accept(ContentType.Application.Json)
        }

        assertEquals(response.status, HttpStatusCode.Unauthorized)
    }

    @Test
    fun unAuthorizedViewNotificationsForProject(): Unit = runBlocking {
        val response = httpClient.get(
            "$PROJECT_PATH/$DEFAULT_PROJECT/$NOTIFICATION_PATH",
        ) {
            accept(ContentType.Application.Json)
        }

        assertEquals(response.status, HttpStatusCode.Unauthorized)
    }

    @Test
    fun unauthorizedCreateNotificationForUser(): Unit = runBlocking {
        val response = httpClient.post(
            "$PROJECT_PATH/$DEFAULT_PROJECT/$USER_PATH/$DEFAULT_USER/$NOTIFICATION_PATH",
        ) {
            contentType(ContentType.Application.Json)
            setBody(notification)
        }

        assertEquals(response.status, HttpStatusCode.Unauthorized)
    }

    @Order(1)
    @Test
    fun createNotificationForUser(): Unit = runBlocking {
        val response = httpClient.post(
            "$PROJECT_PATH/$DEFAULT_PROJECT/$USER_PATH/$DEFAULT_USER/$NOTIFICATION_PATH",
        ) {
            setBody(notification)
            header(HttpHeaders.Authorization, AUTH_HEADERS[HttpHeaders.Authorization])
            contentType(ContentType.Application.Json)
        }

        assertEquals(response.status, HttpStatusCode.Created)
    }

    @Order(2)
    @Test
    fun createBatchNotificationForUser(): Unit = runBlocking {
        val singleNotification = notification
        val notifications = FcmNotifications(
            mutableListOf(
                singleNotification.apply {
                    title = "Test Title 1"
                    fcmMessageId = "xxxyyyy"
                },
            ),
        )

        val response = httpClient.post(
            "$PROJECT_PATH/$DEFAULT_PROJECT/$USER_PATH/$DEFAULT_USER/$NOTIFICATION_PATH/batch",
        ) {
            setBody(notifications)
            header(HttpHeaders.Authorization, AUTH_HEADERS[HttpHeaders.Authorization])
            contentType(ContentType.Application.Json)
        }

        assertEquals(response.status, HttpStatusCode.Created)
    }

    @Test
    fun viewNotificationsForUser(): Unit = runBlocking {
        val response = httpClient.get(
            "$PROJECT_PATH/$DEFAULT_PROJECT/$USER_PATH/$DEFAULT_USER/$NOTIFICATION_PATH",
        ) {
            accept(ContentType.Application.Json)
            header(HttpHeaders.Authorization, AUTH_HEADERS[HttpHeaders.Authorization])
        }

        assertEquals(response.status, HttpStatusCode.OK)
    }

    @Test
    fun viewNotificationsForProject(): Unit = runBlocking {
        val response = httpClient.get(
            "$PROJECT_PATH/$DEFAULT_PROJECT/$NOTIFICATION_PATH",
        ) {
            accept(ContentType.Application.Json)
            header(HttpHeaders.Authorization, AUTH_HEADERS[HttpHeaders.Authorization])
        }

        assertEquals(response.status, HttpStatusCode.OK)
    }

    @Test
    fun forbiddenViewNotificationsForOtherUser(): Unit = runBlocking {
        val response = httpClient.get(
            "$PROJECT_PATH/$DEFAULT_PROJECT/$USER_PATH/sub-2/$NOTIFICATION_PATH",
        ) {
            accept(ContentType.Application.Json)
            header(HttpHeaders.Authorization, AUTH_HEADERS[HttpHeaders.Authorization])
        }

        assertEquals(response.status, HttpStatusCode.Forbidden)
    }

    @Test
    fun forbiddenViewNotificationsForOtherProject(): Unit = runBlocking {
        val response = httpClient.get(
            "$PROJECT_PATH/other-project/$NOTIFICATION_PATH",
        ) {
            accept(ContentType.Application.Json)
            header(HttpHeaders.Authorization, AUTH_HEADERS[HttpHeaders.Authorization])
        }

        assertEquals(response.status, HttpStatusCode.Forbidden)
    }

    companion object {
        private const val APPSERVER_URL = "http://localhost:8080"
        private lateinit var AUTH_HEADERS: Headers
        private lateinit var httpClient: HttpClient
        private const val PROJECT_PATH = "projects"
        private const val USER_PATH = "users"
        private const val DEFAULT_PROJECT = "radar"
        private const val DEFAULT_USER = "sub-1"
        private const val NOTIFICATION_PATH = "messaging/notifications"

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
