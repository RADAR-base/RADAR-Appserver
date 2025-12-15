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

package org.radarbase.appserver.microservices.cloud.messaging.service

import com.google.common.eventbus.EventBus
import jakarta.inject.Inject
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.glassfish.hk2.api.ServiceLocator
import org.radarbase.appserver.microservices.core.dto.DataMessageStateEventDto
import org.radarbase.appserver.microservices.core.entity.DataMessage
import org.radarbase.appserver.microservices.core.entity.DataMessageStateEvent
import org.radarbase.appserver.microservices.core.event.state.MessageState
import org.radarbase.appserver.microservices.core.repository.DataMessageStateEventRepository
import org.radarbase.appserver.microservices.core.service.DataMessageStateEventService
import org.radarbase.appserver.microservices.core.service.FcmDataMessageService
import java.io.IOException
import kotlin.collections.contains

@Suppress("unused")
class DataMessageStateEventServiceImpl @Inject constructor(
    private val dataMessageStateEventRepository: DataMessageStateEventRepository,
    private val dataMessageService: FcmDataMessageService,
    private val serviceLocator: ServiceLocator,
) : DataMessageStateEventService {
    private var dataMessageEventBus: EventBus? = null
        get() {
            if (field == null) {
                return serviceLocator.getService(EventBus::class.java)
                    ?.also { field = it }
            }
            return field
        }

    override suspend fun addDataMessageStateEvent(dataMessageStateEvent: DataMessageStateEvent) {
        dataMessageStateEventRepository.add(dataMessageStateEvent)
    }

    override suspend fun getDataMessageStateEvents(
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

    override suspend fun getDataMessageStateEventsByDataMessageId(
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

    override suspend fun publishDataMessageStateEventExternal(
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
                additionalInfo = Json.decodeFromString(
                    MapSerializer(String.serializer(), String.serializer()),
                    dataMessageStateEventDto.associatedInfo!!,
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

        val stateEvent = org.radarbase.appserver.microservices.core.event.state.dto.DataMessageStateEventDto(
            dataMessage,
            messageState,
            additionalInfo,
            messageTime,
        )
        dataMessageEventBus?.post(stateEvent) ?: logger.error("Event bus is not initialized.")
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
        private val logger = org.slf4j.LoggerFactory.getLogger(DataMessageStateEventService::class.java)
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
