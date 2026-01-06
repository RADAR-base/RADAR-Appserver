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

package org.radarbase.appserver.microservices

import io.ktor.client.HttpClient
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
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.radarbase.appserver.microservices.commons.MpOAuthSupport
import org.radarbase.appserver.microservices.core.dto.ProjectDto

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ProjectEndpointAuthTest {

    @Test
    fun unAuthorizedCreatedProject(): Unit = runBlocking {
        val project = ProjectDto(projectId = "radar")
        val response = httpClient.post(PROJECT_PATH) {
            contentType(ContentType.Application.Json)
            setBody(project)
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun unAuthorizedViewProjects(): Unit = runBlocking {
        val response = httpClient.get(PROJECT_PATH) {
            accept(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun unAuthorizedViewSingleProject(): Unit = runBlocking {
        val response = httpClient.get("$PROJECT_PATH/radar") {
            accept(ContentType.Application.Json)
        }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun forbiddenViewProjects(): Unit = runBlocking {
        val response = httpClient.get(PROJECT_PATH) {
            accept(ContentType.Application.Json)
            header(HttpHeaders.Authorization, AUTH_HEADERS[HttpHeaders.Authorization])
        }
        // Only Admins Can View List Of All Projects
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    @Test
    @Order(1)
    fun createSingleProjectWithAuth() = runBlocking {
        val project = ProjectDto(projectId = "radar")

        val response = httpClient.post(PROJECT_PATH) {
            contentType(ContentType.Application.Json)
            setBody(project)
            header(HttpHeaders.Authorization, AUTH_HEADERS[HttpHeaders.Authorization])
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
            header(HttpHeaders.Authorization, AUTH_HEADERS[HttpHeaders.Authorization])
        }

        assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    @Order(3)
    fun getForbiddenProjectWithAuth() = runBlocking {
        val response = httpClient.get("$PROJECT_PATH/test") {
            accept(ContentType.Application.Json)
            header(HttpHeaders.Authorization, AUTH_HEADERS[HttpHeaders.Authorization])
        }
        assertEquals(HttpStatusCode.Forbidden, response.status)
    }

    companion object {
        private const val PROJECT_PATH = "projects"
        private lateinit var AUTH_HEADERS: Headers
        private lateinit var httpClient: HttpClient

        @BeforeAll
        @JvmStatic
        fun init() {
            httpClient = MpOAuthSupport.initHttpClient()

            val oAuthSupport = MpOAuthSupport().also {
                it.init()
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
