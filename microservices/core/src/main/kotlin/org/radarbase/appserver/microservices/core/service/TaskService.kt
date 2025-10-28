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

package org.radarbase.appserver.microservices.core.service;

import org.radarbase.appserver.microservices.core.dto.protocol.AssessmentType
import org.radarbase.appserver.microservices.core.entity.Task
import org.radarbase.appserver.microservices.core.entity.User
import org.radarbase.appserver.microservices.core.event.state.TaskState
import org.radarbase.appserver.microservices.core.search.QuerySpecification

/**
 * Contract for task-related operations.
 *
 * Implementations should handle validation, persistence, and event publishing
 * for task entities associated with users.
 */
interface TaskService {

    /** Retrieve all tasks across all users. */
    suspend fun getAllTasks(): List<Task>

    /**
     * Retrieve a task by its unique database ID.
     * @throws org.radarbase.jersey.exception.HttpNotFoundException if task not found
     */
    suspend fun getTaskById(id: Long): Task

    /**
     * Retrieve all tasks for a given subject ID.
     * @throws org.radarbase.jersey.exception.HttpNotFoundException if user not found
     */
    suspend fun getTasksBySubjectId(subjectId: String): List<Task>

    /**
     * Retrieve all tasks for a given subject ID and task type.
     * @throws org.radarbase.jersey.exception.HttpNotFoundException if user not found
     */
    suspend fun getTasksBySubjectIdAndType(subjectId: String, type: AssessmentType): List<Task>

    /** Retrieve all tasks for a given user entity. */
    suspend fun getTasksByUser(user: User): List<Task>

    /** Retrieve all tasks that match a specific query specification. */
    suspend fun getTasksBySpecification(spec: QuerySpecification<Task>): List<Task>

    /** Delete all tasks matching a given query specification. */
    suspend fun deleteTasksBySpecification(spec: QuerySpecification<Task>)

    /** Delete all tasks belonging to a specific user by user ID. */
    suspend fun deleteTasksByUserId(userId: Long)

    /**
     * Add a new task for a user.
     *
     * Publishes a [org.radarbase.appserver.microservices.core.event.state.dto.TaskStateEventDto]
     * with [TaskState.ADDED] when successfully added.
     *
     * @throws org.radarbase.appserver.jersey.exception.AlreadyExistsException if the task already exists
     */
    suspend fun addTask(task: Task): Task

    /**
     * Add multiple tasks for a given user.
     *
     * A [TaskState.ADDED] event is published for each newly added task.
     */
    suspend fun addTasks(tasks: List<Task>, user: User): List<Task>

    /**
     * Update an existing task’s status (e.g., to [TaskState.COMPLETED]).
     *
     * @throws org.radarbase.jersey.exception.HttpNotFoundException if the task does not exist
     */
    suspend fun updateTaskStatus(oldTask: Task, state: TaskState): Task?
}
