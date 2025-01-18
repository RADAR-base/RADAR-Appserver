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
package org.radarbase.appserver.service.questionnaire.protocol

import org.radarbase.appserver.dto.protocol.Assessment
import org.radarbase.appserver.dto.protocol.RepeatQuestionnaire
import org.radarbase.appserver.dto.protocol.TimePeriod
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.entity.Task
import org.radarbase.appserver.entity.User
import java.time.Instant
import java.util.*
import java.util.stream.Collectors

class SimpleRepeatQuestionnaireHandler : ProtocolHandler {

    private val timeCalculatorService = TimeCalculatorService()
    private val taskGeneratorService = TaskGeneratorService()

    override fun handle(
        assessmentSchedule: AssessmentSchedule, assessment: Assessment, user: User
    ): AssessmentSchedule {
        val referenceTimestamp = assessmentSchedule.referenceTimestamps ?: return assessmentSchedule.also {
            it.tasks = emptyList()
        }
        val tasks = generateTasks(assessment, referenceTimestamp, user)
        assessmentSchedule.tasks = tasks
        return assessmentSchedule
    }

    private fun generateTasks(
        assessment: Assessment, referenceTimestamps: List<Instant>, user: User
    ): List<Task> {
        val timezone = TimeZone.getTimeZone(user.timezone)
        val repeatQuestionnaire: RepeatQuestionnaire? = assessment.protocol?.repeatQuestionnaire
        val repeatQuestionnaireUnit: String? = repeatQuestionnaire?.unit
        if (repeatQuestionnaireUnit == null) return emptyList()
        val unitsFromZero: List<Int> = repeatQuestionnaire.unitsFromZero ?: emptyList()
        val completionWindow = this.calculateCompletionWindow(assessment.protocol?.completionWindow)

        val tasks = referenceTimestamps.parallelStream().flatMap { referenceTimestamp: Instant ->
            val timePeriod = TimePeriod()
            timePeriod.unit = repeatQuestionnaireUnit
            unitsFromZero.parallelStream().map { unitFromZero: Int ->
                timePeriod.amount = unitFromZero
                val taskTime = timeCalculatorService.advanceRepeat(referenceTimestamp, timePeriod, timezone)
                taskGeneratorService.buildTask(assessment, taskTime, completionWindow).apply {
                    this.user = user
                }
            }
        }.collect(Collectors.toList())
        return tasks
    }

    private fun calculateCompletionWindow(completionWindow: TimePeriod?): Long {
        if (completionWindow == null) return DEFAULT_TASK_COMPLETION_WINDOW
        return timeCalculatorService.timePeriodToMillis(completionWindow)
    }

    companion object {
        private const val DEFAULT_TASK_COMPLETION_WINDOW = 86400000L
    }
}
