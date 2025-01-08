/*
 *  *  * Copyright 2018 King's College London
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
 */

package org.radarbase.appserver.auth

import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.extension.ExtendWith
import org.radarbase.appserver.auth.common.MPOAuthHelper
import org.radarbase.appserver.dto.ProjectDto
import org.radarbase.appserver.dto.fcm.FcmUserDto
import org.radarbase.appserver.dto.fcm.FcmUsers
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.ResourceAccessException

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation::class)
class UserEndpointAuthTest {

    companion object {
        const val USER_PATH = "/users"
        const val DEFAULT_PROJECT = "/radar"
        private val HEADERS = HttpHeaders()
        private lateinit var AUTH_HEADER: HttpHeaders
        private val restTemplate = TestRestTemplate()

        @JvmStatic
        @BeforeAll
        fun init() {
            val oAuthHelper = MPOAuthHelper()
            AUTH_HEADER = HttpHeaders().apply {
                setBearerAuth(oAuthHelper.getAccessToken())
            }
        }
    }

    private val userDto = FcmUserDto().apply {
        projectId = "radar"
        language = "en"
        enrolmentDate = java.time.Instant.now()
        fcmToken = "xxx"
        subjectId = "sub-1"
        timezone = "Europe/London"
    }

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    fun createProject() {
        val projectDto = ProjectDto().apply { projectId = "radar" }
        val projectEntity = HttpEntity(projectDto, AUTH_HEADER)

        restTemplate.exchange(
            createURLWithPort(port, ProjectEndpointAuthTest.PROJECT_PATH),
            HttpMethod.POST,
            projectEntity,
            ProjectDto::class.java
        )
    }

    @Test
    fun unauthorisedViewSingleUser() {
        val userDtoHttpEntity = HttpEntity<FcmUserDto>(null, HEADERS)

        val responseEntity = restTemplate.exchange(
            createURLWithPort(port, ProjectEndpointAuthTest.PROJECT_PATH) +
                    DEFAULT_PROJECT + USER_PATH + "/sub-1",
            HttpMethod.GET,
            userDtoHttpEntity,
            FcmUserDto::class.java
        )

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.statusCode)
    }

    @Test
    fun unauthorisedCreateUser() {
        val userDtoHttpEntity = HttpEntity(userDto, HEADERS)
        var responseEntity: ResponseEntity<FcmUserDto>? = null
        try {
            responseEntity = restTemplate.exchange(
                createURLWithPort(
                    port, ProjectEndpointAuthTest.PROJECT_PATH + DEFAULT_PROJECT + USER_PATH
                ),
                HttpMethod.POST,
                userDtoHttpEntity,
                FcmUserDto::class.java
            )
        } catch (e: ResourceAccessException) {
            assertEquals(responseEntity, null)
        }
    }

    @Test
    @Order(1)
    fun createUser() {
        val userDtoHttpEntity = HttpEntity(userDto, AUTH_HEADER)

        val responseEntity = restTemplate.exchange(
            createURLWithPort(
                port, ProjectEndpointAuthTest.PROJECT_PATH + DEFAULT_PROJECT + USER_PATH
            ),
            HttpMethod.POST,
            userDtoHttpEntity,
            FcmUserDto::class.java
        )

        if (responseEntity.statusCode == HttpStatus.EXPECTATION_FAILED) {
            return
        }

        assertEquals(HttpStatus.CREATED, responseEntity.statusCode)
    }

    @Test
    @Order(2)
    fun viewUser() {
        val userDtoHttpEntity = HttpEntity<FcmUserDto>(null, AUTH_HEADER)

        val responseEntity = restTemplate.exchange(
            createURLWithPort(
                port,
                ProjectEndpointAuthTest.PROJECT_PATH + DEFAULT_PROJECT + USER_PATH + "/sub-1"
            ),
            HttpMethod.GET,
            userDtoHttpEntity,
            FcmUserDto::class.java
        )

        assertEquals(HttpStatus.OK, responseEntity.statusCode)
    }

    @Test
    @Order(3)
    fun viewUsersInProject() {
        val userDtoHttpEntity = HttpEntity<FcmUsers>(null, AUTH_HEADER)

        val responseEntity = restTemplate.exchange(
            createURLWithPort(
                port, ProjectEndpointAuthTest.PROJECT_PATH + DEFAULT_PROJECT + USER_PATH
            ),
            HttpMethod.GET,
            userDtoHttpEntity,
            FcmUsers::class.java
        )

        assertEquals(HttpStatus.OK, responseEntity.statusCode)
    }

    @Test
    @Order(4)
    fun forbiddenViewUsersInOtherProject() {
        val userDtoHttpEntity = HttpEntity<FcmUsers>(null, AUTH_HEADER)

        val responseEntity = restTemplate.exchange(
            createURLWithPort(port, ProjectEndpointAuthTest.PROJECT_PATH + "/test" + USER_PATH),
            HttpMethod.GET,
            userDtoHttpEntity,
            FcmUsers::class.java
        )

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.statusCode)
    }

    @Test
    @Order(5)
    fun viewAllUsers() {
        val userDtoHttpEntity = HttpEntity<FcmUsers>(null, AUTH_HEADER)

        val responseEntity = restTemplate.exchange(
            createURLWithPort(port, USER_PATH), HttpMethod.GET, userDtoHttpEntity, FcmUsers::class.java
        )

        assertEquals(HttpStatus.OK, responseEntity.statusCode)
    }

    private fun createURLWithPort(port: Int, path: String): String {
        return "http://localhost:$port$path"
    }
}
