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

package org.radarbase.appserver.jersey.service.questionnaire_schedule

import jakarta.inject.Inject
import org.radarbase.appserver.jersey.dto.protocol.Assessment
import org.radarbase.appserver.jersey.dto.protocol.AssessmentType
import org.radarbase.appserver.jersey.dto.protocol.Protocol
import org.radarbase.appserver.jersey.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.jersey.dto.questionnaire.Schedule
import org.radarbase.appserver.jersey.entity.Notification
import org.radarbase.appserver.jersey.entity.Task
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.repository.ProjectRepository
import org.radarbase.appserver.jersey.repository.UserRepository
import org.radarbase.appserver.jersey.search.TaskSpecificationsBuilder
import org.radarbase.appserver.jersey.service.FcmNotificationService
import org.radarbase.appserver.jersey.service.TaskService
import org.radarbase.appserver.jersey.service.github.protocol.ProtocolGenerator
import org.radarbase.appserver.jersey.service.scheduling.SchedulingService
import org.radarbase.appserver.jersey.utils.checkInvalidDetails
import org.radarbase.appserver.jersey.utils.checkPresence
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.service.AsyncCoroutineService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Timestamp
import java.time.Duration
import java.time.Instant

@Suppress("unused")
class QuestionnaireScheduleService @Inject constructor(
    private val protocolGenerator: ProtocolGenerator,
    private val scheduleGeneratorService: ScheduleGeneratorService,
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val taskService: TaskService,
    private val notificationService: FcmNotificationService,
    schedulingService: SchedulingService,
    asyncService: AsyncCoroutineService,
) {
    private val subjectScheduleMap: HashMap<String, Schedule> = hashMapOf()

    private val cleanScheduleRef: SchedulingService.RepeatReference = schedulingService.repeat(
        Duration.ofMillis(3_600_000),
        Duration.ofMillis(5_000),
    ) {
        asyncService.runBlocking {
            generateAllSchedules()
        }
    }

    suspend fun getTasksUsingProjectIdAndSubjectId(subjectId: String, projectId: String): List<Task> {
        return getTasksForUser(subjectAndProjectExistsElseThrow(subjectId, projectId))
    }

    suspend fun getTasksByTypeUsingProjectIdAndSubjectId(
        projectId: String,
        subjectId: String,
        type: AssessmentType,
        search: String,
    ): List<Task> {
        getSearchBuilder(projectId, subjectId, type, search).build().also { spec ->
            return this.taskService.getTasksBySpecification(spec)
        }
    }

    suspend fun getTasksForDateUsingProjectIdAndSubjectId(
        subjectId: String,
        projectId: String,
        startTime: Instant,
        endTime: Instant,
    ): List<Task> {
        val user: User = subjectAndProjectExistsElseThrow(subjectId, projectId)
        val tasks: MutableList<Task> = this.getTasksForUser(user).toMutableList()

        tasks.removeIf { task ->
            val taskTime: Timestamp? = task.timestamp
            checkNotNull(taskTime) { "Task timestamp cannot is null in questionnaire scheduler service." }

            val completionWindow: Long? = task.completionWindow
            checkNotNull(completionWindow) { "Task completion window is null in questionnaire scheduler service." }

            taskTime.toInstant().let { taskTimeInstant ->
                taskTimeInstant.plusMillis(completionWindow).isBefore(startTime) || taskTimeInstant.isAfter(endTime)
            }
        }

        return tasks
    }

    suspend fun generateScheduleUsingProjectIdAndSubjectId(subjectId: String, projectId: String): Schedule {
        return subjectAndProjectExistsElseThrow(subjectId, projectId).run {
            generateScheduleForUser(this)
        }
    }

    suspend fun generateScheduleForUser(user: User): Schedule {
        val subjectId: String? = user.subjectId
        checkNotNull(subjectId) { "Subject ID cannot be null in questionnaire scheduler service." }
        val protocol: Protocol? = protocolGenerator.getProtocolForSubject(subjectId)

        val newSchedule: Schedule = protocol?.let {
            val prevSchedule: Schedule = getScheduleForSubject(subjectId)
            val prevTimeZone: String = prevSchedule.timezone ?: checkNotNull(user.timezone) {
                "User timezone cannot be null in questionnaire scheduler service."
            }

            if ((prevSchedule.version != it.version) || (prevTimeZone != user.timezone)) {
                removeScheduleForUser(user)
            }
            scheduleGeneratorService.generateScheduleForUser(user, it, prevSchedule)
        } ?: Schedule()

        return newSchedule.also {
            subjectScheduleMap[subjectId] = it
            saveTasksAndNotifications(user, newSchedule.assessmentSchedules)
        }
    }

    suspend fun saveTasksAndNotifications(user: User, assessmentSchedules: List<AssessmentSchedule?>) {
        assessmentSchedules.filterNotNull()
            .filter(AssessmentSchedule::hasTasks)
            .forEach {
                val (tasks, notifications, reminders) = nonNullTasksNotificationsAndReminders(
                    it.tasks,
                    it.notifications,
                    it.reminders,
                )

                taskService.addTasks(tasks, user)
                notificationService.addNotifications(notifications, user)
                notificationService.addNotifications(reminders, user)
            }
    }

    suspend fun generateScheduleUsingProjectIdAndSubjectIdAndAssessment(
        projectId: String,
        subjectId: String,
        assessment: Assessment,
    ): Schedule {
        val user: User = subjectAndProjectExistsElseThrow(subjectId, projectId)
        val protocol: Protocol? = protocolGenerator.getProtocolForSubject(subjectId)

        checkInvalidDetails<HttpNotFoundException>(
            { protocol == null || !protocol.hasAssessment(assessment.name) },
            { "Assessment not found in protocol. Add assessment to protocol first" },
        )

        val userTimeZone = user.timezone
        checkNotNull(userTimeZone) { "User timezone cannot be null in questionnaire scheduler service." }

        val schedule = getScheduleForSubject(subjectId)
        val assessmentSchedule = scheduleGeneratorService.generateSingleAssessmentSchedule(
            assessment,
            user,
            emptyList(),
            userTimeZone,
        )

        schedule.addAssessmentSchedule(assessmentSchedule)

        saveTasksAndNotifications(user, listOf(assessmentSchedule))

        return schedule
    }

    suspend fun generateAllSchedules() {
        logger.info("Generating all schedules")
        userRepository.findAll().also { users: List<User> ->
            users.forEach {
                generateScheduleForUser(it)
            }
        }
    }

    fun getScheduleForSubject(subjectId: String): Schedule {
        val schedule: Schedule? = subjectScheduleMap[subjectId]
        return schedule ?: Schedule()
    }

    suspend fun getTasksForUser(user: User): List<Task> {
        return taskService.getTasksByUser(user)
    }

    suspend fun subjectAndProjectExistsElseThrow(subjectId: String, projectId: String): User {
        return checkPresence(this.projectRepository.findByProjectId(projectId), "project_not_found") {
            "Project with projectId $projectId not found. Please create the project first."
        }.let { project ->
            checkPresence(
                this.userRepository.findBySubjectIdAndProjectId(
                    subjectId,
                    checkNotNull(project.id) { "Project ID cannot be null." },
                ),
                "user_not_found",
            ) {
                "User with subjectId $subjectId not found. Please create the user first."
            }
        }
    }

    suspend fun removeScheduleForUser(user: User) {
        val userId = checkNotNull(user.id) { "User ID cannot be null." }
        taskService.deleteTasksByUserId(userId)
    }

    suspend fun removeScheduleForUserUsingSubjectIdAndType(
        projectId: String,
        subjectId: String,
        type: AssessmentType,
        search: String,
    ) {
        getSearchBuilder(projectId, subjectId, type, search).build().also { taskSpecification ->
            taskService.deleteTasksBySpecification(taskSpecification)
        }
    }

    suspend fun getSearchBuilder(
        projectId: String,
        subjectId: String,
        type: AssessmentType?,
        search: String?,
    ): TaskSpecificationsBuilder {
        val builder = TaskSpecificationsBuilder()

        subjectAndProjectExistsElseThrow(subjectId, projectId).also { user ->
            builder.with("user", ":", user)
        }

        if (type != null && type != AssessmentType.ALL) {
            builder.with("type", ":", type)
        }
        if (!search.isNullOrBlank()) {
            search.split(COMMA_PATTERN).forEach { searchTerm: String ->
                TASK_SEARCH_PATTERN.matchEntire(searchTerm.trim())?.also { matcher ->
                    val (field, operator, value) = matcher.destructured
                    builder.with(field, operator, value)
                }
            }
        }
        return builder
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(QuestionnaireScheduleService::class.java)

        private val TASK_SEARCH_PATTERN = Regex("(\\w+)([:<>])(\\w+)")
        private val COMMA_PATTERN = Regex(",")

        fun nonNullTasksNotificationsAndReminders(
            tasks: List<Task>?,
            notifications: List<Notification>?,
            reminders: List<Notification>?,
        ): Triple<List<Task>, List<Notification>, List<Notification>> {
            val nonNullTasks = requireNotNull(tasks) { "Tasks cannot be null" }
            val nonNullNotifications = requireNotNull(notifications) { "Notifications cannot be null" }
            val nonNullReminders = requireNotNull(reminders) { "Reminders cannot be null" }

            return Triple(nonNullTasks, nonNullNotifications, nonNullReminders)
        }
    }
}
