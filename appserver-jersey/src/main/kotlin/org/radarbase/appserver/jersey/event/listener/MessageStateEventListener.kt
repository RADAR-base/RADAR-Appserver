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

package org.radarbase.appserver.jersey.event.listener

import com.google.common.eventbus.AllowConcurrentEvents
import com.google.common.eventbus.Subscribe
import jakarta.inject.Inject
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.glassfish.hk2.api.ServiceLocator
import org.radarbase.appserver.jersey.entity.DataMessageStateEvent
import org.radarbase.appserver.jersey.entity.NotificationStateEvent
import org.radarbase.appserver.jersey.event.state.dto.DataMessageStateEventDto
import org.radarbase.appserver.jersey.event.state.dto.NotificationStateEventDto
import org.radarbase.appserver.jersey.service.DataMessageStateEventService
import org.radarbase.appserver.jersey.service.NotificationStateEventService
import org.radarbase.jersey.service.AsyncCoroutineService
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Suppress("unused")
class MessageStateEventListener @Inject constructor(
    private val asyncService: AsyncCoroutineService,
    private val serviceLocator: ServiceLocator,
) {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    private var notificationStateEventService: NotificationStateEventService? = null
        get() {
            if (field == null) {
                return serviceLocator.getService(NotificationStateEventService::class.java)
                    ?.also { field = it }
            }
            return field
        }

    private var dataMessageStateEventService: DataMessageStateEventService? = null
        get() {
            if (field == null) {
                return serviceLocator.getService(DataMessageStateEventService::class.java)
                    ?.also { field = it }
            }
            return field
        }

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Subscribe
    @AllowConcurrentEvents
    fun onNotificationStateChange(event: NotificationStateEventDto) {
        val info = convertMapToString(event.additionalInfo)
        logger.info("Notification state changed. ID: {}, STATE: {}.", event.notification.id, event.state)
        val eventEntity = NotificationStateEvent(
            event.notification,
            event.state,
            event.time,
            info,
        )
        asyncService.runBlocking {
            notificationStateEventService?.addNotificationStateEvent(eventEntity)
                ?: logger.error("NotificationStateEventService is not initialized.")
        }
    }

    @Subscribe
    @AllowConcurrentEvents
    fun onDataMessageStateChange(event: DataMessageStateEventDto) {
        val info = convertMapToString(event.additionalInfo)
        logger.debug("Data Message state changed. ID: {}, STATE: {}", event.dataMessage.id, event.state)
        val eventEntity = DataMessageStateEvent(
            event.dataMessage,
            event.state,
            event.time,
            info,
        )
        asyncService.runBlocking {
            dataMessageStateEventService?.addDataMessageStateEvent(eventEntity)
                ?: logger.error("DataMessageStateEventService is not initialized.")
        }
    }

    fun convertMapToString(additionalInfoMap: Map<String, String>?): String? {
        if (additionalInfoMap == null) return null
        return try {
            json.encodeToString(
                MapSerializer(String.serializer(), String.serializer()),
                additionalInfoMap,
            )
        } catch (e: Exception) {
            logger.warn("error processing event's additional info: {}", additionalInfoMap, e)
            null
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(MessageStateEventListener::class.java)
    }
}
