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

package org.radarbase.appserver.jersey.service.questionnaire_schedule

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import org.radarbase.appserver.jersey.dto.protocol.Assessment
import org.radarbase.appserver.jersey.dto.protocol.Protocol
import org.radarbase.appserver.jersey.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.jersey.dto.questionnaire.Schedule
import org.radarbase.appserver.jersey.entity.Task
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.service.protocol.handler.ProtocolHandler
import org.radarbase.appserver.jersey.utils.mapParallel
import kotlin.collections.orEmpty

interface ScheduleGeneratorService {
    fun getProtocolHandler(assessment: Assessment): ProtocolHandler?

    fun getRepeatProtocolHandler(assessment: Assessment): ProtocolHandler?

    fun getRepeatQuestionnaireHandler(assessment: Assessment): ProtocolHandler?

    fun getNotificationHandler(assessment: Assessment): ProtocolHandler?

    fun getReminderHandler(assessment: Assessment): ProtocolHandler?

    fun getCompletedQuestionnaireHandler(
        assessment: Assessment,
        prevTasks: List<Task>,
        prevTimezone: String,
    ): ProtocolHandler?

    suspend fun generateScheduleForUser(
        user: User,
        protocol: Protocol,
        prevSchedule: Schedule,
    ): Schedule = coroutineScope {
        val assessments = protocol.protocols ?: return@coroutineScope Schedule()

        val prevScheduledTaskByName: Map<String?, List<Task>?> = prevSchedule.assessmentSchedules
            .associate { it.name to it.tasks }

        val prevTimezone = prevSchedule.timezone ?: user.timezone!!

        val assessmentSchedules = assessments.mapParallel(Dispatchers.Default) { assessment ->
            val prevTasks = prevScheduledTaskByName[assessment.name].orEmpty()
            generateSingleAssessmentSchedule(assessment, user, prevTasks, prevTimezone)
        }

        Schedule(assessmentSchedules, user, protocol.version)
    }

    suspend fun generateSingleAssessmentSchedule(
        assessment: Assessment,
        user: User,
        previousTasks: List<Task>,
        prevTimezone: String,
    ): AssessmentSchedule {
        val protocolHandlerRunner = ProtocolHandlerRunner().apply {
            addProtocolHandler(getProtocolHandler(assessment))
            addProtocolHandler(getRepeatProtocolHandler(assessment))
            addProtocolHandler(getRepeatQuestionnaireHandler(assessment))
            addProtocolHandler(getNotificationHandler(assessment))
            addProtocolHandler(getReminderHandler(assessment))
            addProtocolHandler(
                getCompletedQuestionnaireHandler(assessment, previousTasks, prevTimezone),
            )
        }
        return protocolHandlerRunner.runProtocolHandlers(assessment, user)
    }
}
