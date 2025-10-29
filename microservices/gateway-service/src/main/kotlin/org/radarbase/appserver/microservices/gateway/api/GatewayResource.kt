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

package org.radarbase.appserver.microservices.gateway.api

import jakarta.inject.Inject
import jakarta.inject.Provider
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.container.Suspended
import jakarta.ws.rs.core.MediaType.APPLICATION_JSON
import jakarta.ws.rs.core.MediaType.TEXT_PLAIN
import jakarta.ws.rs.core.Response
import kotlinx.serialization.json.Json
import org.radarbase.appserver.microservices.contract.calls.GithubServiceContract
import org.radarbase.appserver.microservices.contract.calls.ProjectServiceContract
import org.radarbase.appserver.microservices.core.dto.ProjectDto
import org.radarbase.appserver.microservices.core.dto.ProjectDtos
import org.radarbase.appserver.microservices.core.utils.Paths.PROJECTS_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.PROJECT_ID
import org.radarbase.appserver.microservices.core.utils.tokenForCurrentRequest
import org.radarbase.appserver.microservices.gateway.config.GatewayConfig
import org.radarbase.appserver.microservices.gateway.config.ServiceRoute
import org.radarbase.appserver.microservices.gateway.utils.GithubHostVerifier.isAllowedGithubUrl
import org.radarbase.appserver.microservices.gateway.utils.Utils.handleProxyResponse
import org.radarbase.auth.authorization.EntityDetails
import org.radarbase.auth.authorization.Permission
import org.radarbase.auth.token.RadarToken
import org.radarbase.jersey.auth.AuthService
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarbase.jersey.service.AsyncCoroutineService
import kotlin.time.Duration.Companion.seconds

@Suppress("UnresolvedRestParam")
@Path("/")
class GatewayResource @Inject constructor(
    private val asyncService: AsyncCoroutineService,
    private val authService: AuthService,
    private val tokenProvider: Provider<RadarToken>,
    config: GatewayConfig,
) {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    private val requestTimeout = config.server.requestTimeout.seconds
    private val projectServiceRoute: ServiceRoute = config.routes.first { it.name == "project" }
    private val prefix = config.externalPrefix

    @POST
    @Path(PROJECTS_PATH)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_READ)
    fun addProject(
        @Valid projectDto: ProjectDto,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            val token = tokenForCurrentRequest(asyncService, tokenProvider)
            authService.checkPermission(
                Permission.SUBJECT_READ,
                EntityDetails(project = projectDto.projectId, subject = token.subject),
                token,
            )

            handleProxyResponse(
                ProjectServiceContract.addProject(
                    projectDto,
                    projectServiceRoute.baseUrl,
                    projectServiceRoute.path,
                    prefix,
                ),
            )
        }
    }

    @PUT
    @Path("$PROJECTS_PATH/$PROJECT_ID")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE)
    fun updateProject(
        @Valid @PathParam("projectId") projectId: String,
        @Valid projectDto: ProjectDto,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            val token = tokenForCurrentRequest(asyncService, tokenProvider)
            authService.checkPermission(
                Permission.SUBJECT_UPDATE,
                EntityDetails(project = projectId, subject = token.subject),
                token,
            )

            handleProxyResponse(
                ProjectServiceContract.updateProject(
                    projectDto,
                    projectServiceRoute.baseUrl,
                    projectServiceRoute.path,
                    prefix,
                ),
            )
        }
    }

    @GET
    @Path(PROJECTS_PATH)
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ)
    fun getAllProjects(
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {

            val proxyResponse = ProjectServiceContract.getAllProjects(
                projectServiceRoute.baseUrl,
                projectServiceRoute.path,
                prefix,
            )

            if (proxyResponse.status !in 200..299) {
                return@runAsCoroutine handleProxyResponse(proxyResponse)
            }

            val decoded: ProjectDtos? = try {
                if (proxyResponse.body != null) {
                    val bodyString = proxyResponse.body?.decodeToString() ?: ""
                    json.decodeFromString<ProjectDtos>(bodyString)
                } else null
            } catch (e: Exception) {
                null
            }

            if (decoded == null) {
                return@runAsCoroutine handleProxyResponse(proxyResponse)
            }

            val token = tokenForCurrentRequest(asyncService, tokenProvider)
            val filtered: MutableList<ProjectDto> = decoded.projects
                .filter { project ->
                    authService.hasPermission(
                        Permission.PROJECT_READ,
                        EntityDetails(project = project.projectId),
                        token,
                    )
                }
                .toMutableList()

            Response.ok(ProjectDtos(filtered)).build()
        }
    }

    @GET
    @Path("$PROJECTS_PATH/project")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ)
    fun getProjectUsingId(
        @QueryParam("id") id: Long,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            val proxyResponse = ProjectServiceContract.getProjectUsingId(
                id,
                projectServiceRoute.baseUrl,
                projectServiceRoute.path,
                prefix,
            )

            val decodedProject: ProjectDto? = try {
                if (proxyResponse.status in 200..299 && proxyResponse.body != null) {
                    val bodyString = proxyResponse.body?.decodeToString() ?: ""
                    json.decodeFromString<ProjectDto>(bodyString)
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }

            val token = tokenForCurrentRequest(asyncService, tokenProvider)
            authService.checkPermission(
                Permission.PROJECT_READ,
                EntityDetails(project = decodedProject?.projectId),
                token,
            )

            handleProxyResponse(proxyResponse)
        }
    }

    @GET
    @Path("$PROJECTS_PATH/$PROJECT_ID")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_READ)
    fun getProjectUsingProjectId(
        @Valid @PathParam("projectId") projectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            val proxyResponse = ProjectServiceContract.getProjectUsingProjectId(
                projectId,
                projectServiceRoute.baseUrl,
                projectServiceRoute.path,
                prefix,
            )

            val decodedProject: ProjectDto? = try {
                if (proxyResponse.status in 200..299 && proxyResponse.body != null) {
                    val bodyString = proxyResponse.body?.decodeToString() ?: ""
                    json.decodeFromString<ProjectDto>(bodyString)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }

            val projectIdForAuth = decodedProject?.projectId ?: projectId

            val token = tokenForCurrentRequest(asyncService, tokenProvider)
            authService.checkPermission(
                Permission.SUBJECT_READ,
                EntityDetails(project = projectIdForAuth, subject = token.subject),
                token,
            )

            handleProxyResponse(proxyResponse)
        }
    }

    @GET
    @Path("/github")
    @Produces(TEXT_PLAIN)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_READ)
    fun getGithubContent(
        @QueryParam("url") url: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            if (!isAllowedGithubUrl(url)) {
                return@runAsCoroutine Response.status(Response.Status.BAD_REQUEST)
                    .entity("invalid or disallowed url")
                    .build()
            }

            val token = tokenForCurrentRequest(asyncService, tokenProvider)
            authService.checkPermission(
                Permission.SUBJECT_READ,
                EntityDetails(project = null, subject = token.subject),
                token,
            )

            GithubServiceContract.getGithubContent(url).let(::handleProxyResponse)
        }
    }

}


