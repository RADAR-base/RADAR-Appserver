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

package org.radarbase.appserver.microservices.project.api

import jakarta.inject.Inject
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
import org.radarbase.appserver.microservices.core.dto.ProjectDto
import org.radarbase.appserver.microservices.project.service.ProjectService
import org.radarbase.appserver.microservices.core.utils.Paths.PROJECTS_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.PROJECT_ID
import org.radarbase.appserver.microservices.project.config.ProjectServiceConfig
import org.radarbase.jersey.service.AsyncCoroutineService
import java.net.URI
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Suppress("UnresolvedRestParam")
@Path("/")
class ProjectResource @Inject constructor(
    private val projectService: ProjectService,
    private val asyncService: AsyncCoroutineService,
    config: ProjectServiceConfig,
) {
    private val requestTimeout: Duration = config.server.requestTimeout.seconds

    @POST
    @Path(PROJECTS_PATH)
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    fun addProject(
        @Valid projectDto: ProjectDto,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            projectService.addProject(projectDto).let {
                Response
                    .created(URI("/projects/project?id=${it.id}"))
                    .entity(it)
                    .build()
            }
        }
    }

    @PUT
    @Path("$PROJECTS_PATH/$PROJECT_ID")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    fun updateProject(
        @Valid @PathParam("projectId") projectId: String,
        @Valid projectDto: ProjectDto,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            projectService.updateProject(projectDto).let {
                Response.ok(it).build()
            }
        }
    }

    @GET
    @Path(PROJECTS_PATH)
    @Produces(APPLICATION_JSON)
    fun getAllProjects(
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            projectService.getAllProjects()
        }.let {
            Response.ok(it).build()
        }
    }

    @GET
    @Path("$PROJECTS_PATH/project")
    @Produces(APPLICATION_JSON)
    fun getProjectUsingId(
        @QueryParam("id") id: Long,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            val project = projectService.getProjectById(id)
            Response.ok(project).build()
        }
    }

    @GET
    @Path("$PROJECTS_PATH/$PROJECT_ID")
    @Produces(APPLICATION_JSON)
    fun getProjectUsingProjectId(
        @Valid @PathParam("projectId") projectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            val project = projectService.getProjectByProjectId(projectId)
            Response.ok(project).build()
        }
    }
}
