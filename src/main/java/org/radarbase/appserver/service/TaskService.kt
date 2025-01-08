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

import org.radarbase.appserver.dto.protocol.AssessmentType
import org.radarbase.appserver.entity.Task
import org.radarbase.appserver.entity.User
import org.radarbase.appserver.event.state.TaskState
import org.radarbase.appserver.event.state.dto.TaskStateEventDto
import org.radarbase.appserver.exception.AlreadyExistsException
import org.radarbase.appserver.repository.TaskRepository
import org.radarbase.appserver.repository.UserRepository
import org.radarbase.appserver.util.checkPresence
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.Timestamp
import java.time.Instant
import java.util.function.Consumer

/**
 * [Service] for interacting with the [Task] [jakarta.persistence.Entity] using
 * the [TaskRepository].
 *
 * @author yatharthranjan
 */
@Service
@Suppress("unused")
class TaskService(
    private val taskRepository: TaskRepository,
    private val userRepository: UserRepository,
    private val eventPublisher: ApplicationEventPublisher?
) {
    @Transactional(readOnly = true)
    fun getAllProjects(): List<Task> {
        return taskRepository.findAll()
    }

    @Transactional(readOnly = true)
    fun getTaskById(id: Long): Task {
        val task = taskRepository.findByIdOrNull(id)

        return checkPresence(task) {
            "Task not found with id $id"
        }
    }

    @Transactional(readOnly = true)
    fun getTasksBySubjectId(subjectId: String?): List<Task> {
        val user = this.userRepository.findBySubjectId(subjectId)
        checkPresence(user) {
            INVALID_SUBJECT_ID_MESSAGE
        }
        return taskRepository.findByUserId(user.id)
    }

    @Transactional(readOnly = true)
    fun getTasksBySubjectIdAndType(subjectId: String?, type: AssessmentType?): List<Task> {
        val user = this.userRepository.findBySubjectId(subjectId)
        checkPresence(user) {
            INVALID_SUBJECT_ID_MESSAGE
        }
        return taskRepository.findByUserIdAndType(user.id, type)
    }

    fun getTasksByUser(user: User): List<Task> {
        return taskRepository.findByUserId(user.id)
    }

    @Transactional
    fun getTasksBySpecification(spec: Specification<Task>): List<Task> {
        return taskRepository.findAll(spec)
    }

    @Transactional
    fun deleteTasksBySpecification(spec: Specification<Task>) {
        val tasks = taskRepository.findAll(spec)
        taskRepository.deleteAll(tasks)
    }

    @Transactional
    fun deleteTasksByUserId(userId: Long?) {
        taskRepository.deleteByUserId(userId)
    }

    @Transactional
    fun addTask(task: Task): Task {
        val user = task.user

        val alreadyExists = this.taskRepository.existsByUserIdAndNameAndTimestamp(user!!.id, task.name, task.timestamp)

        if (!alreadyExists) {
            val saved = this.taskRepository.saveAndFlush<Task>(task)
            user.usermetrics!!.lastOpened = Instant.now()
            this.userRepository.save(user)
            addTaskStateEvent(saved, TaskState.ADDED, saved.createdAt!!.toInstant())
            return saved
        } else throw AlreadyExistsException(
            "The Task Already exists. Please Use update endpoint", task
        )
    }

    @Transactional
    fun addTasks(tasks: MutableList<Task?>, user: User): MutableList<Task?> {
        val newTasks = tasks.filter { task ->
                !this.taskRepository.existsByUserIdAndNameAndTimestamp(
                    user.id,
                    task!!.name,
                    task.timestamp
                )
            }

        val saved = this.taskRepository.saveAllAndFlush(newTasks)
        saved.forEach(Consumer { t -> addTaskStateEvent(t, TaskState.ADDED, t!!.createdAt!!.toInstant()) })

        return saved
    }

    private fun addTaskStateEvent(t: Task?, @Suppress("SameParameterValue") state: TaskState?, time: Instant?) {
        if (eventPublisher != null) {
            val taskStateEventDto = TaskStateEventDto(this, t, state, null, time)
            eventPublisher.publishEvent(taskStateEventDto)
        }
    }

    @Transactional
    fun updateTaskStatus(oldTask: Task, state: TaskState): Task {
        val user = oldTask.user

        val doesntExists = !this.taskRepository.existsByUserIdAndNameAndTimestamp(user!!.id, oldTask.name, oldTask.timestamp)

        checkPresence(doesntExists) {
            "The Task ${oldTask.id} does not exist to set to state $state  Please Use add endpoint"
        }

        if (state == TaskState.COMPLETED) {
            oldTask.completed = true
            oldTask.timeCompleted = Timestamp.from(Instant.now())
        }
        oldTask.status = state
        return this.taskRepository.saveAndFlush(oldTask)
    }

    companion object {
        private const val INVALID_SUBJECT_ID_MESSAGE =
            "The supplied Subject ID is invalid. No user found. Please Create a User First."
    }
}
