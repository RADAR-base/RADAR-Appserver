package org.radarbase.appserver.jersey.event.listener

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.eventbus.AllowConcurrentEvents
import com.google.common.eventbus.Subscribe
import jakarta.inject.Inject
import org.radarbase.appserver.jersey.entity.DataMessageStateEvent
import org.radarbase.appserver.jersey.entity.NotificationStateEvent
import org.radarbase.appserver.jersey.event.state.dto.DataMessageStateEventDto
import org.radarbase.appserver.jersey.event.state.dto.NotificationStateEventDto
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MessageStateEventListener @Inject constructor(
    private val objectMapper: ObjectMapper,
    private val notificationStateEventService: NotificationStateEventService,
    private val dataMessageStateEventService: DataMessageStateEventService
) {
    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Subscribe
    @AllowConcurrentEvents
    fun onNotificationStateChange(event: NotificationStateEventDto) {
        val info = convertMapToString(event.additionalInfo)
        logger.debug("ID: {}, STATE: {}.", event.notification.id, event.state)
        val eventEntity = NotificationStateEvent(
            event.notification, event.state, event.time, info
        )
        notificationStateEventService.addNotificationStateEvent(eventEntity)
    }

    @Subscribe
    @AllowConcurrentEvents
    fun onDataMessageStateChange(event: DataMessageStateEventDto) {
        val info = convertMapToString(event.additionalInfo)
        logger.debug("ID: {}, STATE: {}", event.dataMessage.id, event.state)
        val eventEntity = DataMessageStateEvent(
            event.dataMessage, event.state, event.time, info
        )
        dataMessageStateEventService.addDataMessageStateEvent(eventEntity)
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
        private val logger: Logger = LoggerFactory.getLogger(MessageStateEventListener::class.java)
    }
}
