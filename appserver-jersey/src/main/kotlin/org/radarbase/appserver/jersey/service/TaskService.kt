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

package org.radarbase.appserver.jersey.service

import com.google.common.eventbus.EventBus
import org.radarbase.appserver.jersey.dto.protocol.AssessmentType
import org.radarbase.appserver.jersey.entity.Task
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.event.state.TaskState
import org.radarbase.appserver.jersey.event.state.dto.TaskStateEventDto
import org.radarbase.appserver.jersey.exception.AlreadyExistsException
import org.radarbase.appserver.jersey.repository.TaskRepository
import org.radarbase.appserver.jersey.repository.UserRepository
import org.radarbase.appserver.jersey.search.QuerySpecification
import org.radarbase.appserver.jersey.utils.checkPresence
import java.sql.Timestamp
import java.time.Instant

class TaskService(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val eventPublisher: EventBus,
) {
    suspend fun getAllProjects(): List<Task> {
        return taskRepository.findAll()
    }

    suspend fun getTaskById(id: Long): Task {
        val task = taskRepository.find(id)

        return checkPresence(task, "task_not_found") {
            "Task not found with id $id"
        }
    }

    suspend fun getTasksBySubjectId(subjectId: String): List<Task> {
        val user = this.userRepository.findBySubjectId(subjectId)
        checkPresence(user, "user_not_found") {
            INVALID_SUBJECT_ID_MESSAGE
        }
        val userId = user.id
        checkNotNull(userId) {
            "User id is null when fetching tasks"
        }
        return taskRepository.findByUserId(userId)
    }

    suspend fun getTasksBySubjectIdAndType(subjectId: String, type: AssessmentType): List<Task> {
        val user = this.userRepository.findBySubjectId(subjectId)
        checkPresence(user, "user_not_found") {
            INVALID_SUBJECT_ID_MESSAGE
        }
        return taskRepository.findByUserIdAndType(user.id!!, type)
    }

    suspend fun getTasksByUser(user: User): List<Task> {
        return taskRepository.findByUserId(user.id!!)
    }

    suspend fun getTasksBySpecification(spec: QuerySpecification<Task>): List<Task> {
        return taskRepository.findAll(spec)
    }

    suspend fun deleteTasksBySpecification(spec: QuerySpecification<Task>) {
        val tasks = taskRepository.findAll(spec)
        taskRepository.deleteAll(tasks)
    }

    suspend fun deleteTasksByUserId(userId: Long) {
        taskRepository.deleteByUserId(userId)
    }

    suspend fun addTask(task: Task): Task {
        val user = task.user

        val alreadyExists =
            this.taskRepository.existsByUserIdAndNameAndTimestamp(user!!.id!!, task.name!!, task.timestamp!!)

        if (!alreadyExists) {
            val saved = this.taskRepository.add(task)
            user.usermetrics!!.lastOpened = Instant.now()
            this.userRepository.update(user)
            addTaskStateEvent(saved, TaskState.ADDED, saved.createdAt)
            return saved
        } else throw AlreadyExistsException(
            "task_already_exists", "The Task Already exists. Please Use update endpoint",
        )
    }

    suspend fun addTasks(tasks: List<Task>?, user: User): List<Task> {
        val newTasks = tasks?.filter { task ->
            !this.taskRepository.existsByUserIdAndNameAndTimestamp(
                user.id!!,
                task.name!!,
                task.timestamp!!,
            )
        } ?: return emptyList()

        val saved = newTasks.map { task ->
            taskRepository.add(task)
        }
        saved.forEach { t ->
            addTaskStateEvent(t, TaskState.ADDED, t.createdAt)
        }

        return saved
    }

    private fun addTaskStateEvent(t: Task?, @Suppress("SameParameterValue") state: TaskState?, time: Instant?) {
        val taskStateEventDto = TaskStateEventDto(t, state, null, time)
        eventPublisher.post(taskStateEventDto)
    }

    suspend fun updateTaskStatus(oldTask: Task, state: TaskState): Task {
        val user = oldTask.user

        val doesntExists =
            !this.taskRepository.existsByUserIdAndNameAndTimestamp(user!!.id!!, oldTask.name!!, oldTask.timestamp!!)

        checkPresence(doesntExists, "task_not_found") {
            "The Task ${oldTask.id} does not exist to set to state $state  Please Use add endpoint"
        }

        if (state == TaskState.COMPLETED) {
            oldTask.completed = true
            oldTask.timeCompleted = Timestamp.from(Instant.now())
        }
        oldTask.status = state
        return this.taskRepository.update(oldTask)!!
    }

    companion object {
        private const val INVALID_SUBJECT_ID_MESSAGE =
            "The supplied Subject ID is invalid. No user found. Please Create a User First."
    }
}
