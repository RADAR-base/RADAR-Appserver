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

package org.radarbase.appserver.microservices.task.service.questionnaire.schedule

import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.ws.rs.core.Response
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.radarbase.appserver.microservices.contract.calls.NotificationServiceContract
import org.radarbase.appserver.microservices.contract.calls.ProjectServiceContract
import org.radarbase.appserver.microservices.contract.calls.ProtocolServiceContract
import org.radarbase.appserver.microservices.contract.calls.UserServiceContract
import org.radarbase.appserver.microservices.contract.exception.ProxyResponseException
import org.radarbase.appserver.microservices.contract.utils.Utils.deserializeDtoFromContract
import org.radarbase.appserver.microservices.core.dto.ProjectDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmNotificationDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmNotifications
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUserDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUsers
import org.radarbase.appserver.microservices.core.dto.protocol.Assessment
import org.radarbase.appserver.microservices.core.dto.protocol.AssessmentType
import org.radarbase.appserver.microservices.core.dto.protocol.Protocol
import org.radarbase.appserver.microservices.core.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.microservices.core.dto.questionnaire.Schedule
import org.radarbase.appserver.microservices.core.entity.Notification
import org.radarbase.appserver.microservices.core.entity.Task
import org.radarbase.appserver.microservices.core.entity.User
import org.radarbase.appserver.microservices.core.mapper.Mapper
import org.radarbase.appserver.microservices.core.service.TaskService
import org.radarbase.appserver.microservices.core.service.questionnaire.schedule.ScheduleGeneratorService
import org.radarbase.appserver.microservices.core.utils.Const.NOTIFICATION_MAPPER
import org.radarbase.appserver.microservices.core.utils.Const.USER_MAPPER
import org.radarbase.appserver.microservices.core.utils.checkInvalidDetails
import org.radarbase.appserver.microservices.core.utils.requireNotNullField
import org.radarbase.appserver.microservices.task.config.TaskServiceConfig
import org.radarbase.appserver.microservices.task.search.TaskSpecificationsBuilder
import org.radarbase.appserver.microservices.task.service.scheduling.SchedulingService
import org.radarbase.jersey.exception.HttpNotFoundException
import org.radarbase.jersey.service.AsyncCoroutineService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Timestamp
import java.time.Duration
import java.time.Instant

@Suppress("unused")
class QuestionnaireScheduleService @Inject constructor(
    private val scheduleGeneratorService: ScheduleGeneratorService,
    private val taskService: TaskService,
    @param:Named(USER_MAPPER) val userMapper: Mapper<FcmUserDto, User>,
    @param:Named(NOTIFICATION_MAPPER) val notificationMapper: Mapper<FcmNotificationDto, Notification>,
    schedulingService: SchedulingService,
    asyncService: AsyncCoroutineService,
    config: TaskServiceConfig,
) {
    private val subjectScheduleMap: HashMap<String, Schedule> = hashMapOf()

    private val protocolServiceUrl = config.contract.protocol
    private val projectServiceUrl = config.contract.project
    private val userServiceUrl = config.contract.user
    private val notificationServiceUrl = config.contract.notification

    private val scheduleGeneratorMutex = Mutex()
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
        scheduleGeneratorMutex.withLock {
            val subjectId: String? = user.subjectId
            checkNotNull(subjectId) { "Subject ID cannot be null in questionnaire scheduler service." }
            val protocol: Protocol? = try {
                ProtocolServiceContract.getProtocolForSubject(
                    requireNotNullField(user.projectId, "User's projectId"),
                    subjectId,
                    protocolServiceUrl,
                ).let {
                    deserializeDtoFromContract<Protocol>(it) {
                        "protocol_not_found ; No protocol found for user $subjectId and project ${user.projectId}"
                    }
                }
            } catch (ex: Exception) {
                null
            }

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
                val projectId = requireNotNullField(user.projectId, "User's projectId")
                val subjectId = requireNotNullField(user.subjectId, "User's subjectId")
                val notificationDtos = FcmNotifications(
                    notificationMapper.entitiesToDtos(notifications),
                )
                val reminderDtos = FcmNotifications(
                    notificationMapper.entitiesToDtos(reminders),
                )

                NotificationServiceContract.addBatchNotifications(
                    projectId,
                    subjectId,
                    true,
                    notificationDtos,
                    notificationServiceUrl,
                ).let {
                    if (it.status !in 200..299) {
                        throw ProxyResponseException(
                            Response.Status.fromStatusCode(it.status),
                            it.body?.decodeToString() ?: "Upstream sent an incorrect response",

                        )
                    }
                }

                NotificationServiceContract.addBatchNotifications(
                    projectId,
                    subjectId,
                    true,
                    reminderDtos,
                    notificationServiceUrl
                ).let {
                    if (it.status !in 200..299) {
                        throw ProxyResponseException(
                            Response.Status.fromStatusCode(it.status),
                            it.body?.decodeToString() ?: "Upstream sent an incorrect response",

                            )
                    }
                }
            }
    }

    suspend fun generateScheduleUsingProjectIdAndSubjectIdAndAssessment(
        projectId: String,
        subjectId: String,
        assessment: Assessment,
    ): Schedule {
        val user: User = subjectAndProjectExistsElseThrow(subjectId, projectId)
        val protocol: Protocol? = try {
            ProtocolServiceContract.getProtocolForSubject(
                requireNotNullField(user.projectId, "User's projectId"),
                subjectId,
                protocolServiceUrl,
            ).let {
                deserializeDtoFromContract<Protocol>(it) {
                    "protocol_not_found ; No protocol found for user $subjectId, and project ${user.projectId}"
                }
            }
        } catch (ex: Exception) {
            null
        }

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
        UserServiceContract.getAllUsers(userServiceUrl).let { users ->
            deserializeDtoFromContract<FcmUsers>(users) {
                "users_not_found ; No users found"
            }
        }.users
            .let {
                userMapper.dtosToEntities(it)
            }
            .also { users: List<User> ->
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
        return ProjectServiceContract.getProjectUsingProjectId(projectId, projectServiceUrl).let {
            deserializeDtoFromContract<ProjectDto>(it) {
                "project_not_found ; No project found for projectId $projectId"
            }
        }.let {
            UserServiceContract.getUserUsingProjectIdAndSubjectId(
                projectId,
                subjectId,
                userServiceUrl,
            ).let {
                deserializeDtoFromContract<FcmUserDto>(it) {
                    "user_not_found ; User with subjectId $subjectId not found. Please create a user first"
                }
            }.let {
                userMapper.dtoToEntity(it)
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
