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
import org.radarbase.appserver.dto.DataMessageStateEventDto
import org.radarbase.appserver.entity.DataMessageStateEvent
import org.radarbase.appserver.event.state.MessageState
import org.radarbase.appserver.repository.DataMessageStateEventRepository
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException

@Suppress("unused")
@Service
class DataMessageStateEventService(
    private val dataMessageStateEventRepository: DataMessageStateEventRepository,
    private val dataMessageService: FcmDataMessageService,
    private val dataMessageApplicationEventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper,
) {
    @Transactional
    fun addDataMessageStateEvent(dataMessageStateEvent: DataMessageStateEvent) {
        dataMessageStateEventRepository.save(dataMessageStateEvent)
    }

    @Transactional(readOnly = true)
    fun getDataMessageStateEvents(
        projectId: String?,
        subjectId: String?,
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
                stateEvent.dataMessage!!.id,
                stateEvent.state,
                stateEvent.time,
                stateEvent.associatedInfo,
            )
        }
    }

    @Transactional(readOnly = true)
    fun getDataMessageStateEventsByDataMessageId(
        dataMessageId: Long,
    ): List<DataMessageStateEventDto> {
        val stateEvents: List<DataMessageStateEvent> =
            dataMessageStateEventRepository.findByDataMessageId(dataMessageId)
        return stateEvents.map { stateEvent: DataMessageStateEvent ->
            DataMessageStateEventDto(
                stateEvent.id,
                stateEvent.dataMessage!!.id,
                stateEvent.state,
                stateEvent.time,
                stateEvent.associatedInfo,
            )
        }
    }

    @Transactional
    fun publishDataMessageStateEventExternal(
        projectId: String?,
        subjectId: String?,
        dataMessageId: Long,
        dataMessageStateEventDto: DataMessageStateEventDto,
    ) {
        checkState(dataMessageId, dataMessageStateEventDto.state)
        val dataMessage =
            dataMessageService.getDataMessageByProjectIdAndSubjectIdAndDataMessageId(
                projectId,
                subjectId,
                dataMessageId,
            )

        var additionalInfo: Map<String, String>? = null
        if (!dataMessageStateEventDto.associatedInfo!!.isEmpty()) {
            try {
                additionalInfo =
                    objectMapper.readValue<Map<String, String>>(
                        dataMessageStateEventDto.associatedInfo,
                        object : TypeReference<Map<String, String>>() {
                        },
                    )
            } catch (exc: IOException) {
                throw IllegalStateException(
                    "Cannot convert additionalInfo to Map<String, String>. Please check its format.",
                )
            }
        }

        val stateEvent =
            org.radarbase.appserver.event.state.dto.DataMessageStateEventDto(
                this,
                dataMessage,
                dataMessageStateEventDto.state!!,
                additionalInfo,
                dataMessageStateEventDto.time!!,
            )
        dataMessageApplicationEventPublisher.publishEvent(stateEvent)
    }

    @Throws(IllegalStateException::class)
    private fun checkState(dataMessageId: Long, state: MessageState?) {
        if (EXTERNAL_EVENTS.contains(state)) {
            if (dataMessageStateEventRepository.countByDataMessageId(dataMessageId)
                >= MAX_NUMBER_OF_STATES
            ) {
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
    }
}
