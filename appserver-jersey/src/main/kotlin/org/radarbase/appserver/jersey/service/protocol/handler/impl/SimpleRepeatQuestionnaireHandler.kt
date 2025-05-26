package org.radarbase.appserver.jersey.service.protocol.handler.impl

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.radarbase.appserver.jersey.dto.protocol.Assessment
import org.radarbase.appserver.jersey.dto.protocol.RepeatQuestionnaire
import org.radarbase.appserver.jersey.dto.protocol.TimePeriod
import org.radarbase.appserver.jersey.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.jersey.entity.Task
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.service.protocol.handler.ProtocolHandler
import org.radarbase.appserver.jersey.service.protocol.task.TaskGeneratorService
import org.radarbase.appserver.jersey.service.protocol.time.TimeCalculatorService
import org.radarbase.appserver.jersey.utils.flatMapParallel
import java.time.Instant
import java.util.TimeZone

class SimpleRepeatQuestionnaireHandler : ProtocolHandler {
    private val timeCalculatorService = TimeCalculatorService()
    private val taskGeneratorService = TaskGeneratorService()

    override suspend fun handle(
        assessmentSchedule: AssessmentSchedule, assessment: Assessment, user: User,
    ): AssessmentSchedule {
        val referenceTimestamp = assessmentSchedule.referenceTimestamps
            ?: return assessmentSchedule.also { it.tasks = emptyList() }

        generateTasks(assessment, referenceTimestamp, user).let {
            assessmentSchedule.apply {
                tasks = it
            }
        }
        return assessmentSchedule
    }

    private suspend fun generateTasks(
        assessment: Assessment, referenceTimestamps: List<Instant>, user: User,
    ): List<Task> = coroutineScope {
        val timezone = TimeZone.getTimeZone(user.timezone)

        val repeatQuestionnaire: RepeatQuestionnaire = assessment.protocol?.repeatQuestionnaire ?: return@coroutineScope emptyList()
        val repeatQuestionnaireUnit: String? = repeatQuestionnaire.unit ?: return@coroutineScope emptyList()
        val unitsFromZero: List<Int> = repeatQuestionnaire.unitsFromZero.orEmpty()

        val completionWindow = this@SimpleRepeatQuestionnaireHandler.calculateCompletionWindow(
            assessment.protocol?.completionWindow
        )

        referenceTimestamps.flatMapParallel { referenceTimestamp: Instant ->
            unitsFromZero.map { unitFromZero: Int ->
                async {
                    val period = TimePeriod().apply {
                        unit = repeatQuestionnaireUnit
                        amount = unitFromZero
                    }
                    val taskTime = timeCalculatorService.advanceRepeat(referenceTimestamp, period, timezone)
                    taskGeneratorService.buildTask(assessment, taskTime, completionWindow).apply {
                        this.user = user
                    }
                }
            }.awaitAll()
        }
    }

    private fun calculateCompletionWindow(completionWindow: TimePeriod?): Long {
        if (completionWindow == null) return DEFAULT_TASK_COMPLETION_WINDOW
        return timeCalculatorService.timePeriodToMillis(completionWindow)
    }

    companion object {
        private const val DEFAULT_TASK_COMPLETION_WINDOW = 86400000L
    }
}
