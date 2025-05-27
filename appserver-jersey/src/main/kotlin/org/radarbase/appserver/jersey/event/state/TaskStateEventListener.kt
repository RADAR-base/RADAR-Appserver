package org.radarbase.appserver.jersey.event.state

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.eventbus.AllowConcurrentEvents
import com.google.common.eventbus.Subscribe
import jakarta.inject.Inject
import org.radarbase.appserver.jersey.entity.TaskStateEvent
import org.radarbase.appserver.jersey.event.state.dto.TaskStateEventDto
import org.slf4j.LoggerFactory

class TaskStateEventListener @Inject constructor(
    private val objectMapper: ObjectMapper,
    private val taskStateEventService: TaskStateEventService,
) {
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
        logger.debug("ID: {}, STATE: {}", event.task?.id, event.state)
        val eventEntity = TaskStateEvent(
            event.task, event.state, event.time, info,
        )
        taskStateEventService.addTaskStateEvent(eventEntity)
    }

    fun convertMapToString(additionalInfoMap: Map<String, String>?): String? {
        if (additionalInfoMap == null) {
            return null
        }
        try {
            return objectMapper.writeValueAsString(additionalInfoMap)
        } catch (_: JsonProcessingException) {
            logger.warn("error processing event's additional info: {}", additionalInfoMap)
            return null
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TaskStateEventListener::class.java)
    }
}
