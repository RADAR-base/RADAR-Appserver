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

import lombok.extern.slf4j.Slf4j
import org.radarbase.appserver.dto.protocol.Assessment
import org.radarbase.appserver.dto.protocol.AssessmentType
import org.radarbase.appserver.entity.Task
import org.radarbase.appserver.service.questionnaire.protocol.ProtocolHandler
import org.radarbase.appserver.service.questionnaire.protocol.factory.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.IOException

@Slf4j
@Service
class QuestionnaireScheduleGeneratorService @Autowired constructor() : ScheduleGeneratorService {
    override fun getProtocolHandler(assessment: Assessment): ProtocolHandler? {
        if (assessment.getType() == AssessmentType.CLINICAL) return ProtocolHandlerFactory.getProtocolHandler(
            ProtocolHandlerType.CLINICAL
        )
        else return ProtocolHandlerFactory.getProtocolHandler(ProtocolHandlerType.SIMPLE)
    }

    override fun getRepeatProtocolHandler(assessment: Assessment): ProtocolHandler? {
        if (assessment.getType() == AssessmentType.CLINICAL) return null

        var type = RepeatProtocolHandlerType.SIMPLE
        val repeatProtocol = assessment.getProtocol().getRepeatProtocol()
        if (repeatProtocol.getDayOfWeek() != null) type = RepeatProtocolHandlerType.DAYOFWEEK
        return RepeatProtocolHandlerFactory.getRepeatProtocolHandler(type)
    }

    override fun getRepeatQuestionnaireHandler(assessment: Assessment): ProtocolHandler? {
        if (assessment.getType() == AssessmentType.CLINICAL) return null

        var type = RepeatQuestionnaireHandlerType.SIMPLE
        val repeatQuestionnaire = assessment.getProtocol().getRepeatQuestionnaire()
        if (repeatQuestionnaire.getDayOfWeekMap() != null) type = RepeatQuestionnaireHandlerType.DAYOFWEEKMAP
        if (repeatQuestionnaire.getRandomUnitsFromZeroBetween() != null) type = RepeatQuestionnaireHandlerType.RANDOM
        return RepeatQuestionnaireHandlerFactory.getRepeatQuestionnaireHandler(type)
    }

    override fun getNotificationHandler(assessment: Assessment): ProtocolHandler? {
        if (assessment.getType() == AssessmentType.CLINICAL) return null
        val protocol = assessment.getProtocol().getNotification()

        try {
            return NotificationHandlerFactory.getNotificationHandler(protocol)
        } catch (e: IOException) {
            QuestionnaireScheduleGeneratorService.log.error("Invalid Notification Handler Type")
            return null
        }
    }

    override fun getReminderHandler(assessment: Assessment): ProtocolHandler? {
        if (assessment.getType() == AssessmentType.CLINICAL) return null

        return ReminderHandlerFactory.getReminderHandler()
    }

    public override fun getCompletedQuestionnaireHandler(
        assessment: Assessment,
        prevTasks: MutableList<Task?>?,
        prevTimezone: String?
    ): ProtocolHandler? {
        if (assessment.getType() == AssessmentType.CLINICAL) return null

        return CompletedQuestionnaireHandlerFactory.getCompletedQuestionnaireHandler(prevTasks, prevTimezone)
    }
}
