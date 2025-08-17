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

package org.radarbase.appserver.jersey.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.eventbus.EventBus
import jakarta.inject.Inject
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.glassfish.hk2.api.ServiceLocator
import org.radarbase.appserver.jersey.dto.NotificationStateEventDto
import org.radarbase.appserver.jersey.entity.Notification
import org.radarbase.appserver.jersey.entity.NotificationStateEvent
import org.radarbase.appserver.jersey.event.state.MessageState
import org.radarbase.appserver.jersey.repository.NotificationStateEventRepository
import java.io.IOException

@Suppress("unused")
class NotificationStateEventService @Inject constructor(
    private val notificationStateEventRepository: NotificationStateEventRepository,
    private val notificationService: FcmNotificationService,
    private val serviceLocator: ServiceLocator,
) {
    private var notificationStateEventBus: EventBus? = null
        get() {
            if (field == null) {
                return serviceLocator.getService(EventBus::class.java)
                    ?.also { field = it }
            }
            return field
        }


    suspend fun addNotificationStateEvent(notificationStateEvent: NotificationStateEvent) {
        if (notificationStateEvent.state == MessageState.CANCELLED) {
            // the notification will be removed shortly
            return
        }
        notificationStateEventRepository.add(notificationStateEvent)
    }

    suspend fun getNotificationStateEvents(
        projectId: String,
        subjectId: String,
        notificationId: Long,
    ): List<NotificationStateEventDto> {
        notificationService.getNotificationByProjectIdAndSubjectIdAndNotificationId(
            projectId,
            subjectId,
            notificationId,
        )
        val stateEvents: List<NotificationStateEvent> =
            notificationStateEventRepository.findByNotificationId(notificationId)
        return stateEvents.map { notificationStateEvent: NotificationStateEvent ->
            NotificationStateEventDto(
                notificationStateEvent.id,
                nonNullNotification(notificationStateEvent).id,
                notificationStateEvent.state,
                notificationStateEvent.time,
                notificationStateEvent.associatedInfo,
            )
        }
    }

    suspend fun getNotificationStateEventsByNotificationId(
        notificationId: Long,
    ): List<NotificationStateEventDto> {
        val stateEvents = notificationStateEventRepository.findByNotificationId(notificationId)
        return stateEvents.map { notificationStateEvent: NotificationStateEvent ->
            NotificationStateEventDto(
                notificationStateEvent.id,
                nonNullNotification(notificationStateEvent).id,
                notificationStateEvent.state,
                notificationStateEvent.time,
                notificationStateEvent.associatedInfo,
            )
        }
    }

    suspend fun publishNotificationStateEventExternal(
        projectId: String,
        subjectId: String,
        notificationId: Long,
        notificationStateEventDto: NotificationStateEventDto,
    ) {
        checkState(notificationId, notificationStateEventDto.state)
        val notification = notificationService.getNotificationByProjectIdAndSubjectIdAndNotificationId(
            projectId,
            subjectId,
            notificationId,
        )

        var additionalInfo: Map<String, String>? = null
        if (!notificationStateEventDto.associatedInfo.isNullOrEmpty()) {
            try {
                additionalInfo = Json.decodeFromString(
                    MapSerializer(String.serializer(), String.serializer()),
                    notificationStateEventDto.associatedInfo!!,
                )
            } catch (_: IOException) {
                throw IllegalStateException(
                    "Cannot convert additionalInfo to Map<String, String>. Please check its format.",
                )
            }
        }

        val messageState = requireNotNull(notificationStateEventDto.state) {
            "Notification state event's state can't be null."
        }
        val messageTime = requireNotNull(notificationStateEventDto.time) {
            "Notification state event's time can't be null."
        }

        val stateEvent = org.radarbase.appserver.jersey.event.state.dto.NotificationStateEventDto(
            notification,
            messageState,
            additionalInfo,
            messageTime,
        )
        notificationStateEventBus?.post(stateEvent) ?: log.error("Event bus is not initialized")
    }

    @Throws(IllegalStateException::class)
    private suspend fun checkState(notificationId: Long, state: MessageState?) {
        if (EXTERNAL_EVENTS.contains(state)) {
            if (notificationStateEventRepository.countByNotificationId(notificationId)
                >= MAX_NUMBER_OF_STATES
            ) {
                throw IllegalStateException("The max limit of state changes($MAX_NUMBER_OF_STATES) has been reached. Cannot add new states.")
            }
        } else {
            throw IllegalStateException("The state $state is not an external state and cannot be updated by this endpoint.")
        }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(NotificationStateEventService::class.java)
        private const val MAX_NUMBER_OF_STATES = 20

        private val EXTERNAL_EVENTS = setOf<MessageState>(
            MessageState.DELIVERED,
            MessageState.DISMISSED,
            MessageState.OPENED,
            MessageState.UNKNOWN,
            MessageState.ERRORED,
        )

        private fun nonNullNotification(stateEvent: NotificationStateEvent): Notification =
            checkNotNull(stateEvent.notification) {
                "DataMessage in state event data can't be null"
            }

    }
}
