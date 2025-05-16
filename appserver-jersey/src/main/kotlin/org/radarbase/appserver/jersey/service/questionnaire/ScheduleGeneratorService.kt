package org.radarbase.appserver.jersey.service.questionnaire

import org.radarbase.appserver.jersey.dto.protocol.Assessment
import org.radarbase.appserver.jersey.dto.protocol.Protocol
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.service.questionnaire.protocol.ProtocolHandler
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
