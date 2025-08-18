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

import jakarta.inject.Provider
import jakarta.validation.Valid
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
import org.radarbase.appserver.jersey.config.AppserverConfig
import org.radarbase.appserver.jersey.dto.fcm.FcmNotificationDto
import org.radarbase.appserver.jersey.service.FcmNotificationService
import org.radarbase.appserver.jersey.utils.Paths.ALL_KEYWORD
import org.radarbase.appserver.jersey.utils.Paths.MESSAGING_NOTIFICATION_PATH
import org.radarbase.appserver.jersey.utils.Paths.NOTIFICATION_ID
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
class FcmNotificationResource(
    private val asyncService: AsyncCoroutineService,
    private val authService: AuthService,
    private val tokenProvider: Provider<RadarToken>,
    private val fcmNotificationService: FcmNotificationService,
    config: AppserverConfig,
) {
    private val requestTimeout: Duration = config.server.requestTimeout.seconds

    @GET
    @Path(MESSAGING_NOTIFICATION_PATH)
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ)
    fun getAllNotifications(
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            fcmNotificationService.getAllNotifications().let {
                Response.ok(it).build()
            }
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
            fcmNotificationService.getNotificationById(id).let {
                Response.ok(it).build()
            }
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
        val startTime = startTimeStr?.let { LocalDateTime.parse(it) }
        val endTime = endTimeStr?.let { LocalDateTime.parse(it) }

        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            Response.ok(
                fcmNotificationService.getFilteredNotifications(
                    type,
                    delivered,
                    ttlSeconds,
                    startTime,
                    endTime,
                    limit,
                ),
            ).build()
        }
    }

    @GET
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$MESSAGING_NOTIFICATION_PATH")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_READ, projectPathParam = "projectId", userPathParam = "subjectId")
    fun getNotificationsUsingProjectIdAndSubjectId(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            fcmNotificationService.getNotificationsByProjectIdAndSubjectId(projectId, subjectId).let {
                Response.ok(it).build()
            }
        }
    }

    @GET
    @Path("$PROJECTS_PATH/$PROJECT_ID/$MESSAGING_NOTIFICATION_PATH")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_READ, projectPathParam = "projectId")
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
            fcmNotificationService.getNotificationsByProjectId(projectId).let {
                Response.ok(it).build()
            }
        }
    }

    @POST
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$MESSAGING_NOTIFICATION_PATH")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
    fun addSingleNotification(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @Valid fcmNotification: FcmNotificationDto,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            fcmNotificationService.addNotification(
                fcmNotification,
                subjectId,
                projectId,
            ).let {
                Response.created(URI("$MESSAGING_NOTIFICATION_PATH/${it.id}")).entity(it).build()
            }
        }
    }

    @POST
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$MESSAGING_NOTIFICATION_PATH/schedule")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
    fun scheduleUserNotifications(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            fcmNotificationService.scheduleAllUserNotifications(subjectId, projectId).let {
                Response.ok(it).build()
            }
        }
    }

    @POST
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$MESSAGING_NOTIFICATION_PATH/$NOTIFICATION_ID/schedule")
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
            fcmNotificationService.scheduleNotification(subjectId, projectId, notificationId).let {
                Response.ok(it).build()
            }
        }
    }

    @POST
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$MESSAGING_NOTIFICATION_PATH/batch")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
    fun addBatchNotifications(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @QueryParam("schedule") @DefaultValue("false") schedule: Boolean,
        @Valid fcmNotification: FcmNotificationDto,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            fcmNotificationService.addNotification(
                fcmNotification,
                subjectId,
                projectId,
                schedule,
            ).let {
                Response.ok().build()
            }
        }
    }

    @PUT
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$MESSAGING_NOTIFICATION_PATH")
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
            fcmNotificationService.updateNotification(
                fcmNotification,
                subjectId,
                projectId,
            ).let {
                Response.ok(it).build()
            }
        }
    }

    @DELETE
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$MESSAGING_NOTIFICATION_PATH/$ALL_KEYWORD")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
    fun deleteNotificationsForUser(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            fcmNotificationService.removeNotificationsForUser(projectId, subjectId).let {
                Response.ok().build()
            }
        }
    }

    @DELETE
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$MESSAGING_NOTIFICATION_PATH/$NOTIFICATION_ID")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_UPDATE, projectPathParam = "projectId", userPathParam = "subjectId")
    fun deleteNotificationUsingProjectIdAndSubjectIdAndNotificationId(
        @Valid @PathParam("projectId") projectId: String,
        @Valid @PathParam("subjectId") subjectId: String,
        @PathParam("notificationId") id: Long,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            fcmNotificationService.deleteNotificationByProjectIdAndSubjectIdAndNotificationId(
                projectId,
                subjectId,
                id,
            )
        }
    }
}
