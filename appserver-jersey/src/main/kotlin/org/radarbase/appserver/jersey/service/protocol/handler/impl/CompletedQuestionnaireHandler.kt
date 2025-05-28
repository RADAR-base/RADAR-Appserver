/*
 * Copyright 2025 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.appserver.jersey.service.protocol.handler.impl

import kotlinx.coroutines.Dispatchers
import org.radarbase.appserver.jersey.dto.protocol.Assessment
import org.radarbase.appserver.jersey.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.jersey.entity.Task
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.event.state.TaskState
import org.radarbase.appserver.jersey.service.protocol.handler.ProtocolHandler
import org.radarbase.appserver.jersey.utils.checkPresence
import org.radarbase.appserver.jersey.utils.mapParallel
import java.sql.Timestamp
import java.util.TimeZone

open class CompletedQuestionnaireHandler(
    private val prevTasks: List<Task>,
    private val prevTimezone: String
) : ProtocolHandler {

    override suspend fun handle(
        assessmentSchedule: AssessmentSchedule,
        assessment: Assessment,
        user: User
    ): AssessmentSchedule {
        val currentTimezone = checkPresence(user.timezone, "invalid_user_details") {
            "User's timezone can't be null in completed questionnaire handler"
        }

        val currentTask = assessmentSchedule.tasks ?: emptyList()

        markTasksAsCompleted(currentTask, prevTasks, currentTimezone, prevTimezone)
        return assessmentSchedule
    }

    suspend fun markTasksAsCompleted(
        currentTasks: List<Task>,
        previousTasks: List<Task>,
        currentTimezone: String,
        prevTimezone: String
    ): List<Task> {
        currentTasks.mapParallel(Dispatchers.Default) { newTask ->
            val matching = if (currentTimezone != prevTimezone) {
                val taskTimestamp = newTask.timestamp
                requireNotNull(taskTimestamp) { "Task timestamp cannot be null" }

                val prevTimestamp = getPreviousTimezoneEquivalent(taskTimestamp, currentTimezone, prevTimezone)

                previousTasks.parallelStream().filter { areMatchingTasks(newTask, it, prevTimestamp) }.findFirst()
            } else {
                previousTasks.parallelStream().filter { areMatchingTasks(newTask, it) }.findFirst()
            }

            matching.ifPresent { matchingTask ->
                if (matchingTask.status == TaskState.COMPLETED) {
                    newTask.apply {
                        completed = true
                        timeCompleted = matchingTask.timeCompleted
                        status = TaskState.COMPLETED
                    }
                }
            }
        }
        return currentTasks
    }

    private fun areMatchingTasks(a: Task, b: Task): Boolean {
        return a.timestamp == b.timestamp && a.name == b.name
    }

    private fun areMatchingTasks(a: Task, b: Task, bTimestamp: Timestamp): Boolean {
        return a.timestamp == bTimestamp && a.name == b.name
    }

    private fun getPreviousTimezoneEquivalent(
        taskTimestamp: Timestamp,
        newTimezone: String,
        prevTimezone: String
    ): Timestamp {
        val timezoneDiff = TimeZone.getTimeZone(newTimezone).rawOffset - TimeZone.getTimeZone(prevTimezone).rawOffset
        return Timestamp(taskTimestamp.time + timezoneDiff)
    }
}
