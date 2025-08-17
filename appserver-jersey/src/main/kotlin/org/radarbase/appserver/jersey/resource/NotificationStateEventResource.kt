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
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.container.Suspended
import jakarta.ws.rs.core.MediaType.APPLICATION_JSON
import jakarta.ws.rs.core.Response
import org.radarbase.appserver.jersey.config.AppserverConfig
import org.radarbase.appserver.jersey.dto.NotificationStateEventDto
import org.radarbase.appserver.jersey.service.NotificationStateEventService
import org.radarbase.appserver.jersey.utils.Paths.MESSAGING_NOTIFICATION_PATH
import org.radarbase.appserver.jersey.utils.Paths.NOTIFICATION_ID
import org.radarbase.appserver.jersey.utils.Paths.NOTIFICATION_STATE_EVENTS_PATH
import org.radarbase.appserver.jersey.utils.Paths.PROJECTS_PATH
import org.radarbase.appserver.jersey.utils.Paths.PROJECT_ID
import org.radarbase.appserver.jersey.utils.Paths.SUBJECT_ID
import org.radarbase.appserver.jersey.utils.Paths.USERS_PATH
import org.radarbase.auth.authorization.Permission
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarbase.jersey.service.AsyncCoroutineService
import kotlin.time.Duration.Companion.seconds

@Suppress("UnresolvedRestParam")
@Path("/")
class NotificationStateEventResource @Inject constructor(
    private val notificationStateEventService: NotificationStateEventService,
    private val asyncService: AsyncCoroutineService,
    appserverConfig: AppserverConfig,
) {
    private val requestTimeout = appserverConfig.server.requestTimeout.seconds

    @GET
    @Path("/$MESSAGING_NOTIFICATION_PATH/$NOTIFICATION_ID/$NOTIFICATION_STATE_EVENTS_PATH")
    @Produces(APPLICATION_JSON)
    @Authenticated
    @NeedsPermission(Permission.PROJECT_READ)
    fun getNotificationStateEventsByNotificationId(
        @PathParam("notificationId") notificationId: Long,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            notificationStateEventService.getNotificationStateEventsByNotificationId(notificationId).let {
                Response.ok(it).build()
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
            notificationStateEventService.getNotificationStateEvents(
                projectId,
                subjectId,
                notificationId,
            ).let {
                Response.ok(it).build()
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
            notificationStateEventService.publishNotificationStateEventExternal(
                projectId,
                subjectId,
                notificationId,
                notificationStateEventDto
            )
            notificationStateEventService.getNotificationStateEvents(
                projectId,
                subjectId,
                notificationId
            ).let {
                Response.ok(it).build()
            }
        }
    }


}
