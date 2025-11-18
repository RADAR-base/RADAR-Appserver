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
import jakarta.ws.rs.core.Response
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.glassfish.hk2.api.ServiceLocator
import org.radarbase.appserver.microservices.contract.calls.NotificationServiceContract
import org.radarbase.appserver.microservices.contract.calls.UserServiceContract
import org.radarbase.appserver.microservices.contract.exception.ProxyResponseException
import org.radarbase.appserver.microservices.contract.utils.Utils.deserializeDtoFromContract
import org.radarbase.appserver.microservices.core.dto.TaskStateEventDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUserDto
import org.radarbase.appserver.microservices.core.entity.Task
import org.radarbase.appserver.microservices.core.entity.TaskStateEvent
import org.radarbase.appserver.microservices.core.event.state.TaskState
import org.radarbase.appserver.microservices.core.repository.TaskStateEventRepository
import org.radarbase.appserver.microservices.core.service.TaskService
import org.radarbase.appserver.microservices.core.service.TaskStateEventService
import org.radarbase.appserver.microservices.task.config.TaskServiceConfig
import org.slf4j.LoggerFactory
import javax.naming.SizeLimitExceededException

@Suppress("unused")
class TaskStateEventServiceImpl @Inject constructor(
    private val taskStateEventRepository: TaskStateEventRepository,
    private val taskService: TaskService,
    private val serviceLocator: ServiceLocator,
    config: TaskServiceConfig,
) : TaskStateEventService {
    private val userServiceUrl = config.contract.user
    private val notificationServiceUrl = config.contract.notification

    private var taskStateEventBus: EventBus? = null
        get() {
            if (field == null) {
                return serviceLocator.getService(EventBus::class.java)
                    ?.also { field = it }
            }
            return field
        }

    override suspend fun addTaskStateEvent(taskStateEvent: TaskStateEvent) {
        taskStateEventRepository.add(taskStateEvent)
        val task = checkNotNull(taskStateEvent.task) { "Task in task state event can't be null" }
        val state = checkNotNull(taskStateEvent.state) { "State in task state event can't be null" }

        taskService.updateTaskStatus(task, state)
        if (taskStateEvent.state == TaskState.COMPLETED) {
            val taskId = checkNotNull(task.id) {
                "Task Id can't be null"
            }

            val userId = checkNotNull(task.userId) { "User ID can't be null" }
            val user = deserializeDtoFromContract<FcmUserDto>(
                UserServiceContract.getUserUsingId(userId, userServiceUrl),
            ) {
                "user_not_found ; User with id $userId not found"
            }

            val subjectId = checkNotNull(user.subjectId) {
                "Subject ID can't be null"
            }
            val projectId = checkNotNull(user.projectId) {
                "Project ID can't be null"
            }
            NotificationServiceContract.deleteNotificationUsingProjectIdAndSubjectIdAndTaskId(
                projectId,
                subjectId,
                taskId,
                notificationServiceUrl,
            ).let {
                if (it.status !in 200..299) {
                    throw ProxyResponseException(
                        Response.Status.fromStatusCode(it.status),
                        it.body?.decodeToString() ?: "Upstream failed to delete notification",
                    )
                }
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    override suspend fun getTaskStateEvents(
        projectId: String?,
        subjectId: String?,
        taskId: Long,
    ): List<TaskStateEventDto> {
        val task: Task = taskService.getTaskById(taskId)
        val stateEvents: List<TaskStateEvent> = taskStateEventRepository.findByTaskId(taskId)
        return stateEvents.map { ts ->
            TaskStateEventDto(
                id = ts.id,
                taskId = task.id,
                state = ts.state,
                time = ts.time,
                associatedInfo = ts.associatedInfo,
            )
        }
    }

    override suspend fun getTaskStateEventsByTaskId(
        taskId: Long,
    ): List<TaskStateEventDto> {
        val stateEvents: List<TaskStateEvent> = taskStateEventRepository.findByTaskId(taskId)
        return stateEvents.map { ts ->
            TaskStateEventDto(
                id = ts.id,
                taskId = ts.task?.id,
                state = ts.state,
                time = ts.time,
                associatedInfo = ts.associatedInfo,
            )
        }
    }

    @Suppress("UNUSED_PARAMETER")
    @Throws(SizeLimitExceededException::class)
    override suspend fun publishTaskStateEventExternal(
        projectId: String,
        subjectId: String,
        taskId: Long,
        taskStateEventDto: TaskStateEventDto,
    ) {
        val taskState = requireNotNull(taskStateEventDto.state) { "State is missing" }
        checkState(taskId, taskState)
        val task = taskService.getTaskById(taskId)

        val additionalInfo: Map<String, String>? = if (!taskStateEventDto.associatedInfo.isNullOrEmpty()) {
            try {
                Json.decodeFromString(
                    MapSerializer(String.serializer(), String.serializer()),
                    taskStateEventDto.associatedInfo!!,
                )
            } catch (exc: IllegalArgumentException) {
                throw IllegalArgumentException(
                    "Cannot convert additionalInfo to Map<String, String>. Please check its format.",
                    exc,
                )
            }
        } else {
            null
        }

        val stateEvent = org.radarbase.appserver.microservices.core.event.state.dto.TaskStateEventDto(
            task,
            taskStateEventDto.state,
            additionalInfo,
            taskStateEventDto.time,
        )
        taskStateEventBus?.post(stateEvent) ?: logger.warn("EventBus is not initialized.")
    }

    @Throws(SizeLimitExceededException::class, IllegalStateException::class)
    private suspend fun checkState(taskId: Long, state: TaskState) {
        if (state in EXTERNAL_EVENTS) {
            if (taskStateEventRepository.countByTaskId(taskId) >= MAX_NUMBER_OF_STATES) {
                throw SizeLimitExceededException(
                    "The max limit of state changes($MAX_NUMBER_OF_STATES) has been reached. Cannot add new states.",
                )
            }
        } else {
            throw IllegalStateException(
                "The state $state is not an external state and cannot be updated by this endpoint.",
            )
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TaskStateEventServiceImpl::class.java)

        private val EXTERNAL_EVENTS: Set<TaskState> = setOf(
            TaskState.COMPLETED,
            TaskState.UNKNOWN,
            TaskState.ERRORED,
        )

        private const val MAX_NUMBER_OF_STATES = 20
    }
}
