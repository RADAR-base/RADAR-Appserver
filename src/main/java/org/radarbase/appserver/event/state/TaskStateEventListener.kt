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
package org.radarbase.appserver.event.state

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.radarbase.appserver.entity.TaskStateEvent
import org.radarbase.appserver.event.state.dto.TaskStateEventDto
import org.radarbase.appserver.service.TaskStateEventService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionalEventListener

@Component
class TaskStateEventListener(
    @field:Transient private val objectMapper: ObjectMapper,
    @field:Transient private val taskStateEventService: TaskStateEventService
) {
    /**
     * Handle an application event.
     * // we can add more event listeners by annotating with @EventListener
     *
     * @param event the event to respond to
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(classes = [TaskStateEventDto::class])
    @Async
    fun onTaskStateChange(event: TaskStateEventDto) {
        val info = convertMapToString(event.additionalInfo)
        logger.debug("ID: {}, STATE: {}", event.task?.id, event.state)
        val eventEntity = TaskStateEvent(
            event.task, event.state, event.time, info
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
