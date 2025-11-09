package org.radarbase.appserver.microservices.task.event.listener

import com.google.common.eventbus.AllowConcurrentEvents
import com.google.common.eventbus.Subscribe
import jakarta.inject.Inject
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.glassfish.hk2.api.ServiceLocator
import org.radarbase.appserver.microservices.core.entity.TaskStateEvent
import org.radarbase.appserver.microservices.core.event.state.dto.TaskStateEventDto
import org.radarbase.appserver.microservices.core.service.TaskStateEventService
import org.radarbase.jersey.service.AsyncCoroutineService
import org.slf4j.LoggerFactory

@Suppress("unused")
class TaskStateEventListener @Inject constructor(
    private val asyncService: AsyncCoroutineService,
    private val serviceLocator: ServiceLocator,
) {
    private var taskStateEventService: TaskStateEventService? = null
        get() {
            if (field == null) {
                return serviceLocator.getService(TaskStateEventService::class.java)
                    ?.also { field = it }
            }
            return field
        }

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    /**
     * Handle an application event.
     * // we can add more event listeners by annotating with @EventListener
     *
     * @param event the event to respond to
     */
    @Subscribe
    @AllowConcurrentEvents
    fun onTaskStateChange(event: TaskStateEventDto) {
        val info = convertMapToString(event.additionalInfo)
        logger.info("Task state changed. ID: {}, STATE: {}", event.task?.id, event.state)
        val eventEntity = TaskStateEvent(
            event.task,
            event.state,
            event.time,
            info,
        )
        asyncService.runBlocking {
            taskStateEventService?.addTaskStateEvent(eventEntity)
                ?: logger.error("TaskStateEventService is not initialized.")
        }
    }

    fun convertMapToString(additionalInfoMap: Map<String, String>?): String? {
        if (additionalInfoMap == null) return null
        return try {
            json.encodeToString(
                MapSerializer(String.Companion.serializer(), String.serializer()),
                additionalInfoMap,
            )
        } catch (e: Exception) {
            logger.error("error processing event's additional info: {}", additionalInfoMap, e)
            null
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TaskStateEventListener::class.java)
    }
}
