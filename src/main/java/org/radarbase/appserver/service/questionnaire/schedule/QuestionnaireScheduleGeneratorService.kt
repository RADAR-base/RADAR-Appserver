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
import org.radarbase.appserver.dto.protocol.AssessmentType.CLINICAL
import org.radarbase.appserver.dto.protocol.NotificationProtocol
import org.radarbase.appserver.dto.protocol.RepeatProtocol
import org.radarbase.appserver.dto.protocol.RepeatQuestionnaire
import org.radarbase.appserver.entity.Task
import org.radarbase.appserver.service.questionnaire.protocol.ProtocolHandler
import org.radarbase.appserver.service.questionnaire.protocol.factory.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.IOException

@Service
class QuestionnaireScheduleGeneratorService : ScheduleGeneratorService {

    override fun getProtocolHandler(assessment: Assessment): ProtocolHandler {
        return when (assessment.type) {
            CLINICAL -> ProtocolHandlerFactory.getProtocolHandler(ProtocolHandlerType.CLINICAL)
            else -> ProtocolHandlerFactory.getProtocolHandler(ProtocolHandlerType.SIMPLE)
        }
    }

    override fun getRepeatProtocolHandler(assessment: Assessment): ProtocolHandler? {
        if (assessment.type == CLINICAL) return null

        val repeatProtocol: RepeatProtocol? = assessment.protocol.repeatProtocol
        val type = if (repeatProtocol?.dayOfWeek != null) {
            RepeatProtocolHandlerType.DAYOFWEEK
        } else {
            RepeatProtocolHandlerType.SIMPLE
        }
        return RepeatProtocolHandlerFactory.getRepeatProtocolHandler(type)
    }

    override fun getRepeatQuestionnaireHandler(assessment: Assessment): ProtocolHandler? {
        if (assessment.type == CLINICAL) return null

        val repeatQuestionnaire: RepeatQuestionnaire? = assessment.protocol.repeatQuestionnaire
        val type = when {
            repeatQuestionnaire?.dayOfWeekMap != null -> RepeatQuestionnaireHandlerType.DAYOFWEEKMAP
            repeatQuestionnaire?.randomUnitsFromZeroBetween != null -> RepeatQuestionnaireHandlerType.RANDOM
            else -> RepeatQuestionnaireHandlerType.SIMPLE
        }

        return RepeatQuestionnaireHandlerFactory.getRepeatQuestionnaireHandler(type)
    }

    override fun getNotificationHandler(assessment: Assessment): ProtocolHandler? {
        if (assessment.getType() == CLINICAL) return null
        val protocol: NotificationProtocol = assessment.protocol?.notification ?: return null

        return try {
            NotificationHandlerFactory.getNotificationHandler(protocol)
        } catch (_: IOException) {
            logger.error("Invalid Notification Handler Type")
            null
        }
    }

    override fun getReminderHandler(assessment: Assessment): ProtocolHandler? {
        return if (assessment.getType() == CLINICAL) {
            null
        } else ReminderHandlerFactory.reminderHandler
    }

    override fun getCompletedQuestionnaireHandler(
        assessment: Assessment, prevTasks: List<Task>, prevTimezone: String
    ): ProtocolHandler? {
        return if (assessment.getType() == CLINICAL) {
            null
        } else CompletedQuestionnaireHandlerFactory.getCompletedQuestionnaireHandler(prevTasks, prevTimezone)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(QuestionnaireScheduleGeneratorService::class.java)
    }
}
