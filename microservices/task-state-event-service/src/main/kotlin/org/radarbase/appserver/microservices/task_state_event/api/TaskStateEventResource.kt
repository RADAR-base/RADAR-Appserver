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

package org.radarbase.appserver.microservices.task_state_event.api

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
import org.radarbase.appserver.microservices.core.dto.TaskStateEventDto
import org.radarbase.appserver.microservices.core.service.TaskStateEventService
import org.radarbase.appserver.microservices.core.utils.Paths.PROJECTS_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.PROJECT_ID
import org.radarbase.appserver.microservices.core.utils.Paths.QUESTIONNAIRE_SCHEDULE
import org.radarbase.appserver.microservices.core.utils.Paths.QUESTIONNAIRE_STATE_EVENTS_PATH
import org.radarbase.appserver.microservices.core.utils.Paths.SUBJECT_ID
import org.radarbase.appserver.microservices.core.utils.Paths.TASK_ID
import org.radarbase.appserver.microservices.core.utils.Paths.USERS_PATH
import org.radarbase.appserver.microservices.task_state_event.config.TaskStateEventServiceConfig
import org.radarbase.jersey.service.AsyncCoroutineService
import kotlin.time.Duration.Companion.seconds

@Suppress("UnresolvedRestParam")
@Path("/")
class TaskStateEventResource @Inject constructor(
    private val taskStateEventService: TaskStateEventService,
    private val asyncService: AsyncCoroutineService,
    taskStateEventServiceConfig: TaskStateEventServiceConfig,
) {
    private val requestTimeout = taskStateEventServiceConfig.server.requestTimeout.seconds

    @GET
    @Path("/$QUESTIONNAIRE_SCHEDULE/$TASK_ID/$QUESTIONNAIRE_STATE_EVENTS_PATH")
    @Produces(APPLICATION_JSON)
    fun getTaskStateEventsByTaskId(
        @PathParam("taskId") taskId: Long,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            taskStateEventService.getTaskStateEventsByTaskId(taskId).let {
                Response.ok(it).build()
            }
        }
    }

    @GET
    @Path("/$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$QUESTIONNAIRE_SCHEDULE/$TASK_ID/$QUESTIONNAIRE_STATE_EVENTS_PATH")
    @Produces(APPLICATION_JSON)
    fun getTaskStateEvents(
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") subjectId: String,
        @PathParam("taskId") taskId: Long,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            taskStateEventService.getTaskStateEvents(projectId, subjectId, taskId).let {
                Response.ok(it).build()
            }
        }
    }

    @POST
    @Path("/$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$QUESTIONNAIRE_SCHEDULE/$TASK_ID/$QUESTIONNAIRE_STATE_EVENTS_PATH")
    @Produces(APPLICATION_JSON)
    fun postTaskStateEvent(
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") subjectId: String,
        @PathParam("taskId") taskId: Long,
        taskStateEventDto: TaskStateEventDto,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            taskStateEventService.publishTaskStateEventExternal(
                projectId,
                subjectId,
                taskId,
                taskStateEventDto,
            )

            taskStateEventService.getTaskStateEvents(projectId, subjectId, taskId).let {
                Response.ok(it).build()
            }
        }
    }
}
