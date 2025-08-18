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
import org.radarbase.appserver.dto.NotificationStateEventDto
import org.radarbase.appserver.entity.NotificationStateEvent
import org.radarbase.appserver.event.state.MessageState
import org.radarbase.appserver.repository.NotificationStateEventRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException

@Service
class NotificationStateEventService(
    private val notificationStateEventRepository: NotificationStateEventRepository,
    private val notificationService: FcmNotificationService,
    private val notificationApplicationEventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun addNotificationStateEvent(notificationStateEvent: NotificationStateEvent) {
        if (notificationStateEvent.state == MessageState.CANCELLED) {
            // the notification will be removed shortly
            return
        }
        notificationStateEventRepository.save(notificationStateEvent)
    }

    @Transactional(readOnly = true)
    fun getNotificationStateEvents(
        projectId: String?,
        subjectId: String?,
        notificationId: Long,
    ): List<NotificationStateEventDto> {
        notificationService.getNotificationByProjectIdAndSubjectIdAndNotificationId(
            projectId,
            subjectId,
            notificationId,
        )
        val stateEvents: List<NotificationStateEvent> =
            notificationStateEventRepository.findByNotificationId(notificationId)
        return stateEvents.map { notificationState: NotificationStateEvent? ->
            NotificationStateEventDto(
                notificationState?.id,
                notificationState?.notification?.id,
                notificationState?.state,
                notificationState?.time,
                notificationState?.associatedInfo,
            )
        }
    }

    @Transactional(readOnly = true)
    fun getNotificationStateEventsByNotificationId(
        notificationId: Long,
    ): List<NotificationStateEventDto> {
        val stateEvents = notificationStateEventRepository.findByNotificationId(notificationId)
        return stateEvents.map { notificationState: NotificationStateEvent? ->
            NotificationStateEventDto(
                notificationState?.id,
                notificationState?.notification?.id,
                notificationState?.state,
                notificationState?.time,
                notificationState?.associatedInfo,
            )
        }
    }

    @Transactional
    fun publishNotificationStateEventExternal(
        projectId: String?,
        subjectId: String?,
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
                additionalInfo = objectMapper.readValue<Map<String, String>>(
                    notificationStateEventDto.associatedInfo,
                    object : TypeReference<Map<String, String>>() {
                    },
                )
            } catch (_: IOException) {
                throw IllegalStateException(
                    "Cannot convert additionalInfo to Map<String, String>. Please check its format.",
                )
            }
        }

        val stateEvent = org.radarbase.appserver.event.state.dto.NotificationStateEventDto(
            this,
            notification,
            notificationStateEventDto.state!!,
            additionalInfo,
            notificationStateEventDto.time!!,
        )
        notificationApplicationEventPublisher.publishEvent(stateEvent)
    }

    @Throws(IllegalStateException::class)
    private fun checkState(notificationId: Long, state: MessageState?) {
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
        private const val MAX_NUMBER_OF_STATES = 20

        private val EXTERNAL_EVENTS = setOf<MessageState>(
            MessageState.DELIVERED,
            MessageState.DISMISSED,
            MessageState.OPENED,
            MessageState.UNKNOWN,
            MessageState.ERRORED,
        )
    }
}
