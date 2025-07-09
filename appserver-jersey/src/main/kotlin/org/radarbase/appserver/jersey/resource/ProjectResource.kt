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

package org.radarbase.appserver.jersey.resource

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
import jakarta.ws.rs.core.Response
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import org.radarbase.appserver.jersey.config.AppserverConfig
import org.radarbase.appserver.jersey.dto.ProjectDto
import org.radarbase.appserver.jersey.dto.ProjectDtos
import org.radarbase.appserver.jersey.service.ProjectService
import org.radarbase.appserver.jersey.utils.Paths.PROJECTS_PATH
import org.radarbase.appserver.jersey.utils.Paths.PROJECT_ID
import org.radarbase.appserver.jersey.utils.tokenForCurrentRequest
import org.radarbase.auth.authorization.EntityDetails
import org.radarbase.auth.authorization.Permission
import org.radarbase.auth.token.RadarToken
import org.radarbase.jersey.auth.AuthService
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarbase.jersey.service.AsyncCoroutineService
import java.net.URI
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Suppress("UnresolvedRestParam")
@Path("/")
class ProjectResource @Inject constructor(
    private val projectService: ProjectService,
    private val asyncService: AsyncCoroutineService,
    private val authService: AuthService,
    private val tokenProvider: Provider<RadarToken>,
    config: AppserverConfig,
) {
    private val requestTimeout: Duration = config.server.requestTimeout.seconds
    private val mpSecurityEnabled: Boolean = config.mp.security.enabled


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
            if (mpSecurityEnabled) {
                val token = tokenForCurrentRequest(asyncService, tokenProvider)
                authService.checkPermission(
                    Permission.SUBJECT_READ,
                    EntityDetails(project = projectDto.projectId, subject = token.subject),
                    token,
                )
                projectService.addProject(projectDto).let {
                    Response
                        .created(URI("/projects/project?id=${projectDto.id}"))
                        .entity(it)
                        .build()
                }
            } else {
                projectService.addProject(projectDto).let {
                    Response
                        .created(URI("/projects/project?id=${projectDto.id}"))
                        .entity(it)
                        .build()
                }
            }
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
            projectService.updateProject(projectDto).let {
                Response.ok(it).build()
            }
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
            projectService.getAllProjects().projects
                .asFlow()
                .filter {
                    authService.hasPermission(
                        Permission.PROJECT_READ,
                        EntityDetails(project = it.projectId),
                        tokenForCurrentRequest(asyncService, tokenProvider)
                    )
                }.toList()
                .toMutableList()
                .let {
                    ProjectDtos(it.toMutableList())
                }.let {
                    Response.ok(it).build()
                }
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
            val project = projectService.getProjectById(id)
            val token = tokenForCurrentRequest(asyncService, tokenProvider)
            authService.checkPermission(
                Permission.PROJECT_READ,
                EntityDetails(project = project.projectId),
                token
            )
                .let {
                    Response.ok(it).build()
                }
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
            val project = projectService.getProjectByProjectId(projectId)
            val token = tokenForCurrentRequest(asyncService, tokenProvider)
            authService.checkPermission(
                Permission.SUBJECT_READ,
                EntityDetails(project = project.projectId, subject = token.subject),
                token
            )
            Response.ok(project).build()
        }
    }
}
