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
import jakarta.ws.rs.DELETE
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
import org.radarbase.appserver.jersey.config.AppserverConfig
import org.radarbase.appserver.jersey.dto.fcm.FcmDataMessageDto
import org.radarbase.appserver.jersey.dto.fcm.FcmDataMessages
import org.radarbase.appserver.jersey.service.FcmDataMessageService
import org.radarbase.appserver.jersey.utils.Paths.ALL_KEYWORD
import org.radarbase.appserver.jersey.utils.Paths.MESSAGING_DATA_PATH
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
import java.time.LocalDateTime
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Suppress("UnresolvedRestParam")
@Path("/")
class FcmDataMessageResource @Inject constructor(
    private val fcmDataMessageService: FcmDataMessageService,
    private val asyncService: AsyncCoroutineService,
    private val authService: AuthService,
    private val tokenProvider: Provider<RadarToken>,
    config: AppserverConfig,
) {
    private val requestTimeout: Duration = config.server.requestTimeout.seconds
    private val mpSecurityEnabled: Boolean = config.mp.security.enabled

    @GET
    @Path(MESSAGING_DATA_PATH)
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ)
    fun getAllDataMessages(
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            Response.ok(fcmDataMessageService.getAllDataMessages()).build()
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
            Response.ok(fcmDataMessageService.getDataMessageById(id)).build()
        }
    }

    @GET
    @Path("$MESSAGING_DATA_PATH/filter")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ)
    fun getFilteredDataMessages(
        @Valid @QueryParam("type") type: String?,
        @Valid @QueryParam("delivered") delivered: Boolean?,
        @Valid @QueryParam("ttlSeconds") ttlSeconds: Int?,
        @Valid @QueryParam("startTime") startTime: LocalDateTime?,
        @Valid @QueryParam("endTime") endTime: LocalDateTime?,
        @Valid @QueryParam("limit") limit: Int?,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            Response.ok(
                fcmDataMessageService.getFilteredDataMessages(
                    type, delivered, ttlSeconds, startTime, endTime, limit,
                ),
            ).build()
        }
    }

    @GET
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$MESSAGING_DATA_PATH")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_READ, projectPathParam = "projectId", userPathParam = "subjectId")
    fun getDataMessageUsingProjectIdAndSubjectId(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            fcmDataMessageService.getDataMessagesByProjectIdAndSubjectId(projectId, subjectId).let {
                Response.ok(it).build()
            }
        }
    }

    @GET
    @Path("$PROJECTS_PATH/$PROJECT_ID/$MESSAGING_DATA_PATH")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_READ, projectPathParam = "projectId")
    fun getDataMessageUsingProjectId(
        @Valid @PathParam("projectId") projectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            if (mpSecurityEnabled) {
                val token = tokenForCurrentRequest(asyncService, tokenProvider)
                authService.checkPermission(
                    Permission.SUBJECT_READ,
                    EntityDetails(project = projectId, subject = token.subject),
                    token,
                )
                fcmDataMessageService.getDataMessagesByProjectId(projectId).let {
                    Response.ok(it).build()
                }
            } else {
                fcmDataMessageService.getDataMessagesByProjectId(projectId).let {
                    Response.ok(it).build()
                }
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
            fcmDataMessageService.addDataMessage(
                fcmDataMessage,
                subjectId,
                projectId,
            ).let {
                Response.created(URI("$MESSAGING_DATA_PATH/${it.id}")).entity(it).build()
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
            fcmDataMessageService.addDataMessages(
                fcmDataMessages, subjectId, projectId,
            ).let {
                Response.ok().build()
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
            fcmDataMessageService.updateDataMessage(
                fcmDataMessage,
                subjectId,
                projectId,
            ).let {
                Response.ok(it).build()
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
            fcmDataMessageService.removeDataMessagesForUser(projectId, subjectId).let {
                Response.ok().build()
            }
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
            fcmDataMessageService.deleteDataMessageByProjectIdAndSubjectIdAndDataMessageId(
                projectId, subjectId, id,
            )
        }
    }
}
