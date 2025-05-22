package org.radarbase.appserver.jersey.resource

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
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.radarbase.appserver.jersey.dto.fcm.FcmUserDto
import org.radarbase.appserver.jersey.service.UserService
import org.radarbase.jersey.service.AsyncCoroutineService
import java.net.URI

@Path("")
class UserResource @Inject constructor(
    private val userService: UserService,
    private val asyncService: AsyncCoroutineService,
) {

    @POST
    @Path("/projects/{projectId}/users")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun addUserToProject(
        @Valid @PathParam("projectId") projectId: String,
        @Suspended asyncResponse: AsyncResponse,
        @Valid userDto: FcmUserDto,
        @QueryParam("forceFcmToken") forceFcmToken: Boolean = false,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        userDto.projectId = projectId
        if (forceFcmToken) userService.checkFcmTokenExistsAndReplace(userDto)
        val user = userService.saveUserInProject(userDto)
        Response.created(URI("/users/user?id=${user.id}")).entity(user).build()
    }

    @PUT
    @Path("/projects/{projectId}/users/{subjectId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    fun updateUserInProject(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @Valid userDto: FcmUserDto,
        @Suspended asyncResponse: AsyncResponse,
        @QueryParam("forceFcmToken") forceFcmToken: Boolean = false,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        userDto.apply {
            this.subjectId = subjectId
            this.projectId = projectId
        }
        if (forceFcmToken) userService.checkFcmTokenExistsAndReplace(userDto)
        val user = userService.updateUser(userDto)
        Response.ok(user).build()
    }

    @GET
    @Path("/users")
    @Produces(MediaType.APPLICATION_JSON)
    fun getAllUsers(
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        userService.getAllRadarUsers().let {
            Response.ok(it).build()
        }
    }

    @GET
    @Path("/users/user")
    @Produces(MediaType.APPLICATION_JSON)
    fun getUserById(
        @QueryParam("id") id: Long,
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        userService.getUserById(id).let {
            Response.ok(it).build()
        }
    }

    @GET
    @Path("/users/user/{subjectId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getUserBySubjectId(
        @PathParam("subjectId") subjectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        userService.getUserBySubjectId(subjectId).let {
            Response.ok(it).build()
        }
    }


    @GET
    @Path("/projects/{projectId}/users")
    @Produces(MediaType.APPLICATION_JSON)
    fun getUsersUsingProjectId(
        @Valid @PathParam("projectId") projectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        userService.getUsersByProjectId(projectId).let {
            Response.ok(it).build()
        }
    }

    @GET
    @Path("/projects/{projectId}/users/{subjectId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getUsersUsingProjectIdAndSubjectId(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) = asyncService.runAsCoroutine(asyncResponse) {
        userService.getUserByProjectIdAndSubjectId(projectId, subjectId).let {
            Response.ok(it).build()
        }
    }
}
