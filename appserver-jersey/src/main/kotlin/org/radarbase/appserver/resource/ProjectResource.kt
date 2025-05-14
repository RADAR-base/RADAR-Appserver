package org.radarbase.appserver.resource

import jakarta.inject.Inject
import jakarta.ws.rs.*
import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.container.Suspended
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.radarbase.appserver.dto.ProjectDto
import org.radarbase.appserver.service.ProjectService
import org.radarbase.jersey.service.AsyncCoroutineService
import org.slf4j.LoggerFactory
import java.net.URI

@Path("/projects")
class ProjectResource @Inject constructor(
    private val projectService: ProjectService,
    private val asyncService: AsyncCoroutineService,
) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
//    @Authenticated
//    @NeedsPermission(Permission.PROJECT_READ)
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

    companion object {
        private val logger = LoggerFactory.getLogger(ProjectResource::class.java)
    }
}
