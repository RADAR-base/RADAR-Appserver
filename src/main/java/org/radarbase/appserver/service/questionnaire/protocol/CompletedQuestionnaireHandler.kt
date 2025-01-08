package org.radarbase.appserver.service.questionnaire.protocol

import org.radarbase.appserver.dto.protocol.Assessment
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.entity.Task
import org.radarbase.appserver.entity.User
import org.radarbase.appserver.event.state.TaskState
import org.radarbase.appserver.util.checkPresence
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.util.TimeZone

open class CompletedQuestionnaireHandler(
    private val prevTasks: List<Task>,
    private val prevTimezone: String
) : ProtocolHandler {

    override fun handle(
        assessmentSchedule: AssessmentSchedule,
        assessment: Assessment,
        user: User
    ): AssessmentSchedule {
        val currentTimezone = checkPresence(user.timezone) {
            "User's timezone can't be null in completed questionnaire handler"
        }

        val currentTask = assessmentSchedule.tasks ?: emptyList()

        markTasksAsCompleted(currentTask, prevTasks, currentTimezone, prevTimezone)
        return assessmentSchedule
    }

    @Transactional
    open fun markTasksAsCompleted(
        currentTasks: List<Task>,
        previousTasks: List<Task>,
        currentTimezone: String,
        prevTimezone: String
    ): List<Task> {
        currentTasks.parallelStream().forEach { newTask ->
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
