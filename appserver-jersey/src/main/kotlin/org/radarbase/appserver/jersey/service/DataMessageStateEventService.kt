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
import org.radarbase.appserver.jersey.dto.DataMessageStateEventDto
import org.radarbase.appserver.jersey.entity.DataMessage
import org.radarbase.appserver.jersey.entity.DataMessageStateEvent
import org.radarbase.appserver.jersey.event.state.MessageState
import org.radarbase.appserver.jersey.repository.DataMessageStateEventRepository
import java.io.IOException

@Suppress("unused")
class DataMessageStateEventService @Inject constructor(
    private val dataMessageStateEventRepository: DataMessageStateEventRepository,
    private val dataMessageService: FcmDataMessageService,
    private val dataMessageEventBus: EventBus,
    private val objectMapper: ObjectMapper,
) {
    suspend fun addDataMessageStateEvent(dataMessageStateEvent: DataMessageStateEvent) {
        dataMessageStateEventRepository.add(dataMessageStateEvent)
    }

    suspend fun getDataMessageStateEvents(
        projectId: String,
        subjectId: String,
        dataMessageId: Long,
    ): List<DataMessageStateEventDto> {
        dataMessageService.getDataMessageByProjectIdAndSubjectIdAndDataMessageId(
            projectId,
            subjectId,
            dataMessageId,
        )

        val stateEvents: List<DataMessageStateEvent> =
            dataMessageStateEventRepository.findByDataMessageId(dataMessageId)
        return stateEvents.map { stateEvent: DataMessageStateEvent ->
            DataMessageStateEventDto(
                stateEvent.id,
                nonNullDataMessage(stateEvent).id,
                stateEvent.state,
                stateEvent.time,
                stateEvent.associatedInfo,
            )
        }
    }

    suspend fun getDataMessageStateEventsByDataMessageId(
        dataMessageId: Long,
    ): List<DataMessageStateEventDto> {
        val stateEvents: List<DataMessageStateEvent> =
            dataMessageStateEventRepository.findByDataMessageId(dataMessageId)
        return stateEvents.map { stateEvent: DataMessageStateEvent ->
            DataMessageStateEventDto(
                stateEvent.id,
                nonNullDataMessage(stateEvent).id,
                stateEvent.state,
                stateEvent.time,
                stateEvent.associatedInfo,
            )
        }
    }

    suspend fun publishDataMessageStateEventExternal(
        projectId: String,
        subjectId: String,
        dataMessageId: Long,
        dataMessageStateEventDto: DataMessageStateEventDto,
    ) {
        checkState(dataMessageId, dataMessageStateEventDto.state)
        val dataMessage = dataMessageService.getDataMessageByProjectIdAndSubjectIdAndDataMessageId(
            projectId,
            subjectId,
            dataMessageId,
        )

        var additionalInfo: Map<String, String>? = null

        if (!dataMessageStateEventDto.associatedInfo.isNullOrEmpty()) {
            try {
                additionalInfo = objectMapper.readValue(
                    dataMessageStateEventDto.associatedInfo,
                    object : TypeReference<Map<String, String>>() {},
                )
            } catch (_: IOException) {
                throw IllegalStateException(
                    "Cannot convert additionalInfo to Map<String, String>. Please check its format.",
                )
            }
        }

        val messageState = requireNotNull(dataMessageStateEventDto.state) {
            "Data Message state event's state can't be null."
        }
        val messageTime = requireNotNull(dataMessageStateEventDto.time) {
            "Data Message state event's time can't be null."
        }

        val stateEvent = org.radarbase.appserver.jersey.event.state.dto.DataMessageStateEventDto(
            dataMessage,
            messageState,
            additionalInfo,
            messageTime,
        )
        dataMessageEventBus.post(stateEvent)
    }

    @Throws(IllegalStateException::class)
    private suspend fun checkState(dataMessageId: Long, state: MessageState?) {
        if (EXTERNAL_EVENTS.contains(state)) {
            if (dataMessageStateEventRepository.countByDataMessageId(dataMessageId) >= MAX_NUMBER_OF_STATES) {
                throw IllegalStateException(
                    ("The max limit of state changes($MAX_NUMBER_OF_STATES) has been reached. Cannot add new states."),
                )
            }
        } else {
            throw IllegalStateException(("The state $state is not an external state and cannot be updated by this endpoint."))
        }
    }

    companion object {
        private val EXTERNAL_EVENTS = setOf(
            MessageState.DELIVERED,
            MessageState.DISMISSED,
            MessageState.OPENED,
            MessageState.UNKNOWN,
            MessageState.ERRORED,
        )
        private const val MAX_NUMBER_OF_STATES = 20

        private fun nonNullDataMessage(stateEvent: DataMessageStateEvent): DataMessage =
            checkNotNull(stateEvent.dataMessage) {
                "DataMessage in state event data can't be null"
            }
    }
}
