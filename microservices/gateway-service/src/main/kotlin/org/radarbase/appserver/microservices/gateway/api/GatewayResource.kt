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
import jakarta.ws.rs.core.MediaType.TEXT_PLAIN
import jakarta.ws.rs.core.Response
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.json.Json
import org.radarbase.appserver.microservices.contract.calls.GithubServiceContract
import org.radarbase.appserver.microservices.contract.calls.ProjectServiceContract
import org.radarbase.appserver.microservices.contract.calls.ProtocolServiceContract
import org.radarbase.appserver.microservices.contract.calls.UserServiceContract
import org.radarbase.appserver.microservices.core.dto.ProjectDto
import org.radarbase.appserver.microservices.core.dto.ProjectDtos
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUserDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUsers
import org.radarbase.appserver.microservices.core.utils.Paths.PROJECTS_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.PROJECT_ID
import org.radarbase.appserver.microservices.core.utils.Paths.PROTOCOLS_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.SUBJECT_ID
import org.radarbase.appserver.microservices.core.utils.Paths.USERS_PATH
import org.radarbase.appserver.microservices.core.utils.tokenForCurrentRequest
import org.radarbase.appserver.microservices.gateway.config.GatewayConfig
import org.radarbase.appserver.microservices.gateway.config.ServiceRoute
import org.radarbase.appserver.microservices.gateway.service.GatewayService
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
    private val gatewayService: GatewayService,
    config: GatewayConfig,
) {
    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    private val requestTimeout = config.server.requestTimeout.seconds
    private val projectServiceRoute: ServiceRoute = config.routes.first { it.name == "project" }
    private val protocolServiceRoute: ServiceRoute = config.routes.first { it.name == "protocol" }
    private val userServiceRoute: ServiceRoute = config.routes.first { it.name == "user" }
    private val githubServiceRoute: ServiceRoute = config.routes.first { it.name == "github" }

//-------------------------------------------------Project Service------------------------------------------------------

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
                    projectId,
                    projectDto,
                    projectServiceRoute.baseUrl,
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
            )

            if (proxyResponse.status !in 200..299) {
                return@runAsCoroutine handleProxyResponse(proxyResponse)
            }

            val proxyResponseBody: ByteArray? = proxyResponse.body
            val decoded: ProjectDtos? = try {
                if (proxyResponseBody != null) {
                    val bodyString = proxyResponseBody.decodeToString()
                    json.decodeFromString<ProjectDtos>(bodyString)
                } else null
            } catch (_: Exception) {
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
            )

            val proxyResponseBody: ByteArray? = proxyResponse.body
            val decodedProject: ProjectDto = try {
                if (proxyResponse.status in 200..299 && proxyResponseBody != null) {
                    val bodyString = proxyResponseBody.decodeToString()
                    json.decodeFromString<ProjectDto>(bodyString)
                } else {
                    return@runAsCoroutine handleProxyResponse(proxyResponse)
                }
            } catch (_: Exception) {
                return@runAsCoroutine handleProxyResponse(proxyResponse)
            }

            val token = tokenForCurrentRequest(asyncService, tokenProvider)
            authService.checkPermission(
                Permission.PROJECT_READ,
                EntityDetails(project = decodedProject.projectId),
                token,
            )

            Response.ok(decodedProject).build()
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
            )

            val projectResponseBody = proxyResponse.body
            val decodedProject: ProjectDto? = try {
                if (proxyResponse.status in 200..299 && projectResponseBody != null) {
                    (proxyResponse.body?.decodeToString() ?: "").let { bodyString ->
                        json.decodeFromString<ProjectDto>(bodyString)
                    }
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }

            if (decodedProject == null) {
                return@runAsCoroutine handleProxyResponse(proxyResponse)
            }

            val projectIdForAuth = decodedProject.projectId ?: projectId
            val token = tokenForCurrentRequest(asyncService, tokenProvider)
            authService.checkPermission(
                Permission.SUBJECT_READ,
                EntityDetails(project = projectIdForAuth, subject = token.subject),
                token,
            )

            Response.ok(decodedProject).build()
        }
    }

//-------------------------------------------Github Service-------------------------------------------------------------

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
                return@runAsCoroutine Response.status(Response.Status.BAD_REQUEST).entity("invalid or disallowed url")
                    .build()
            }

            val token = tokenForCurrentRequest(asyncService, tokenProvider)
            authService.checkPermission(
                Permission.SUBJECT_READ,
                EntityDetails(project = null, subject = token.subject),
                token,
            )

            GithubServiceContract.getGithubContent(githubServiceRoute.baseUrl, url).let(::handleProxyResponse)
        }
    }

//-------------------------------------------Protocol Service-----------------------------------------------------------

    @GET
    @Path(PROTOCOLS_PATH)
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ)
    fun getProtocols(
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            val token = tokenForCurrentRequest(asyncService, tokenProvider)
            authService.checkPermission(
                Permission.PROJECT_READ,
                EntityDetails(project = null, subject = token.subject),
                token,
            )

            val proxyResponse = ProtocolServiceContract.getProtocols(
                protocolServiceRoute.baseUrl,
            )

            handleProxyResponse(proxyResponse)
        }
    }

    @GET
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$PROTOCOLS_PATH")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ, projectPathParam = "projectId", userPathParam = "subjectId")
    fun getProtocolsUsingProjectIdAndSubjectId(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            val token = tokenForCurrentRequest(asyncService, tokenProvider)
            authService.checkPermission(
                Permission.PROJECT_READ,
                EntityDetails(project = projectId, subject = token.subject),
                token,
            )

            val proxyResponse = ProtocolServiceContract.getProtocolForSubject(
                projectId,
                subjectId,
                protocolServiceRoute.baseUrl,
            )

            handleProxyResponse(proxyResponse)
        }
    }

    @GET
    @Path("$PROJECTS_PATH/$PROJECT_ID/$PROTOCOLS_PATH")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ, projectPathParam = "projectId")
    fun getProtocolsUsingProjectId(
        @Valid @PathParam("projectId") projectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            val token = tokenForCurrentRequest(asyncService, tokenProvider)
            authService.checkPermission(
                Permission.PROJECT_READ,
                EntityDetails(project = projectId, subject = token.subject),
                token,
            )

            val proxyResponse = ProtocolServiceContract.getProtocolsForProject(
                projectId,
                protocolServiceRoute.baseUrl,
            )

            handleProxyResponse(proxyResponse)
        }
    }


//---------------------------------------------User Service-------------------------------------------------------------

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
            fcmUserDto.projectId = projectId
            val token = tokenForCurrentRequest(asyncService, tokenProvider)
            authService.checkPermission(
                Permission.SUBJECT_UPDATE,
                EntityDetails(project = projectId, subject = token.subject),
                token,
            )

            val response = UserServiceContract.addUser(
                fcmUserDto,
                projectId,
                forceFcmToken,
                userServiceRoute.baseUrl,
            )
            handleProxyResponse(response)
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

            val updatedUserResponse = UserServiceContract.updateUser(
                userDto,
                projectId,
                subjectId,
                forceFcmToken,
                userServiceRoute.baseUrl,
            )
            handleProxyResponse(updatedUserResponse)
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
            val usersProxyResponse = UserServiceContract.getAllUsers(userServiceRoute.baseUrl)
            val proxyResponseBody = usersProxyResponse.body

            val decodedUsers: FcmUsers = try {
                if (proxyResponseBody != null && usersProxyResponse.status in 200..299) {
                    proxyResponseBody.decodeToString().let {
                        json.decodeFromString<FcmUsers>(it)
                    }
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            } ?: run {
                return@runAsCoroutine handleProxyResponse(usersProxyResponse)
            }

            decodedUsers.users.asFlow().filter {
                authService.hasPermission(
                    Permission.SUBJECT_READ,
                    EntityDetails(project = it.projectId, subject = it.subjectId),
                    tokenForCurrentRequest(asyncService, tokenProvider),
                )
            }.toList().toMutableList().let {
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
            val userProxyResponse = UserServiceContract.getUserUsingId(id, userServiceRoute.baseUrl)
            val proxyResponseBody = userProxyResponse.body
            val user: FcmUserDto = try {
                if (proxyResponseBody != null && userProxyResponse.status in 200..299) {
                    proxyResponseBody.decodeToString().let {
                        json.decodeFromString<FcmUserDto>(it)
                    }
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            } ?: run {
                return@runAsCoroutine handleProxyResponse(userProxyResponse)
            }
            gatewayService.fcmUserDtoAsResponseIfAuthorized(user)
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
            val proxyUserResponse = UserServiceContract.getUserUsingSubjectId(
                subjectId,
                userServiceRoute.baseUrl,
            )

            gatewayService.run {
                (dtoFromProxyResponse<FcmUserDto>(json, proxyUserResponse) ?: run {
                    return@runAsCoroutine handleProxyResponse(proxyUserResponse)
                }).let {
                    gatewayService.fcmUserDtoAsResponseIfAuthorized(it)
                }
            }
        }
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
            val users = UserServiceContract.getUsersUsingProjectId(projectId, userServiceRoute.baseUrl).let {
                gatewayService.run {
                    (dtoFromProxyResponse<FcmUsers>(json, it) ?: run {
                        return@runAsCoroutine handleProxyResponse(it)
                    })
                }
            }
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
            val user =
                UserServiceContract.getUserUsingProjectIdAndSubjectId(projectId, subjectId, userServiceRoute.baseUrl)
                    .let {
                        gatewayService.run {
                            (dtoFromProxyResponse<FcmUserDto>(json, it) ?: run {
                                return@runAsCoroutine handleProxyResponse(it)
                            })
                        }
                    }

            Response.ok(user).build()
        }
    }

    @DELETE
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
    fun deleteUserUsingProjectIdAndSubjectId(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            handleProxyResponse(
                UserServiceContract.deleteUserUsingProjectIdAndSubjectId(
                    projectId,
                    subjectId,
                    userServiceRoute.baseUrl,
                ),
            )
        }
    }

}


