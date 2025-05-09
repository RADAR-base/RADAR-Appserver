/*
 *
 *  *
 *  *  * Copyright 2018 King's College London
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *  *
 *  *
 *
 */

package org.radarbase.appserver.service.questionnaire.schedule

import org.radarbase.appserver.dto.protocol.Assessment
import org.radarbase.appserver.dto.protocol.Protocol
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.dto.questionnaire.Schedule
import org.radarbase.appserver.entity.Task
import org.radarbase.appserver.entity.User
import org.radarbase.appserver.service.questionnaire.protocol.ProtocolHandler
import java.util.stream.Collectors

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

    fun generateScheduleForUser(user: User, protocol: Protocol, prevSchedule: Schedule): Schedule {
        val assessments: List<Assessment> = protocol.protocols ?: return Schedule()
        val prevAssessmentSchedules: List<AssessmentSchedule> = prevSchedule.assessmentSchedules
        val prevTimezone: String = prevSchedule.timezone ?: user.timezone!!

        val assessmentSchedules: List<AssessmentSchedule> = assessments.parallelStream().map { assessment: Assessment ->
            val prevTasks: List<Task> = prevAssessmentSchedules.firstOrNull { it.name == assessment.name }?.tasks
                ?: emptyList()
            generateSingleAssessmentSchedule(assessment, user, prevTasks, prevTimezone)
        }.collect(Collectors.toList())

        return Schedule(assessmentSchedules, user, protocol.version)
    }

    fun generateSingleAssessmentSchedule(
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
