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
import org.radarbase.appserver.dto.protocol.TimePeriod
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.entity.Task
import org.radarbase.appserver.entity.User
import java.time.Instant
import java.util.*

class RandomRepeatQuestionnaireHandler {

    private val defaultTaskCompletionWindow = 86_400_000L
    private val timeCalculatorService = TimeCalculatorService()
    private val taskGeneratorService = TaskGeneratorService()

    fun handle(
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
