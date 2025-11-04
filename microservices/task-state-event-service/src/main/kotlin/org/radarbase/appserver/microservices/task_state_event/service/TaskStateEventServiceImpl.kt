package org.radarbase.appserver.microservices.task_state_event.service

import com.google.common.eventbus.EventBus
import jakarta.inject.Inject
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.glassfish.hk2.api.ServiceLocator
import org.radarbase.appserver.microservices.core.dto.TaskStateEventDto
import org.radarbase.appserver.microservices.core.entity.Task
import org.radarbase.appserver.microservices.core.entity.TaskStateEvent
import org.radarbase.appserver.microservices.core.event.state.TaskState
import org.radarbase.appserver.microservices.core.repository.TaskStateEventRepository
import org.radarbase.appserver.microservices.core.service.FcmNotificationService
import org.radarbase.appserver.microservices.core.service.TaskService
import org.radarbase.appserver.microservices.core.service.TaskStateEventService
import org.slf4j.LoggerFactory
import java.io.IOException
import javax.naming.SizeLimitExceededException

@Suppress("unused")
class TaskStateEventServiceImpl @Inject constructor(
    private val taskStateEventRepository: TaskStateEventRepository,
    private val taskService: TaskService,
    private val notificationService: FcmNotificationService,
    private val serviceLocator: ServiceLocator,
) : TaskStateEventService {
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
        val task = checkNotNull(taskStateEvent.taskId) { "Task in task state event can't be null" }
        val state = checkNotNull(taskStateEvent.state) { "State in task state event can't be null" }

        taskService.updateTaskStatus(task, state)
        if (taskStateEvent.state == TaskState.COMPLETED) {
            notificationService.deleteNotificationsByTaskId(task)
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
                taskId = ts.taskId,
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
            } catch (exc: IOException) {
                throw IllegalStateException(
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
