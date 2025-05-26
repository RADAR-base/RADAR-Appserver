package org.radarbase.appserver.jersey.service.questionnaire_schedule

import org.radarbase.appserver.jersey.dto.protocol.Assessment
import org.radarbase.appserver.jersey.dto.protocol.AssessmentType
import org.radarbase.appserver.jersey.dto.protocol.NotificationProtocol
import org.radarbase.appserver.jersey.dto.protocol.RepeatProtocol
import org.radarbase.appserver.jersey.dto.protocol.RepeatQuestionnaire
import org.radarbase.appserver.jersey.entity.Task
import org.radarbase.appserver.jersey.service.protocol.handler.ProtocolHandler
import org.radarbase.appserver.jersey.service.protocol.handler.factory.CompletedQuestionnaireHandlerFactory
import org.radarbase.appserver.jersey.service.protocol.handler.factory.NotificationHandlerFactory
import org.radarbase.appserver.jersey.service.protocol.handler.factory.ProtocolHandlerFactory
import org.radarbase.appserver.jersey.service.protocol.handler.factory.ProtocolHandlerType
import org.radarbase.appserver.jersey.service.protocol.handler.factory.ReminderHandlerFactory
import org.radarbase.appserver.jersey.service.protocol.handler.factory.RepeatProtocolHandlerFactory
import org.radarbase.appserver.jersey.service.protocol.handler.factory.RepeatProtocolHandlerType
import org.radarbase.appserver.jersey.service.protocol.handler.factory.RepeatQuestionnaireHandlerFactory
import org.radarbase.appserver.jersey.service.protocol.handler.factory.RepeatQuestionnaireHandlerType
import org.slf4j.LoggerFactory
import java.io.IOException

class QuestionnaireScheduleGeneratorService : ScheduleGeneratorService {

    override fun getProtocolHandler(assessment: Assessment): ProtocolHandler {
        return when (assessment.type) {
            AssessmentType.CLINICAL -> ProtocolHandlerFactory.getProtocolHandler(ProtocolHandlerType.CLINICAL)
            else -> ProtocolHandlerFactory.getProtocolHandler(ProtocolHandlerType.SIMPLE)
        }
    }

    override fun getRepeatProtocolHandler(assessment: Assessment): ProtocolHandler? {
        if (assessment.type == AssessmentType.CLINICAL) return null

        val repeatProtocol: RepeatProtocol? = assessment.protocol?.repeatProtocol
        val type = if (repeatProtocol?.dayOfWeek != null) {
            RepeatProtocolHandlerType.DAYOFWEEK
        } else {
            RepeatProtocolHandlerType.SIMPLE
        }
        return RepeatProtocolHandlerFactory.getRepeatProtocolHandler(type)
    }

    override fun getRepeatQuestionnaireHandler(assessment: Assessment): ProtocolHandler? {
        if (assessment.type == AssessmentType.CLINICAL) return null

        val repeatQuestionnaire: RepeatQuestionnaire? = assessment.protocol?.repeatQuestionnaire
        val type = when {
            repeatQuestionnaire?.dayOfWeekMap != null -> RepeatQuestionnaireHandlerType.DAYOFWEEKMAP
            repeatQuestionnaire?.randomUnitsFromZeroBetween != null -> RepeatQuestionnaireHandlerType.RANDOM
            else -> RepeatQuestionnaireHandlerType.SIMPLE
        }

        return RepeatQuestionnaireHandlerFactory.getRepeatQuestionnaireHandler(type)
    }

    override fun getNotificationHandler(assessment: Assessment): ProtocolHandler? {
        if (assessment.type == AssessmentType.CLINICAL) return null
        val protocol: NotificationProtocol = assessment.protocol?.notification ?: return null

        return try {
            NotificationHandlerFactory.getNotificationHandler(protocol)
        } catch (_: IOException) {
            logger.error("Invalid Notification Handler Type")
            null
        }
    }

    override fun getReminderHandler(assessment: Assessment): ProtocolHandler? {
        return if (assessment.type == AssessmentType.CLINICAL) {
            null
        } else ReminderHandlerFactory.reminderHandler
    }

    override fun getCompletedQuestionnaireHandler(
        assessment: Assessment, prevTasks: List<Task>, prevTimezone: String
    ): ProtocolHandler? {
        return if (assessment.type == AssessmentType.CLINICAL) {
            null
        } else CompletedQuestionnaireHandlerFactory.getCompletedQuestionnaireHandler(prevTasks, prevTimezone)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(QuestionnaireScheduleGeneratorService::class.java)
    }
}
