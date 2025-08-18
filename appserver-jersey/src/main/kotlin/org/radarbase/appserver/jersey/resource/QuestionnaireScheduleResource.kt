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

@file:Suppress("UnresolvedRestParam")

package org.radarbase.appserver.jersey.resource

import jakarta.inject.Inject
import jakarta.validation.Valid
import jakarta.ws.rs.DELETE
import jakarta.ws.rs.DefaultValue
import jakarta.ws.rs.GET
import jakarta.ws.rs.POST
import jakarta.ws.rs.PUT
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.QueryParam
import jakarta.ws.rs.container.AsyncResponse
import jakarta.ws.rs.container.Suspended
import jakarta.ws.rs.core.Response
import org.radarbase.appserver.jersey.config.AppserverConfig
import org.radarbase.appserver.jersey.dto.protocol.Assessment
import org.radarbase.appserver.jersey.dto.protocol.AssessmentType
import org.radarbase.appserver.jersey.service.questionnaire.schedule.QuestionnaireScheduleService
import org.radarbase.appserver.jersey.utils.Paths.PROJECTS_PATH
import org.radarbase.appserver.jersey.utils.Paths.PROJECT_ID
import org.radarbase.appserver.jersey.utils.Paths.QUESTIONNAIRE_SCHEDULE
import org.radarbase.appserver.jersey.utils.Paths.SUBJECT_ID
import org.radarbase.appserver.jersey.utils.Paths.USERS_PATH
import org.radarbase.auth.authorization.Permission
import org.radarbase.jersey.auth.Authenticated
import org.radarbase.jersey.auth.NeedsPermission
import org.radarbase.jersey.service.AsyncCoroutineService
import java.net.URI
import java.time.Instant
import java.util.Locale
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Path("/")
class QuestionnaireScheduleResource @Inject constructor(
    private val scheduleService: QuestionnaireScheduleService,
    private val asyncService: AsyncCoroutineService,
    appserverConfig: AppserverConfig,
) {
    private val requestTimeout: Duration = appserverConfig.server.requestTimeout.seconds

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
            try {
                scheduleService.generateScheduleUsingProjectIdAndSubjectId(
                    projectId,
                    subjectId,
                )
                Response.created(
                    URI("$PROJECTS_PATH/$projectId/$USERS_PATH/$subjectId/$QUESTIONNAIRE_SCHEDULE"),
                ).build()
            } catch (ex: Exception) {
                Response.status(Response.Status.BAD_REQUEST).entity(
                    "Error while generating schedule: ${ex.message}",
                )
            }
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
            try {
                scheduleService.generateScheduleUsingProjectIdAndSubjectIdAndAssessment(
                    projectId,
                    subjectId,
                    assessment,
                )
                Response.created(
                    URI("$PROJECTS_PATH/$projectId/$USERS_PATH/$subjectId/$QUESTIONNAIRE_SCHEDULE"),
                ).build()
            } catch (ex: Exception) {
                Response.status(Response.Status.BAD_REQUEST).entity(
                    "Error while generating schedule: ${ex.message}",
                )
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
            val startTime: Instant? = startTimeStr?.let { Instant.parse(it) }
            val endTime: Instant? = endTimeStr?.let { Instant.parse(it) }
            val assessmentType = AssessmentType.valueOf(type.uppercase(Locale.getDefault()))

            var sent = false
            if (startTime != null && endTime != null) {
                scheduleService.getTasksForDateUsingProjectIdAndSubjectId(
                    projectId,
                    subjectId,
                    startTime,
                    endTime,
                ).let {
                    sent = true
                    Response.ok(it).build()
                }
            }

            if (assessmentType != AssessmentType.ALL) {
                sent = true
                Response.ok(
                    scheduleService.getTasksByTypeUsingProjectIdAndSubjectId(
                        projectId,
                        subjectId,
                        assessmentType,
                        search,
                    ),
                ).build()
            }

            if (!sent) {
                Response.ok(
                    scheduleService.getTasksUsingProjectIdAndSubjectId(
                        projectId,
                        subjectId,
                    ),
                ).build()
            }
        }
    }

    @DELETE
    @Path("$PROJECTS_PATH/$PROJECT_ID/$USERS_PATH/$SUBJECT_ID/$QUESTIONNAIRE_SCHEDULE")
    @Authenticated
    @NeedsPermission(Permission.SUBJECT_READ, projectPathParam = "projectId", userPathParam = "subjectId")
    fun deleteScheduleForUser(
        @PathParam("projectId") projectId: String,
        @PathParam("subjectId") subjectId: String,
        @QueryParam("type") @DefaultValue("all") type: String,
        @QueryParam("search") @DefaultValue("") search: String,
        @Suspended asyncResponse: AsyncResponse,
    ) {
        asyncService.runAsCoroutine(asyncResponse, requestTimeout) {
            val assessmentType = AssessmentType.valueOf(type.uppercase(Locale.getDefault()))
            scheduleService.removeScheduleForUserUsingSubjectIdAndType(
                projectId,
                subjectId,
                assessmentType,
                search,
            )
            Response.ok().build()
        }
    }
}
