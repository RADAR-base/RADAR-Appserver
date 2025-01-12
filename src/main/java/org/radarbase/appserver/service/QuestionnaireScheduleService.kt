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

package org.radarbase.appserver.service

import org.radarbase.appserver.dto.protocol.Assessment
import org.radarbase.appserver.dto.protocol.AssessmentType
import org.radarbase.appserver.dto.protocol.Protocol
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.dto.questionnaire.Schedule
import org.radarbase.appserver.entity.Task
import org.radarbase.appserver.entity.User
import org.radarbase.appserver.exception.NotFoundException
import org.radarbase.appserver.repository.ProjectRepository
import org.radarbase.appserver.repository.UserRepository
import org.radarbase.appserver.search.TaskSpecificationsBuilder
import org.radarbase.appserver.service.questionnaire.protocol.ProtocolGenerator
import org.radarbase.appserver.service.questionnaire.schedule.QuestionnaireScheduleGeneratorService
import org.radarbase.appserver.util.checkInvalidDetails
import org.radarbase.appserver.util.checkPresence
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.data.jpa.domain.Specification
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.Instant

@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class QuestionnaireScheduleService(
    private val protocolGenerator: ProtocolGenerator,
    private val userRepository: UserRepository,
    private val scheduleGeneratorService: QuestionnaireScheduleGeneratorService,
    private val projectRepository: ProjectRepository,
    private val taskService: TaskService,
    private val notificationService: FcmNotificationService
) {
    private val subjectScheduleMap: MutableMap<String?, Schedule> = hashMapOf()

    @Transactional
    fun getTasksUsingProjectIdAndSubjectId(subjectId: String?, projectId: String?): List<Task> {
        return getTasksForUser(subjectAndProjectExistsElseThrow(subjectId, projectId))
    }

    @Transactional
    fun getTasksByTypeUsingProjectIdAndSubjectId(
        projectId: String?,
        subjectId: String?,
        type: AssessmentType?,
        search: String?
    ): List<Task> {
        getSearchBuilder(projectId, subjectId, type, search).build().also { spec ->
            return this.taskService.getTasksBySpecification(spec)
        }

        /** Not sure about it, Keeping it (as it is) from java to refactored kotlin file, just in case needed in future */
//                if (type != AssessmentType.ALL) {
//            return getTasksUsingProjectIdAndSubjectId(projectId, subjectId);
//        } else {
//            User user = subjectAndProjectExistElseThrow(subjectId, projectId);
//            return this.taskRepository.findByUserIdAndType(user.getId(), type);
//        }
    }

    @Transactional
    fun getTasksForDateUsingProjectIdAndSubjectId(
        subjectId: String?,
        projectId: String?,
        startTime: Instant?,
        endTime: Instant?
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

    @Transactional
    fun generateScheduleUsingProjectIdAndSubjectId(subjectId: String?, projectId: String?): Schedule {
        return subjectAndProjectExistsElseThrow(subjectId, projectId).run {
            generateScheduleForUser(this)
        }
    }

    @Transactional
    fun generateScheduleForUser(user: User): Schedule {
        val subjectId: String? = user.subjectId
        checkNotNull(subjectId) { "Subject ID cannot be null in questionnaire scheduler service." }

        val protocol: Protocol? = protocolGenerator.getProtocolForSubject(subjectId)
        val newSchedule: Schedule
        if (protocol == null) {
            newSchedule = Schedule()
        } else {
            val prevSchedule: Schedule = getScheduleForSubject(subjectId)
            val prevTimeZone: String = prevSchedule.timezone ?: user.timezone!!

            if ((prevSchedule.version != protocol.version) || (prevTimeZone != user.timezone)) {
                removeScheduleForUser(user)
            }
            newSchedule = scheduleGeneratorService.generateScheduleForUser(user, protocol, prevSchedule)
        }
        subjectScheduleMap[user.subjectId] = newSchedule
        saveTasksAndNotifications(user, newSchedule.assessmentSchedules)
        return newSchedule
    }

    fun saveTasksAndNotifications(user: User, assessmentSchedules: List<AssessmentSchedule?>) {
        assessmentSchedules.filterNotNull()
            .filter(AssessmentSchedule::hasTasks)
            .forEach {
                taskService.addTasks(it.tasks, user)
                notificationService.addNotifications(it.notifications, user)
                notificationService.addNotifications(it.reminders, user)
            }
    }

    fun generateScheduleUsingProjectIdAndSubjectIdAndAssessment(
        projectId: String,
        subjectId: String,
        assessment: Assessment
    ): Schedule {

        val user: User = subjectAndProjectExistsElseThrow(subjectId, projectId)
        val protocol: Protocol? = protocolGenerator.getProtocolForSubject(subjectId)

        checkInvalidDetails<NotFoundException>(
            {  protocol == null || !protocol.hasAssessment(assessment.name) },
            { "Assessment not found in protocol. Add assessment to protocol first" }
        )

        val userTimeZone = user.timezone
        checkNotNull(userTimeZone) { "User timezone cannot be null in questionnaire scheduler service." }

        val schedule = getScheduleForSubject(subjectId)
        val assessmentSchedule = scheduleGeneratorService.generateSingleAssessmentSchedule(
            assessment,
            user,
            emptyList(),
            userTimeZone
        )

        schedule.addAssessmentSchedule(assessmentSchedule)

        saveTasksAndNotifications(user, listOf(assessmentSchedule))

        return schedule
    }

    @Scheduled(fixedRate = 3_600_000)
    fun generateAllSchedules() {
        logger.info("Generating all schedules")
        userRepository.findAll().also { users: List<User> ->
            users.forEach(this::generateScheduleForUser)
        }
    }

    fun getScheduleForSubject(subjectId: String): Schedule {
        val schedule: Schedule? = subjectScheduleMap[subjectId]
        return schedule ?: Schedule()
    }

    @Transactional
    fun getTasksForUser(user: User): List<Task> {
        return taskService.getTasksByUser(user)
    }

    fun subjectAndProjectExistsElseThrow(subjectId: String?, projectId: String?): User {
        return checkPresence(this.projectRepository.findByProjectId(projectId)) {
            "Project with projectId $projectId not found. Please create the project first."
        }.let { project ->
            checkPresence(this.userRepository.findBySubjectIdAndProjectId(subjectId, project.id)) {
                "User with subjectId $subjectId not found. Please create the user first."
            }
        }
    }

    fun removeScheduleForUser(user: User) {
        taskService.deleteTasksByUserId(user.id)
    }

    fun removeScheduleForUserUsingSubjectIdAndType(projectId: String, subjectId: String, type: AssessmentType, search: String) {
        val specification: Specification<Task>? = getSearchBuilder(projectId, subjectId, type, search).build()

        taskService.deleteTasksBySpecification(specification)
    }

    fun getSearchBuilder(
        projectId: String?,
        subjectId: String?,
        type: AssessmentType?,
        search: String?
    ): TaskSpecificationsBuilder {
        val builder = TaskSpecificationsBuilder()

        subjectAndProjectExistsElseThrow(subjectId, projectId).also { user ->
            builder.with("user", ":", user)
        }

        if (type != AssessmentType.ALL && type != null) {
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
    }
}