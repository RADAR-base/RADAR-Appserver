package org.radarbase.appserver.jersey.service.protocol.handler.impl

import org.radarbase.appserver.jersey.dto.protocol.Assessment
import org.radarbase.appserver.jersey.dto.protocol.TimePeriod
import org.radarbase.appserver.jersey.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.jersey.entity.Task
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.service.protocol.handler.ProtocolHandler
import org.radarbase.appserver.jersey.service.questionnaire_schedule.task.TaskGeneratorService
import org.radarbase.appserver.jersey.service.protocol.time.TimeCalculatorService
import java.time.Instant
import java.util.TimeZone


class RandomRepeatQuestionnaireHandler: ProtocolHandler {
    private val defaultTaskCompletionWindow = 86_400_000L
    private val timeCalculatorService = TimeCalculatorService()
    private val taskGeneratorService = TaskGeneratorService()

    override suspend fun handle(
        assessmentSchedule: AssessmentSchedule,
        assessment: Assessment,
        user: User
    ): AssessmentSchedule {

        val referenceTimestamps = assessmentSchedule.referenceTimestamps ?: return assessmentSchedule.apply {
            tasks = emptyList()
        }

        val tasks = generateTasks(
            assessment,
            referenceTimestamps,
            user
        )
        assessmentSchedule.tasks = tasks
        return assessmentSchedule
    }

    private fun generateTasks(
        assessment: Assessment,
        referenceTimestamps: List<Instant>,
        user: User
    ): List<Task> {
        val timezone = TimeZone.getTimeZone(user.timezone)
        val repeatQuestionnaire = assessment.protocol?.repeatQuestionnaire
        val randomUnitsFromZeroBetween = repeatQuestionnaire?.randomUnitsFromZeroBetween ?: return emptyList()
        val completionWindow = calculateCompletionWindow(assessment.protocol?.completionWindow)

        val tasks = mutableListOf<Task>()
        for (referenceTimestamp in referenceTimestamps) {
            val timePeriod = TimePeriod().apply { unit = repeatQuestionnaire.unit }
            for (range in randomUnitsFromZeroBetween) {
                timePeriod.amount = getRandomAmountInRange(range)
                val taskTime = timeCalculatorService.advanceRepeat(referenceTimestamp, timePeriod, timezone)
                val task = taskGeneratorService.buildTask(assessment, taskTime, completionWindow).apply {
                    this.user = user
                }
                tasks.add(task)
            }
        }
        return tasks
    }

    private fun getRandomAmountInRange(range: Array<Int>): Int {
        val (lowerLimit, upperLimit) = range
        return (lowerLimit .. upperLimit).random()
    }

    private fun calculateCompletionWindow(completionWindow: TimePeriod?): Long {
        return completionWindow?.let {
            timeCalculatorService.timePeriodToMillis(it)
        } ?: defaultTaskCompletionWindow
    }
}
