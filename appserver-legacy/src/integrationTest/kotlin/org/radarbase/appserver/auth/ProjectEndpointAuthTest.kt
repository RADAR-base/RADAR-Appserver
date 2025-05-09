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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.radarbase.appserver.auth.common.MPOAuthHelper
import org.radarbase.appserver.auth.common.OAuthHelper
import org.radarbase.appserver.dto.ProjectDto
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.*
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.client.ResourceAccessException

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ProjectEndpointAuthTest {
    @Transient
    private val restTemplate = TestRestTemplate()

    @LocalServerPort
    @Transient
    private var port = 0

    @Test
    fun unauthorisedCreateProject() {
        val projectDto = ProjectDto(projectId = "radar")
        val projectEntity = HttpEntity<ProjectDto?>(projectDto, HEADERS)

        var responseEntity: ResponseEntity<ProjectDto>? = null
        try {
            responseEntity =
                restTemplate.exchange<ProjectDto?>(
                    createURLWithPort(port, PROJECT_PATH),
                    HttpMethod.POST,
                    projectEntity,
                    ProjectDto::class.java,
                )

            assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.statusCode)
        } catch (_: ResourceAccessException) {
            assertEquals(responseEntity, null)
        }
    }

    @Test
    fun unauthorisedViewProjects() {
        val projectEntity = HttpEntity<Nothing>(HEADERS)

        val responseEntity: ResponseEntity<String> =
            restTemplate.exchange<String>(
                createURLWithPort(port, PROJECT_PATH),
                HttpMethod.GET,
                projectEntity,
                String::class.java,
            )
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.statusCode)
    }

    @Test
    fun unauthorisedViewSingleProject() {
        val projectEntity = HttpEntity<Nothing>(HEADERS)

        val responseEntity: ResponseEntity<ProjectDto> =
            restTemplate.exchange<ProjectDto?>(
                createURLWithPort(port, "/projects/radar"),
                HttpMethod.GET,
                projectEntity,
                ProjectDto::class.java,
            )
        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.statusCode)
    }

    @Test
    fun forbiddenViewProjects() {
        val projectEntity = HttpEntity<Nothing>(AUTH_HEADER)

        val responseEntity: ResponseEntity<String> =
            restTemplate.exchange<String>(
                createURLWithPort(port, PROJECT_PATH),
                HttpMethod.GET,
                projectEntity,
                String::class.java,
            )

        // Only Admins can view the list of all projects
        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode())
    }

    @Test
    @Order(1)
    fun createSingleProjectWithAuth() {
        val projectDto = ProjectDto(projectId = "radar")
        val projectEntity = HttpEntity<ProjectDto>(projectDto, AUTH_HEADER)

        val responseEntity: ResponseEntity<ProjectDto> =
            restTemplate.exchange<ProjectDto>(
                createURLWithPort(port, PROJECT_PATH),
                HttpMethod.POST,
                projectEntity,
                ProjectDto::class.java,
            )

        if (responseEntity.statusCode == HttpStatus.EXPECTATION_FAILED) {
            // The auth was successful but expectation failed if the project already exits.
            // Since this is just an auth test we can return.
            return
        }
        assertEquals(HttpStatus.CREATED, responseEntity.statusCode)
    }

    @Order(2)
    @Test
    fun getSingleProjectWithAuth() {
        val projectEntity = HttpEntity<Nothing>(AUTH_HEADER)

        val responseEntity: ResponseEntity<ProjectDto> =
            restTemplate.exchange<ProjectDto>(
                createURLWithPort(port, "/projects/radar"),
                HttpMethod.GET,
                projectEntity,
                ProjectDto::class.java,
            )

        assertEquals(
            HttpStatus.OK,
            responseEntity.statusCode,
        )
    }

    @Order(3)
    @Test
    fun getForbiddenProjectWithAuth() {
        val projectEntity =
            HttpEntity<ProjectDto?>(null, AUTH_HEADER)

        val responseEntity: ResponseEntity<ProjectDto?> =
            restTemplate.exchange<ProjectDto?>(
                createURLWithPort(port, "/projects/test"),
                HttpMethod.GET,
                projectEntity,
                ProjectDto::class.java,
            )

        // Access denied as the user has only access to the project that it is part of.
        assertEquals(
            HttpStatus.FORBIDDEN,
            responseEntity.statusCode,
        )
    }

    companion object {
        const val PROJECT_PATH: String = "/projects"
        private val HEADERS = HttpHeaders()
        private lateinit var AUTH_HEADER: HttpHeaders

        @JvmStatic
        @BeforeAll
        fun init() {
            val oAuthHelper: OAuthHelper = MPOAuthHelper()
            AUTH_HEADER = HttpHeaders()
            AUTH_HEADER.setBearerAuth(oAuthHelper.getAccessToken())
        }

        fun createURLWithPort(port: Int, uri: String): String {
            return "http://localhost:$port$uri"
        }
    }
}
