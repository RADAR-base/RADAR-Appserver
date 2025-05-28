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
import jakarta.validation.Valid
import jakarta.ws.rs.*
import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.container.Suspended
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.radarbase.appserver.jersey.dto.ProjectDto
import org.radarbase.appserver.jersey.service.ProjectService
import org.radarbase.jersey.service.AsyncCoroutineService
import org.slf4j.LoggerFactory
import java.net.URI

@Path("")
class ProjectResource @Inject constructor(
    private val projectService: ProjectService,
    private val asyncService: AsyncCoroutineService,
) {

    @GET
    @Path("/projects")
    @Produces(MediaType.APPLICATION_JSON)
    fun getAllProjects(
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse) {
            projectService.getAllProjects().run {
                Response.ok(this).build()
            }
        }
    }

    @POST
    @Path("/projects")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun addProject(
        project: ProjectDto,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse) {
            projectService.addProject(project).run {
                Response.created(URI("/projects")).entity(this).build()
            }
        }
    }

    @PUT
    @Path("/projects/{projectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateProject(
        @Valid @PathParam("projectId") projectId: String,
        @Valid project: ProjectDto,
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        projectService.updateProject(project).let(Response::ok).build()
    }

    @GET
    @Path("/projects/project")
    @Produces(MediaType.APPLICATION_JSON)
    fun getProjectUsingId(
        @QueryParam("id") id: Long,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse) {
            projectService.getProjectById(id).let {
                Response.ok(it).build()
            }
        }
    }

    @GET
    @Path("/projects/{projectId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getProjectByProjectId(
        @Valid @PathParam("projectId") projectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse) {
            projectService.getProjectByProjectId(projectId).let {
                Response.ok(it).build()
            }
        }
    }
    
    companion object {
        private val logger = LoggerFactory.getLogger(ProjectResource::class.java)
    }
}
