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

package org.radarbase.appserver.microservices.user.api

import jakarta.inject.Inject
import jakarta.validation.Valid
import jakarta.ws.rs.Consumes
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.DefaultValue
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
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUserDto
import org.radarbase.appserver.microservices.core.service.UserService
import org.radarbase.appserver.microservices.core.utils.Paths.PROJECTS_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.PROJECT_ID
import org.radarbase.appserver.microservices.core.utils.Paths.SUBJECT_ID
import org.radarbase.appserver.microservices.core.utils.Paths.USERS_PATH
import org.radarbase.appserver.microservices.user.config.UserServiceConfig
import org.radarbase.jersey.service.AsyncCoroutineService
import java.net.URI
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Suppress("UnresolvedRestParam")
@Path("/")
class UserResource @Inject constructor(
    private val userService: UserService,
    private val asyncService: AsyncCoroutineService,
    config: UserServiceConfig,
) {
    private val requestTimeout: Duration = config.server.requestTimeout.seconds

    @POST
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    fun addUserToProject(
        @Valid fcmUserDto: FcmUserDto,
        @Valid @PathParam("projectId") projectId: String,
        @QueryParam("forceFcmToken") @DefaultValue("false") forceFcmToken: Boolean,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            fcmUserDto.projectId = projectId
            if (forceFcmToken) userService.checkFcmTokenExistsAndReplace(fcmUserDto)
            userService.saveUserInProject(fcmUserDto).let {
                Response.created(URI("/projects/$projectId/users/?id=${it.id}")).entity(it).build()
            }
        }
    }

    @PUT
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    fun updateUserInProject(
        @Valid userDto: FcmUserDto,
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @QueryParam("forceFcmToken") @DefaultValue("false") forceFcmToken: Boolean,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            userDto.apply {
                this.subjectId = subjectId
                this.projectId = projectId
            }
            if (forceFcmToken) userService.checkFcmTokenExistsAndReplace(userDto)
            userService.updateUser(userDto).let {
                Response.ok(it).build()
            }
        }
    }

    @GET
    @Path(USERS_PATH)
    @Produces(APPLICATION_JSON)
    fun getAllRadarUsers(
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            userService.getAllRadarUsers().let {
                Response.ok(it).build()
            }
        }
    }

    @GET
    @Path("$USERS_PATH/user")
    @Produces(APPLICATION_JSON)
    fun getRadarUserUsingId(
        @QueryParam("id") id: Long,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            userService.getUserById(id).let { user ->
                Response.ok(user).build()
            }
        }
    }

    @GET
    @Path("$USERS_PATH/$SUBJECT_ID")
    @Produces(APPLICATION_JSON)
    fun getRadarUserUsingSubjectId(
        @PathParam("subjectId") subjectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            userService.getUserBySubjectId(subjectId).let { user ->
                Response.ok(user).build()
            }
        }
    }

    @GET
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH")
    @Produces(APPLICATION_JSON)
    fun getUsersUsingProjectId(
        @Valid @PathParam("projectId") projectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            userService.getUsersByProjectId(projectId).let { users ->
                Response.ok(users).build()
            }
        }
    }

    @GET
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID")
    @Produces(APPLICATION_JSON)
    fun getUsersUsingProjectIdAndSubjectId(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            userService.getUserByProjectIdAndSubjectId(projectId, subjectId).let {
                Response.ok(it).build()
            }
        }
    }

    @DELETE
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID")
    @Produces(APPLICATION_JSON)
    fun deleteUserUsingProjectIdAndSubjectId(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            userService.deleteUserByProjectIdAndSubjectId(projectId, subjectId)
            Response.ok().build()
        }
    }
}
