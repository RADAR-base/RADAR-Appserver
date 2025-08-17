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
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import org.radarbase.appserver.jersey.config.AppserverConfig
import org.radarbase.appserver.jersey.dto.fcm.FcmUserDto
import org.radarbase.appserver.jersey.dto.fcm.FcmUsers
import org.radarbase.appserver.jersey.service.UserService
import org.radarbase.appserver.jersey.utils.Paths.PROJECTS_PATH
import org.radarbase.appserver.jersey.utils.Paths.PROJECT_ID
import org.radarbase.appserver.jersey.utils.Paths.SUBJECT_ID
import org.radarbase.appserver.jersey.utils.Paths.USERS_PATH
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
class UserResource @Inject constructor(
    private val userService: UserService,
    private val asyncService: AsyncCoroutineService,
    private val authService: AuthService,
    private val tokenProvider: Provider<RadarToken>,
    config: AppserverConfig,
) {
    private val requestTimeout: Duration = config.server.requestTimeout.seconds

    @POST
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH")
    @Consumes(APPLICATION_JSON)
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE)
    fun addUserToProject(
        @Valid fcmUserDto: FcmUserDto,
        @Valid @PathParam("projectId") projectId: String,
        @QueryParam("forceFcmToken") @DefaultValue("false") forceFcmToken: Boolean,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            val token = tokenForCurrentRequest(asyncService, tokenProvider)
            authService.checkPermission(
                Permission.SUBJECT_UPDATE,
                EntityDetails(project = projectId, subject = token.subject),
                token,
            )
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
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
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
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_READ)
    fun getAllRadarUsers(
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            userService.getAllRadarUsers()
                .users
                .asFlow()
                .filter {
                    authService.hasPermission(
                        Permission.SUBJECT_READ,
                        EntityDetails(project = it.projectId, subject = it.subjectId),
                        tokenForCurrentRequest(asyncService, tokenProvider),
                    )
                }
                .toList()
                .toMutableList().let {
                    FcmUsers(it)
                }.let {
                    Response.ok(it).build()
                }

        }
    }

    @GET
    @Path("$USERS_PATH/user")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_READ)
    fun getRadarUserUsingId(
        @QueryParam("id") id: Long,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            val user: FcmUserDto = userService.getUserById(id)
            fcmUserDtoAsResponseIfAuthorized(user)
        }
    }

    @GET
    @Path("$USERS_PATH/$SUBJECT_ID")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_READ)
    fun getRadarUserUsingSubjectId(
        @PathParam("subjectId") subjectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            val user: FcmUserDto = userService.getUserBySubjectId(subjectId)
            fcmUserDtoAsResponseIfAuthorized(user)
        }
    }

    suspend fun fcmUserDtoAsResponseIfAuthorized(
        fcmUserDto: FcmUserDto,
    ): Response {
        authService.checkPermission(
            Permission.SUBJECT_READ,
            EntityDetails(project = fcmUserDto.projectId, subject = fcmUserDto.subjectId),
            tokenForCurrentRequest(asyncService, tokenProvider),
        )

        return Response.ok(fcmUserDto).build()
    }

    @GET
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_READ)
    fun getUsersUsingProjectId(
        @Valid @PathParam("projectId") projectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            val users = userService.getUsersByProjectId(projectId)
            val token = tokenForCurrentRequest(asyncService, tokenProvider)
            authService.checkPermission(
                Permission.SUBJECT_READ,
                EntityDetails(project = projectId, subject = token.subject),
                token,
            )
            Response.ok(users).build()
        }
    }

    @GET
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_READ, projectPathParam = "projectId", userPathParam = "subjectId")
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
}

