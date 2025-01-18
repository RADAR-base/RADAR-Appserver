/*
 *
 *  *
 *  *  * Copyright 2018 King's College London
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *  *
 *  *
 *
 */
package org.radarbase.appserver.auth

import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith
import org.radarbase.appserver.auth.ProjectEndpointAuthTest.Companion.createURLWithPort
import org.radarbase.appserver.auth.common.MPOAuthHelper
import org.radarbase.appserver.auth.common.OAuthHelper
import org.radarbase.appserver.dto.ProjectDto
import org.radarbase.appserver.dto.fcm.FcmNotificationDto
import org.radarbase.appserver.dto.fcm.FcmNotifications
import org.radarbase.appserver.dto.fcm.FcmUserDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.*
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.jdbc.JdbcTestUtils
import org.springframework.web.client.ResourceAccessException
import java.time.Duration
import java.time.Instant

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class NotificationEndpointAuthTest {
    @Transient
    private val fcmNotificationDto = FcmNotificationDto()
        .withScheduledTime(Instant.now().plus(Duration.ofSeconds(100)))
        .withBody("Test Body")
        .withSourceId("test-source")
        .withTitle("Test Title")
        .withTtlSeconds(86400)
        .withFcmMessageId("123455")
        .withAdditionalData(HashMap<String?, String?>())
        .withAppPackage("armt")
        .withSourceType("armt")
        .withType("ESM")

    @LocalServerPort
    @Transient
    private val port = 0

    @BeforeEach
    fun createUserAndProject() {
        val projectDto: ProjectDto = ProjectDto(null, "radar", null, null)
        val projectEntity = HttpEntity<ProjectDto?>(projectDto, AUTH_HEADER)

        restTemplate.exchange<ProjectDto?>(
            "http://localhost:" + port + ProjectEndpointAuthTest.PROJECT_PATH,
            HttpMethod.POST,
            projectEntity,
            ProjectDto::class.java
        )

        val userDto: FcmUserDto? =
                FcmUserDto().apply {
                    projectId = "radar"
                    this.language = "en"
                    enrolmentDate = Instant.now()
                    fcmToken = "xxx"
                    subjectId = "sub-1"
                    timezone = "Europe/London"
                }
        val userDtoHttpEntity = HttpEntity<FcmUserDto?>(userDto, AUTH_HEADER)

        restTemplate.exchange<FcmUserDto?>(
            createURLWithPort(
                port,
                (ProjectEndpointAuthTest.PROJECT_PATH
                        + UserEndpointAuthTest.DEFAULT_PROJECT
                        + UserEndpointAuthTest.USER_PATH)
            ),
            HttpMethod.POST,
            userDtoHttpEntity,
            FcmUserDto::class.java
        )
    }

    @Test
    fun unauthorisedViewNotificationsForUser() {
        val notificationDtoHttpEntity = HttpEntity<FcmNotifications?>(null, HEADERS)

        val notificationDtoResponseEntity: ResponseEntity<FcmNotifications?> =
            restTemplate.exchange<FcmNotifications?>(
                createURLWithPort(
                    port,
                    (ProjectEndpointAuthTest.PROJECT_PATH
                            + UserEndpointAuthTest.DEFAULT_PROJECT
                            + UserEndpointAuthTest.USER_PATH
                            + DEFAULT_USER
                            + "/messaging/notifications")
                ),
                HttpMethod.GET,
                notificationDtoHttpEntity,
                FcmNotifications::class.java
            )

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, notificationDtoResponseEntity.statusCode)
    }

    @Test
    fun unauthorisedViewNotificationsForProject() {
        val notificationDtoHttpEntity = HttpEntity<FcmNotifications?>(null, HEADERS)

        val notificationDtoResponseEntity: ResponseEntity<FcmNotifications?> =
            restTemplate.exchange<FcmNotifications?>(
                createURLWithPort(
                    port,
                    (ProjectEndpointAuthTest.PROJECT_PATH
                            + UserEndpointAuthTest.DEFAULT_PROJECT
                            + "/messaging/notifications")
                ),
                HttpMethod.GET,
                notificationDtoHttpEntity,
                FcmNotifications::class.java
            )

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, notificationDtoResponseEntity.statusCode)
    }

    @Test
    fun unauthorisedCreateNotificationsForUser() {
        val notificationDtoHttpEntity =
            HttpEntity<FcmNotificationDto?>(fcmNotificationDto, HEADERS)

        var notificationDtoResponseEntity: ResponseEntity<FcmNotificationDto?>? = null
        try {
            notificationDtoResponseEntity =
                restTemplate.exchange<FcmNotificationDto?>(
                    createURLWithPort(
                        port,
                        (ProjectEndpointAuthTest.PROJECT_PATH
                                + UserEndpointAuthTest.DEFAULT_PROJECT
                                + UserEndpointAuthTest.USER_PATH
                                + DEFAULT_USER
                                + NOTIFICATION_PATH)
                    ),
                    HttpMethod.POST,
                    notificationDtoHttpEntity,
                    FcmNotificationDto::class.java
                )
        } catch (e: ResourceAccessException) {
            Assertions.assertEquals(notificationDtoResponseEntity, null)
        }
    }

    @Test
    @Order(1)
    fun createNotificationForUser() {
        val notificationDtoHttpEntity =
            HttpEntity<FcmNotificationDto?>(fcmNotificationDto, AUTH_HEADER)

        val notificationDtoResponseEntity: ResponseEntity<FcmNotificationDto?> =
            restTemplate.exchange<FcmNotificationDto?>(
                createURLWithPort(
                    port,
                    (ProjectEndpointAuthTest.PROJECT_PATH
                            + UserEndpointAuthTest.DEFAULT_PROJECT
                            + UserEndpointAuthTest.USER_PATH
                            + DEFAULT_USER
                            + NOTIFICATION_PATH)
                ),
                HttpMethod.POST,
                notificationDtoHttpEntity,
                FcmNotificationDto::class.java
            )

        Assertions.assertEquals(HttpStatus.CREATED, notificationDtoResponseEntity.statusCode)
    }

    @Test
    @Order(2)
    fun createBatchNotificationsForUser() {
        fcmNotificationDto.title = "new title"
        fcmNotificationDto.fcmMessageId = "xxxyyyy"
        val notificationDtoHttpEntity =
            HttpEntity<FcmNotifications?>(
                FcmNotifications()
                    .withNotifications(
                        listOf<FcmNotificationDto>(fcmNotificationDto)
                    ),
                AUTH_HEADER
            )

        val notificationDtoResponseEntity: ResponseEntity<FcmNotifications?> =
            restTemplate.exchange<FcmNotifications?>(
                createURLWithPort(
                    port,
                    (ProjectEndpointAuthTest.PROJECT_PATH
                            + UserEndpointAuthTest.DEFAULT_PROJECT
                            + UserEndpointAuthTest.USER_PATH
                            + DEFAULT_USER
                            + NOTIFICATION_PATH
                            + "/batch")
                ),
                HttpMethod.POST,
                notificationDtoHttpEntity,
                FcmNotifications::class.java
            )

        Assertions.assertEquals(HttpStatus.OK, notificationDtoResponseEntity.statusCode)
    }

    @Test
    fun viewNotificationsForUser() {
        val notificationDtoHttpEntity = HttpEntity<FcmNotifications?>(null, AUTH_HEADER)

        val notificationDtoResponseEntity: ResponseEntity<FcmNotifications?> =
            restTemplate.exchange<FcmNotifications?>(
                createURLWithPort(
                    port,
                    (ProjectEndpointAuthTest.PROJECT_PATH
                            + UserEndpointAuthTest.DEFAULT_PROJECT
                            + UserEndpointAuthTest.USER_PATH
                            + DEFAULT_USER
                            + NOTIFICATION_PATH)
                ),
                HttpMethod.GET,
                notificationDtoHttpEntity,
                FcmNotifications::class.java
            )

        Assertions.assertEquals(HttpStatus.OK, notificationDtoResponseEntity.statusCode)
    }

    @Test
    fun viewNotificationsForProject() {
        val notificationDtoHttpEntity = HttpEntity<FcmNotifications?>(null, AUTH_HEADER)

        val notificationDtoResponseEntity: ResponseEntity<FcmNotifications?> =
            restTemplate.exchange<FcmNotifications?>(
                createURLWithPort(
                    port,
                    (ProjectEndpointAuthTest.PROJECT_PATH
                            + UserEndpointAuthTest.DEFAULT_PROJECT
                            + NOTIFICATION_PATH)
                ),
                HttpMethod.GET,
                notificationDtoHttpEntity,
                FcmNotifications::class.java
            )

        Assertions.assertEquals(HttpStatus.OK, notificationDtoResponseEntity.statusCode)
    }

    @Test
    fun forbiddenViewNotificationsForOtherUser() {
        val notificationDtoHttpEntity = HttpEntity<FcmNotifications?>(null, AUTH_HEADER)

        val notificationDtoResponseEntity: ResponseEntity<FcmNotifications?> =
            restTemplate.exchange<FcmNotifications?>(
                createURLWithPort(
                    port,
                    (ProjectEndpointAuthTest.PROJECT_PATH
                            + UserEndpointAuthTest.DEFAULT_PROJECT
                            + UserEndpointAuthTest.USER_PATH
                            + "/sub-2"
                            + NOTIFICATION_PATH)
                ),
                HttpMethod.GET,
                notificationDtoHttpEntity,
                FcmNotifications::class.java
            )

        Assertions.assertEquals(HttpStatus.FORBIDDEN, notificationDtoResponseEntity.statusCode)
    }

    @Test
    fun forbiddenViewNotificationsForOtherProject() {
        val notificationDtoHttpEntity = HttpEntity<FcmNotifications?>(null, HEADERS)

        val notificationDtoResponseEntity: ResponseEntity<FcmNotifications?> =
            restTemplate.exchange<FcmNotifications?>(
                createURLWithPort(
                    port, ProjectEndpointAuthTest.PROJECT_PATH + "/test" + NOTIFICATION_PATH
                ),
                HttpMethod.GET,
                notificationDtoHttpEntity,
                FcmNotifications::class.java
            )

        Assertions.assertEquals(HttpStatus.UNAUTHORIZED, notificationDtoResponseEntity.statusCode)
    }

    companion object {
        private val HEADERS = HttpHeaders()
        private const val NOTIFICATION_PATH = "/messaging/notifications"
        private const val DEFAULT_USER = "/sub-1"
        private var AUTH_HEADER: HttpHeaders? = null
        private val restTemplate = TestRestTemplate()

        @BeforeAll
        @JvmStatic
        fun init() {
            val oAuthHelper: OAuthHelper = MPOAuthHelper()
            AUTH_HEADER = HttpHeaders()
            AUTH_HEADER!!.setBearerAuth(oAuthHelper.getAccessToken())
        }

        @AfterAll
        @JvmStatic
        fun clearDatabase(@Autowired jdbcTemplate: JdbcTemplate) {
            JdbcTestUtils.deleteFromTables(jdbcTemplate, "notifications", "users", "projects")
        }

    }
}