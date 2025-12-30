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
import org.radarbase.appserver.microservices.contract.calls.DataMessageServiceContract
import org.radarbase.appserver.microservices.contract.calls.GithubServiceContract
import org.radarbase.appserver.microservices.contract.calls.NotificationServiceContract
import org.radarbase.appserver.microservices.contract.calls.NotificationStateEventServiceContract
import org.radarbase.appserver.microservices.contract.calls.ProjectServiceContract
import org.radarbase.appserver.microservices.contract.calls.ProtocolServiceContract
import org.radarbase.appserver.microservices.contract.calls.QuestionnaireScheduleContract
import org.radarbase.appserver.microservices.contract.calls.TaskStateEventServiceContract
import org.radarbase.appserver.microservices.contract.calls.UserServiceContract
import org.radarbase.appserver.microservices.contract.exception.InvalidUpstreamResponseException
import org.radarbase.appserver.microservices.core.dto.NotificationStateEventDto
import org.radarbase.appserver.microservices.core.dto.ProjectDto
import org.radarbase.appserver.microservices.core.dto.ProjectDtos
import org.radarbase.appserver.microservices.core.dto.TaskStateEventDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmDataMessageDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmDataMessages
import org.radarbase.appserver.microservices.core.dto.fcm.FcmNotificationDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmNotifications
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUserDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUsers
import org.radarbase.appserver.microservices.core.dto.protocol.Assessment
import org.radarbase.appserver.microservices.core.utils.Paths.ALL_KEYWORD
import org.radarbase.appserver.microservices.core.utils.Paths.MESSAGING_DATA_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.MESSAGING_NOTIFICATION_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.NOTIFICATION_ID
import org.radarbase.appserver.microservices.core.utils.Paths.NOTIFICATION_STATE_EVENTS_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.PROJECTS_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.PROJECT_ID
import org.radarbase.appserver.microservices.core.utils.Paths.PROTOCOLS_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.QUESTIONNAIRE_SCHEDULE
import org.radarbase.appserver.microservices.core.utils.Paths.QUESTIONNAIRE_STATE_EVENTS_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.SUBJECT_ID
import org.radarbase.appserver.microservices.core.utils.Paths.TASKS_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.TASK_ID
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
import java.net.URI
import java.time.LocalDateTime
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
    private val taskServiceRoute: ServiceRoute = config.routes.first { it.name == "task" }
    private val cloudMessagingServiceRoute: ServiceRoute = config.routes.first { it.name == "messaging" }

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

            if (decoded == null) throw InvalidUpstreamResponseException()

            val token = tokenForCurrentRequest(asyncService, tokenProvider)
            val filtered: MutableList<ProjectDto> = decoded.projects.filter { project ->
                authService.hasPermission(
                    Permission.PROJECT_READ,
                    EntityDetails(project = project.projectId),
                    token,
                )
            }.toMutableList()

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

            if (proxyResponse.status !in 200..299) {
                return@runAsCoroutine handleProxyResponse(proxyResponse)
            }

            val proxyResponseBody: ByteArray? = proxyResponse.body
            val decodedProject: ProjectDto? = try {
                if (proxyResponseBody != null) {
                    val bodyString = proxyResponseBody.decodeToString()
                    json.decodeFromString<ProjectDto>(bodyString)
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }

            if (decodedProject == null) throw InvalidUpstreamResponseException()

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
            tokenForCurrentRequest(asyncService, tokenProvider).also {
                authService.checkPermission(
                    Permission.SUBJECT_READ,
                    EntityDetails(project = projectId, subject = it.subject),
                    it,
                )
            }

            val proxyResponse = ProjectServiceContract.getProjectUsingProjectId(
                projectId,
                projectServiceRoute.baseUrl,
            )

            if (proxyResponse.status !in 200..299) {
                return@runAsCoroutine handleProxyResponse(proxyResponse)
            }

            val projectResponseBody = proxyResponse.body
            val decodedProject: ProjectDto? = try {
                if (projectResponseBody != null) {
                    (proxyResponse.body?.decodeToString() ?: "").let { bodyString ->
                        json.decodeFromString<ProjectDto>(bodyString)
                    }
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }

            if (decodedProject == null) throw InvalidUpstreamResponseException()
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
            if (usersProxyResponse.status !in 200..299) {
                return@runAsCoroutine handleProxyResponse(usersProxyResponse)
            }

            val proxyResponseBody = usersProxyResponse.body

            val decodedUsers: FcmUsers = try {
                proxyResponseBody?.decodeToString()?.let {
                    json.decodeFromString<FcmUsers>(it)
                }
            } catch (_: Exception) {
                null
            } ?: throw InvalidUpstreamResponseException()


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

            if (userProxyResponse.status !in 200..299) {
                return@runAsCoroutine handleProxyResponse(userProxyResponse)
            }

            val proxyResponseBody = userProxyResponse.body
            val user: FcmUserDto = try {
                proxyResponseBody?.decodeToString()?.let {
                    json.decodeFromString<FcmUserDto>(it)
                }
            } catch (_: Exception) {
                null
            } ?: throw InvalidUpstreamResponseException()
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

            if (proxyUserResponse.status !in 200..299) {
                return@runAsCoroutine handleProxyResponse(proxyUserResponse)
            }

            gatewayService.run {
                (dtoFromProxyResponse<FcmUserDto>(json, proxyUserResponse)
                    ?: throw InvalidUpstreamResponseException()).let {
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
            val token = tokenForCurrentRequest(asyncService, tokenProvider)
            authService.checkPermission(
                Permission.SUBJECT_READ,
                EntityDetails(project = projectId, subject = token.subject),
                token,
            )

            val users = UserServiceContract.getUsersUsingProjectId(projectId, userServiceRoute.baseUrl).let {
                if (it.status !in 200..299) {
                    return@runAsCoroutine handleProxyResponse(it)
                }

                gatewayService.run {
                    (dtoFromProxyResponse<FcmUsers>(json, it) ?: throw InvalidUpstreamResponseException())
                }
            }
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

                        if (it.status !in 200..299) {
                            return@runAsCoroutine handleProxyResponse(it)
                        }

                        gatewayService.run {
                            (dtoFromProxyResponse<FcmUserDto>(json, it) ?: throw InvalidUpstreamResponseException())
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

//----------------------------------------Task State Event Service------------------------------------------------------

    @GET
    @Path("/$QUESTIONNAIRE_SCHEDULE/$TASK_ID/$QUESTIONNAIRE_STATE_EVENTS_PATH")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_READ)
    fun getTaskStateEventsByTaskId(
        @PathParam("taskId") taskId: Long,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            handleProxyResponse(
                TaskStateEventServiceContract.getTaskStateEventsByTaskId(taskId, taskServiceRoute.baseUrl),
            )
        }
    }

    @GET
    @Path("/$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$QUESTIONNAIRE_SCHEDULE/$TASK_ID/$QUESTIONNAIRE_STATE_EVENTS_PATH")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_READ, projectPathParam = "projectId", userPathParam = "subjectId")
    fun getTaskStateEvents(
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") subjectId: String,
        @PathParam("taskId") taskId: Long,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            TaskStateEventServiceContract.getTaskStateEvents(
                projectId,
                subjectId,
                taskId,
                taskServiceRoute.baseUrl,
            ).let(::handleProxyResponse)
        }
    }

    @POST
    @Path("/$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$QUESTIONNAIRE_SCHEDULE/$TASK_ID/$QUESTIONNAIRE_STATE_EVENTS_PATH")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
    fun postTaskStateEvents(
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") subjectId: String,
        @PathParam("taskId") taskId: Long,
        taskStateEventDto: TaskStateEventDto,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            TaskStateEventServiceContract.postTaskStateEvent(
                projectId,
                subjectId,
                taskId,
                taskStateEventDto,
                taskServiceRoute.baseUrl,
            ).let(::handleProxyResponse)
        }
    }

    @POST
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$QUESTIONNAIRE_SCHEDULE")
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
    fun generateScheduleUsingProjectIdAndSubjectId(
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") subjectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            QuestionnaireScheduleContract.generateScheduleUsingProjectIdAndSubjectId(
                projectId,
                subjectId,
                taskServiceRoute.baseUrl,
            ).let(::handleProxyResponse)
        }
    }

    @PUT
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$QUESTIONNAIRE_SCHEDULE")
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
    fun generateScheduleUsingProtocol(
        @Valid assessment: Assessment,
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") subjectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            QuestionnaireScheduleContract.generateScheduleUsingProtocol(
                assessment,
                projectId,
                subjectId,
                taskServiceRoute.baseUrl,
            ).let {
                handleProxyResponse(it)
            }
        }
    }

    @GET
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$QUESTIONNAIRE_SCHEDULE")
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_READ, projectPathParam = "projectId", userPathParam = "subjectId")
    fun getScheduleUsingProjectIdAndSubjectId(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @QueryParam("type") @DefaultValue("all") type: String,
        @QueryParam("search") @DefaultValue("") search: String,
        @QueryParam("startTime") startTimeStr: String?,
        @QueryParam("endTime") endTimeStr: String?,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {

            QuestionnaireScheduleContract.getScheduleUsingProjectIdAndSubjectId(
                projectId,
                subjectId,
                type,
                search,
                startTimeStr,
                endTimeStr,
                taskServiceRoute.baseUrl,
            ).let { handleProxyResponse(it) }
        }
    }

    @DELETE
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$QUESTIONNAIRE_SCHEDULE")
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
    fun deleteScheduleForUser(
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") subjectId: String,
        @QueryParam("type") @DefaultValue("all") type: String,
        @QueryParam("search") @DefaultValue("") search: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            QuestionnaireScheduleContract.deleteScheduleForUser(
                projectId,
                subjectId,
                type,
                search,
                taskServiceRoute.baseUrl,
            ).let { handleProxyResponse(it) }
        }
    }

    //---------------------------------------Notification Service-----------------------------------------------------------
    @GET
    @Path(MESSAGING_NOTIFICATION_PATH)
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ)
    fun getAllNotifications(
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            handleProxyResponse(
                NotificationServiceContract.getAllNotifications(
                    cloudMessagingServiceRoute.baseUrl,
                ),
            )
        }
    }

    @GET
    @Path("$MESSAGING_NOTIFICATION_PATH/{id}")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE)
    fun getNotificationUsingId(
        @Valid @PathParam("id") id: Long,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            handleProxyResponse(
                NotificationServiceContract.getNotificationUsingId(id, taskServiceRoute.baseUrl),
            )
        }
    }

    @GET
    @Path("$MESSAGING_NOTIFICATION_PATH/filter")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ)
    fun getFilteredNotifications(
        @Valid @QueryParam("type") type: String?,
        @Valid @QueryParam("delivered") delivered: Boolean?,
        @Valid @QueryParam("ttlSeconds") ttlSeconds: Int?,
        @Valid @QueryParam("startTime") startTimeStr: String?,
        @Valid @QueryParam("endTime") endTimeStr: String?,
        @Valid @QueryParam("limit") limit: Int?,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            NotificationServiceContract.getFilteredNotifications(
                type,
                delivered,
                ttlSeconds,
                startTimeStr,
                endTimeStr,
                limit,
                cloudMessagingServiceRoute.baseUrl,
            ).let {
                handleProxyResponse(it)
            }
        }
    }

    @GET
    @Path("${PROJECTS_PATH}/${PROJECT_ID}/${USERS_PATH}/${SUBJECT_ID}/$MESSAGING_NOTIFICATION_PATH")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_READ, projectPathParam = "projectId", userPathParam = "subjectId")
    fun getNotificationsUsingProjectIdAndSubjectId(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            NotificationServiceContract.getNotificationsUsingProjectIdAndSubjectId(
                projectId,
                subjectId,
                cloudMessagingServiceRoute.baseUrl,
            ).let {
                handleProxyResponse(it)
            }
        }
    }

    @GET
    @Path("${PROJECTS_PATH}/${PROJECT_ID}/$MESSAGING_NOTIFICATION_PATH")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_READ)
    fun getNotificationsUsingProjectId(
        @Valid @PathParam("projectId") projectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            val token = tokenForCurrentRequest(asyncService, tokenProvider)
            authService.checkPermission(
                Permission.SUBJECT_READ,
                EntityDetails(project = projectId, subject = token.subject),
                token,
            )

            handleProxyResponse(
                NotificationServiceContract.getNotificationsUsingProjectId(
                    projectId, cloudMessagingServiceRoute.baseUrl,
                ),
            )
        }
    }

    @POST
    @Path("${PROJECTS_PATH}/${PROJECT_ID}/${USERS_PATH}/${SUBJECT_ID}/$MESSAGING_NOTIFICATION_PATH")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
    fun addSingleNotification(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @Valid fcmNotification: FcmNotificationDto,
        @QueryParam("schedule") @DefaultValue("true") schedule: Boolean,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            NotificationServiceContract.addSingleNotification(
                projectId,
                subjectId,
                fcmNotification,
                schedule,
                cloudMessagingServiceRoute.baseUrl,
            ).let {
                handleProxyResponse(it)
            }
        }
    }

    @POST
    @Path("${PROJECTS_PATH}/${PROJECT_ID}/${USERS_PATH}/${SUBJECT_ID}/$MESSAGING_NOTIFICATION_PATH/schedule")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
    fun scheduleUserNotifications(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            NotificationServiceContract.scheduleUserNotifications(
                projectId,
                subjectId,
                cloudMessagingServiceRoute.baseUrl,
            ).let {
                handleProxyResponse(it)
            }
        }
    }

    @POST
    @Path("${PROJECTS_PATH}/${PROJECT_ID}/${USERS_PATH}/${SUBJECT_ID}/$MESSAGING_NOTIFICATION_PATH/$NOTIFICATION_ID/schedule")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
    fun scheduleUserNotification(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @Valid @PathParam("notificationId") notificationId: Long,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            NotificationServiceContract.scheduleUserNotification(
                projectId,
                subjectId, notificationId, cloudMessagingServiceRoute.baseUrl,
            ).let {
                handleProxyResponse(it)
            }
        }
    }

    @POST
    @Path("${PROJECTS_PATH}/${PROJECT_ID}/${USERS_PATH}/${SUBJECT_ID}/$MESSAGING_NOTIFICATION_PATH/batch")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
    fun addBatchNotifications(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @QueryParam("schedule") @DefaultValue("false") schedule: Boolean,
        @Valid fcmNotification: FcmNotifications,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            NotificationServiceContract.addBatchNotifications(
                projectId, subjectId, schedule, fcmNotification, cloudMessagingServiceRoute.baseUrl,
            ).let {
                handleProxyResponse(it)
            }
        }
    }

    @PUT
    @Path("${PROJECTS_PATH}/${PROJECT_ID}/${USERS_PATH}/${SUBJECT_ID}/$MESSAGING_NOTIFICATION_PATH")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
    fun updateNotification(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @Valid fcmNotification: FcmNotificationDto,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            NotificationServiceContract.updateNotification(
                projectId,
                subjectId,
                fcmNotification,
                cloudMessagingServiceRoute.baseUrl,
            ).let {
                handleProxyResponse(it)
            }
        }
    }

    @DELETE
    @Path("${PROJECTS_PATH}/${PROJECT_ID}/${USERS_PATH}/${SUBJECT_ID}/$MESSAGING_NOTIFICATION_PATH/$ALL_KEYWORD")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
    fun deleteNotificationsForUser(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            NotificationServiceContract.deleteNotificationsForUser(
                projectId,
                subjectId,
                cloudMessagingServiceRoute.baseUrl,
            ).let {
                handleProxyResponse(it)
            }
        }
    }

    @DELETE
    @Path("${PROJECTS_PATH}/${PROJECT_ID}/${USERS_PATH}/${SUBJECT_ID}/$MESSAGING_NOTIFICATION_PATH/$NOTIFICATION_ID")
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
    fun deleteNotificationUsingProjectIdAndSubjectIdAndNotificationId(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @PathParam("notificationId") id: Long,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            NotificationServiceContract.deleteNotificationUsingProjectIdAndSubjectIdAndNotificationId(
                projectId,
                subjectId,
                id,
                cloudMessagingServiceRoute.baseUrl,
            ).let {
                handleProxyResponse(it)
            }
        }
    }

    @DELETE
    @Path("${PROJECTS_PATH}/${PROJECT_ID}/${USERS_PATH}/${SUBJECT_ID}/$MESSAGING_NOTIFICATION_PATH/$TASKS_PATH/{id}")
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
    fun deleteNotificationUsingProjectIdAndSubjectIdAndTaskId(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @PathParam("id") id: Long,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            NotificationServiceContract.deleteNotificationUsingProjectIdAndSubjectIdAndTaskId(
                projectId,
                subjectId,
                id,
                cloudMessagingServiceRoute.baseUrl,
            ).let {
                handleProxyResponse(it)
            }
        }
    }

// ------------------------------------------Notification State Event Service-------------------------------------------

    @GET
    @Path("/$MESSAGING_NOTIFICATION_PATH/$NOTIFICATION_ID/$NOTIFICATION_STATE_EVENTS_PATH")
    @Produces(APPLICATION_JSON)
    fun getNotificationStateEventsByNotificationId(
        @PathParam("notificationId") notificationId: Long,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            NotificationStateEventServiceContract.getNotificationStateEventsByNotificationId(
                notificationId,
                cloudMessagingServiceRoute.baseUrl,
            ).let {
                handleProxyResponse(it)
            }
        }
    }

    @GET
    @Path("/$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$MESSAGING_NOTIFICATION_PATH/$NOTIFICATION_ID/$NOTIFICATION_STATE_EVENTS_PATH")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_READ, projectPathParam = "projectId", userPathParam = "subjectId")
    fun getNotificationStateEvents(
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") subjectId: String,
        @PathParam("notificationId") notificationId: Long,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            NotificationStateEventServiceContract.getNotificationStateEvents(
                projectId,
                subjectId,
                notificationId,
                cloudMessagingServiceRoute.baseUrl,
            ).let {
                handleProxyResponse(it)
            }
        }
    }

    @POST
    @Path("/$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$MESSAGING_NOTIFICATION_PATH/$NOTIFICATION_ID/$NOTIFICATION_STATE_EVENTS_PATH")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
    fun postNotificationStateEvent(
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") subjectId: String,
        @PathParam("notificationId") notificationId: Long,
        notificationStateEventDto: NotificationStateEventDto,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            NotificationStateEventServiceContract.postNotificationStateEvents(
                projectId,
                subjectId,
                notificationId,
                notificationStateEventDto,
                cloudMessagingServiceRoute.baseUrl,
            ).let {
                handleProxyResponse(it)
            }
        }
    }

//-----------------------------------------------------Data Message Service---------------------------------------------

    @GET
    @Path(MESSAGING_DATA_PATH)
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ)
    fun getAllDataMessages(
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            handleProxyResponse(
                DataMessageServiceContract.getAllDataMessages(
                    cloudMessagingServiceRoute.baseUrl,
                ),
            )
        }
    }

    @GET
    @Path("$MESSAGING_DATA_PATH/{id}")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_READ)
    fun getDataMessageUsingId(
        @PathParam("id") id: Long,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            handleProxyResponse(
                DataMessageServiceContract.getDataMessageUsingId(id, taskServiceRoute.baseUrl),
            )
        }
    }

    @GET
    @Path("$MESSAGING_DATA_PATH/filtered")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ)
    fun getFilteredDataMessages(
        @Valid @QueryParam("type") type: String?,
        @Valid @QueryParam("delivered") delivered: Boolean?,
        @Valid @QueryParam("ttlSeconds") ttlSeconds: Int?,
        @Valid @QueryParam("startTime") startTimeStr: String?,
        @Valid @QueryParam("endTime") endTimeStr: String?,
        @Valid @QueryParam("limit") limit: Int?,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            DataMessageServiceContract.getFilteredDataMessages(
                type,
                delivered,
                ttlSeconds,
                startTimeStr,
                endTimeStr,
                limit,
                cloudMessagingServiceRoute.baseUrl,
            ).let {
                handleProxyResponse(it)
            }
        }
    }

    @GET
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$MESSAGING_DATA_PATH")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_READ, projectPathParam = "projectId", userPathParam = "subjectId")
    fun getDataMessagesUsingProjectIdAndSubjectId(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            DataMessageServiceContract.getDataMessageUsingProjectIdAndSubjectId(
                projectId,
                subjectId,
                cloudMessagingServiceRoute.baseUrl,
            ).let {
                handleProxyResponse(it)
            }
        }
    }

    @GET
    @Path("$PROJECTS_PATH/$PROJECT_ID/$MESSAGING_DATA_PATH")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_READ)
    fun getDataMessagesUsingProjectId(
        @Valid @PathParam("projectId") projectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            val token = tokenForCurrentRequest(asyncService, tokenProvider)
            authService.checkPermission(
                Permission.SUBJECT_READ,
                EntityDetails(project = projectId, subject = token.subject),
                token,
            )

            DataMessageServiceContract.getDataMessagesUsingProjectId(
                projectId, cloudMessagingServiceRoute.baseUrl,
            ).let {
                handleProxyResponse(it)
            }
        }
    }

    @POST
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$MESSAGING_DATA_PATH")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
    fun addSingleDataMessage(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @Valid fcmDataMessage: FcmDataMessageDto,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            DataMessageServiceContract.addSingleDataMessage(
                projectId, subjectId, fcmDataMessage, cloudMessagingServiceRoute.baseUrl,
            ).let {
                handleProxyResponse(it)
            }
        }
    }

    @POST
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$MESSAGING_DATA_PATH/batch")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
    fun addBatchDataMessages(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @Valid fcmDataMessages: FcmDataMessages,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            DataMessageServiceContract.addBatchDataMessages(
                projectId, subjectId, fcmDataMessages, cloudMessagingServiceRoute.baseUrl,
            ).let {
                handleProxyResponse(it)
            }
        }
    }

    @PUT
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$MESSAGING_DATA_PATH")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
    fun updateDataMessage(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @Valid fcmDataMessage: FcmDataMessageDto,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            DataMessageServiceContract.updateDataMessage(
                projectId, subjectId, fcmDataMessage, cloudMessagingServiceRoute.baseUrl,
            ).let {
                handleProxyResponse(it)
            }
        }
    }

    @DELETE
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$MESSAGING_DATA_PATH/$ALL_KEYWORD")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
    fun deleteDataMessageForUser(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            handleProxyResponse(
                DataMessageServiceContract.deleteDataMessageForUser(
                    projectId,
                    subjectId,
                    cloudMessagingServiceRoute.baseUrl,
                ),
            )
        }
    }

    @DELETE
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$MESSAGING_DATA_PATH/{id}")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
    fun deleteDataMessageUsingProjectIdAndSubjectIdAndDataMessageId(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @PathParam("id") id: Long,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            DataMessageServiceContract.deleteDataMessageUsingProjectIdAndSubjectIdAndNotificationId(
                projectId,
                subjectId,
                id,
                cloudMessagingServiceRoute.baseUrl,
            ).let {
                handleProxyResponse(it)
            }
        }
    }
}
