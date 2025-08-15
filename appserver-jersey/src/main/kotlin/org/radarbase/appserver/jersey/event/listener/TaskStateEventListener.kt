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

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.eventbus.AllowConcurrentEvents
import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import jakarta.inject.Inject
import kotlinx.coroutines.runBlocking
import org.glassfish.hk2.api.ServiceLocator
import org.radarbase.appserver.jersey.entity.TaskStateEvent
import org.radarbase.appserver.jersey.event.state.dto.TaskStateEventDto
import org.radarbase.appserver.jersey.service.TaskStateEventService
import org.radarbase.jersey.service.AsyncCoroutineService
import org.slf4j.LoggerFactory
import kotlin.properties.Delegates

@Suppress("unused")
class TaskStateEventListener @Inject constructor(
    private val objectMapper: ObjectMapper,
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
            event.task, event.state, event.time, info,
        )
        asyncService.runBlocking {
            taskStateEventService?.addTaskStateEvent(eventEntity)
                ?: logger.error("TaskStateEventService is not initialized.")
        }
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
