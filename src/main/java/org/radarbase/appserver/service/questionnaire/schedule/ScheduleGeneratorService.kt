package org.radarbase.appserver.service.questionnaire.schedule

import org.radarbase.appserver.dto.protocol.Assessment
import org.radarbase.appserver.dto.protocol.Protocol
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.dto.questionnaire.Schedule
import org.radarbase.appserver.entity.Task
import org.radarbase.appserver.entity.User
import org.radarbase.appserver.service.questionnaire.protocol.ProtocolHandler
import java.util.function.Function
import java.util.stream.Collectors

interface ScheduleGeneratorService {
    fun getProtocolHandler(assessment: Assessment?): ProtocolHandler?

    fun getRepeatProtocolHandler(assessment: Assessment?): ProtocolHandler?

    fun getRepeatQuestionnaireHandler(assessment: Assessment?): ProtocolHandler?

    fun getNotificationHandler(assessment: Assessment?): ProtocolHandler?

    fun getReminderHandler(assessment: Assessment?): ProtocolHandler?

    fun getCompletedQuestionnaireHandler(
        assessment: Assessment?,
        prevTasks: MutableList<Task?>?,
        prevTimezone: String?
    ): ProtocolHandler?

    fun generateScheduleForUser(user: User, protocol: Protocol, prevSchedule: Schedule): Schedule {
        val assessments = protocol.getProtocols()
        if (assessments == null) {
            return Schedule()
        }
        val prevAssessmentSchedules = prevSchedule.getAssessmentSchedules()
        val prevTimezone = if (prevSchedule.getTimezone() != null)
            prevSchedule.getTimezone()
        else
            user.timezone

        val assessmentSchedules = assessments.parallelStream()
            .map<AssessmentSchedule?> { assessment: Assessment? ->
                val tasks = prevAssessmentSchedules.stream()
                    .filter { a: AssessmentSchedule? -> a!!.getName() == assessment!!.getName() }
                    .findFirst()
                    .map<MutableList<Task?>>(Function { obj: AssessmentSchedule? -> obj!!.getTasks() })
                    .orElse(mutableListOf<Task?>())
                generateSingleAssessmentSchedule(assessment, user, tasks, prevTimezone)
            }
            .collect(Collectors.toList())

        return Schedule(assessmentSchedules, user, protocol.getVersion())
    }

    fun generateSingleAssessmentSchedule(
        assessment: Assessment?,
        user: User?,
        previousTasks: MutableList<Task?>?,
        prevTimezone: String?
    ): AssessmentSchedule? {
        val protocolHandlerRunner = ProtocolHandlerRunner()
        protocolHandlerRunner.addProtocolHandler(this.getProtocolHandler(assessment))
        protocolHandlerRunner.addProtocolHandler(this.getRepeatProtocolHandler(assessment))
        protocolHandlerRunner.addProtocolHandler(this.getRepeatQuestionnaireHandler(assessment))
        protocolHandlerRunner.addProtocolHandler(this.getNotificationHandler(assessment))
        protocolHandlerRunner.addProtocolHandler(this.getReminderHandler(assessment))
        protocolHandlerRunner.addProtocolHandler(
            this.getCompletedQuestionnaireHandler(assessment, previousTasks, prevTimezone)
        )
        return protocolHandlerRunner.runProtocolHandlers(assessment, user)
    }
}
