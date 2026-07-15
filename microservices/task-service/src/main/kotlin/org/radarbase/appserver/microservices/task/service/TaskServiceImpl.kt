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

package org.radarbase.appserver.microservices.task.service

import com.google.common.eventbus.EventBus
import jakarta.inject.Inject
import jakarta.inject.Named
import org.radarbase.appserver.microservices.contract.calls.UserServiceContract
import org.radarbase.appserver.microservices.contract.utils.Utils.deserializeDtoFromContract
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUserDto
import org.radarbase.appserver.microservices.core.dto.protocol.AssessmentType
import org.radarbase.appserver.microservices.core.entity.Task
import org.radarbase.appserver.microservices.core.entity.User
import org.radarbase.appserver.microservices.core.event.state.TaskState
import org.radarbase.appserver.microservices.core.event.state.dto.TaskStateEventDto
import org.radarbase.appserver.microservices.core.exception.AlreadyExistsException
import org.radarbase.appserver.microservices.core.mapper.Mapper
import org.radarbase.appserver.microservices.core.repository.TaskRepository
import org.radarbase.appserver.microservices.core.search.QuerySpecification
import org.radarbase.appserver.microservices.core.service.TaskService
import org.radarbase.appserver.microservices.core.utils.Const.USER_MAPPER
import org.radarbase.appserver.microservices.core.utils.checkPresence
import org.radarbase.appserver.microservices.core.utils.requireNotNullField
import org.radarbase.appserver.microservices.task.config.TaskServiceConfig
import java.sql.Timestamp
import java.time.Instant

@Suppress("unused")
class TaskServiceImpl @Inject constructor(
    private val taskRepository: TaskRepository,
    private val eventPublisher: EventBus,
    @param:Named(USER_MAPPER) val userMapper: Mapper<FcmUserDto, User>,
    config: TaskServiceConfig,
) : TaskService {
    private val userServiceUrl = config.contract.user

    override suspend fun getAllTasks(): List<Task> {
        return taskRepository.findAll()
    }

    override suspend fun getTaskById(id: Long): Task {
        val task = taskRepository.find(id)

        return checkPresence(task, "task_not_found") {
            "Task not found with id $id"
        }
    }

    override suspend fun getTasksBySubjectId(subjectId: String): List<Task> {
        val user = getUserBySubjectId(subjectId)
        checkPresence(user, "user_not_found") {
            INVALID_SUBJECT_ID_MESSAGE
        }

        return taskRepository.findByUserId(user.let(::nonNullUserId))
    }

    override suspend fun getTasksBySubjectIdAndType(subjectId: String, type: AssessmentType): List<Task> {
        val user = getUserBySubjectId(subjectId)
        checkPresence(user, "user_not_found") {
            INVALID_SUBJECT_ID_MESSAGE
        }

        return taskRepository.findByUserIdAndType(nonNullUserId(user), type)
    }

    override suspend fun getTasksByUser(user: User): List<Task> {
        return taskRepository.findByUserId(nonNullUserId(user))
    }

    override suspend fun getTasksBySpecification(spec: QuerySpecification<Task>): List<Task> {
        return taskRepository.findAll(spec)
    }

    override suspend fun deleteTasksBySpecification(spec: QuerySpecification<Task>) {
        val tasks = taskRepository.findAll(spec)
        taskRepository.deleteAll(tasks)
    }

    override suspend fun deleteTasksByUserId(userId: Long) {
        taskRepository.deleteByUserId(userId)
    }

    override suspend fun addTask(task: Task): Task {
        val (user, taskName, taskTimestamp) = validateUserTaskNameAndTaskTimestamp(task)
        val alreadyExists = this.taskRepository.existsByUserIdAndNameAndTimestamp(
            nonNullUserId(user),
            taskName,
            taskTimestamp,
        )

        if (!alreadyExists) {
            val saved = this.taskRepository.add(task)
            val userMetrics = checkNotNull(user.usermetrics) { "User metrics cannot be null" }
            userMetrics.lastOpened = Instant.now()
            val projectId = requireNotNullField(user.projectId, "User projectId")
            val subjectId = requireNotNullField(user.subjectId, "User subjectId")

            UserServiceContract.updateUser(
                FcmUserDto(user),
                projectId,
                subjectId,
                false,
                userServiceUrl,
            ).also {
                deserializeDtoFromContract<FcmUserDto>(it) { "user_not_found ; user with id ${user.id} not found" }
            }

            val taskCreationTimestamp = checkNotNull(saved.createdAt) { "Task creation timestamp cannot be null" }
            addTaskStateEvent(saved, TaskState.ADDED, taskCreationTimestamp.toInstant())
            return saved
        } else {
            throw AlreadyExistsException(
                "task_already_exists",
                "The Task Already exists. Please Use update endpoint",
            )
        }
    }

    override suspend fun addTasks(tasks: List<Task>, user: User): List<Task> {
        val newTasks = tasks.filter { task ->
            val taskName = checkNotNull(task.name) { "Task name cannot be null" }
            val taskTimestamp = checkNotNull(task.timestamp) { "Task timestamp cannot be null" }

            !this.taskRepository.existsByUserIdAndNameAndTimestamp(
                nonNullUserId(user),
                taskName,
                taskTimestamp,
            )
        }

        val saved = newTasks.map { task ->
            taskRepository.add(task)
        }
        saved.forEach { t ->
            val taskCreationTimestamp = checkNotNull(t.createdAt) { "Task creation timestamp cannot be null" }
            addTaskStateEvent(t, TaskState.ADDED, taskCreationTimestamp.toInstant())
        }

        return saved
    }

    private fun addTaskStateEvent(t: Task?, @Suppress("SameParameterValue") state: TaskState, time: Instant) {
        val taskStateEventDto = TaskStateEventDto(t, state, null, time)
        eventPublisher.post(taskStateEventDto)
    }

    override suspend fun updateTaskStatus(oldTask: Task, state: TaskState): Task? {
        val (user, taskName, taskTimestamp) = validateUserTaskNameAndTaskTimestamp(oldTask)

        val doesntExists = !this.taskRepository.existsByUserIdAndNameAndTimestamp(
            nonNullUserId(user),
            taskName,
            taskTimestamp,
        )

        if (doesntExists) {
            "The Task ${oldTask.id} does not exist to set to state $state  Please Use add endpoint"
        }

        if (state == TaskState.COMPLETED) {
            oldTask.completed = true
            oldTask.timeCompleted = Timestamp.from(Instant.now())
        }
        oldTask.status = state
        return this.taskRepository.update(oldTask)
    }

    private suspend fun getUserBySubjectId(subjectId: String): FcmUserDto {
        return deserializeDtoFromContract<FcmUserDto>(
            UserServiceContract.getUserUsingSubjectId(subjectId, userServiceUrl),
        ) {
            "user_not_found ; user with subjectId $subjectId not found"
        }
    }

    private suspend fun validateUserTaskNameAndTaskTimestamp(task: Task): Triple<User, String, Timestamp> {
        val userId = requireNotNullField(task.userId, "Task user")
        val userDto = deserializeDtoFromContract<FcmUserDto>(
            UserServiceContract.getUserUsingId(userId, userServiceUrl),
        ) {
            "user_not_found ; user with id $userId not found"
        }

        val user = userMapper.dtoToEntity(userDto).apply {
            usermetrics?.also {
                it.user = this
            }
        }

        val taskName = checkNotNull(task.name) { "Task name cannot be null" }
        val taskTimestamp = checkNotNull(task.timestamp) { "Task timestamp cannot be null" }

        return Triple(user, taskName, taskTimestamp)
    }


    companion object {
        private const val INVALID_SUBJECT_ID_MESSAGE =
            "The supplied Subject ID is invalid. No user found. Please Create a User First."

        fun nonNullUserId(user: FcmUserDto): Long = checkNotNull(user.id) {
            "User id cannot be null"
        }

        fun nonNullUserId(user: User): Long = checkNotNull(user.id) {
            "User id cannot be null"
        }

    }
}
