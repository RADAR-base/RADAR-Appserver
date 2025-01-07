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

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.radarbase.appserver.dto.TaskStateEventDto
import org.radarbase.appserver.entity.TaskStateEvent
import org.radarbase.appserver.event.state.TaskState
import org.radarbase.appserver.repository.TaskStateEventRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException
import javax.naming.SizeLimitExceededException

@Suppress("unused")
@Service
class TaskStateEventService(
    private val taskStateEventRepository: TaskStateEventRepository,
    private val taskService: TaskService,
    private val notificationService: FcmNotificationService,
    private val taskApplicationEventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper
) {

    @Transactional
    fun addTaskStateEvent(taskStateEvent: TaskStateEvent) {
        taskStateEventRepository.save(taskStateEvent)
        taskService.updateTaskStatus(taskStateEvent.task, taskStateEvent.state)
        if (taskStateEvent.state == TaskState.COMPLETED) {
            notificationService.deleteNotificationsByTaskId(taskStateEvent.task)
        }
    }

    @Transactional(readOnly = true)
    fun getTaskStateEvents(
        projectId: String?, subjectId: String?, taskId: Long
    ): List<TaskStateEventDto> {
        val task = taskService.getTaskById(taskId)
        val stateEvents = taskStateEventRepository.findByTaskId(taskId)
        return stateEvents.map { ns ->
            TaskStateEventDto(
                id = ns.id,
                taskId = task.id,
                state = ns.state,
                time = ns.time,
                associatedInfo = ns.associatedInfo
            )
        }
    }

    @Transactional(readOnly = true)
    fun getTaskStateEventsByTaskId(
        taskId: Long
    ): List<TaskStateEventDto> {
        val stateEvents: List<TaskStateEvent> = taskStateEventRepository.findByTaskId(taskId)
        return stateEvents.map { ns ->
            TaskStateEventDto(
                id = ns.id,
                taskId = ns.task?.id,
                state = ns.state,
                time = ns.time,
                associatedInfo = ns.associatedInfo
            )
        }
    }

    @Transactional
    @Throws(SizeLimitExceededException::class)
    fun publishNotificationStateEventExternal(
        projectId: String,
        subjectId: String,
        taskId: Long,
        taskStateEventDto: TaskStateEventDto
    ) {
        val taskState = requireNotNull(taskStateEventDto.state) { "State is missing" }
        checkState(taskId, taskState)
        val task = taskService.getTaskById(taskId)

        val additionalInfo: Map<String, String>? = if (!taskStateEventDto.associatedInfo.isNullOrEmpty()) {
            try {
                objectMapper.readValue(
                    taskStateEventDto.associatedInfo,
                    object : TypeReference<Map<String, String>>() {}
                )
            } catch (exc: IOException) {
                throw IllegalStateException(
                    "Cannot convert additionalInfo to Map<String, String>. Please check its format.",
                    exc
                )
            }
        } else {
            null
        }

        val stateEvent = org.radarbase.appserver.event.state.dto.TaskStateEventDto(
            this,
            task,
            taskStateEventDto.state,
            additionalInfo,
            taskStateEventDto.time
        )
        taskApplicationEventPublisher.publishEvent(stateEvent)
    }

    @Throws(SizeLimitExceededException::class, IllegalStateException::class)
    private fun checkState(taskId: Long, state: TaskState) {
        if (state in EXTERNAL_EVENTS) {
            if (taskStateEventRepository.countByTaskId(taskId) >= MAX_NUMBER_OF_STATES) {
                throw SizeLimitExceededException(
                    "The max limit of state changes($MAX_NUMBER_OF_STATES) has been reached. Cannot add new states."
                )
            }
        } else {
            throw IllegalStateException(
                "The state $state is not an external state and cannot be updated by this endpoint."
            )
        }
    }

    companion object {
        private val EXTERNAL_EVENTS: Set<TaskState> = setOf(
            TaskState.COMPLETED,
            TaskState.UNKNOWN,
            TaskState.ERRORED
        )

        private const val MAX_NUMBER_OF_STATES = 20
    }
}
