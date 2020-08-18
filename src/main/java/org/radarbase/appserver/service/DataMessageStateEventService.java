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

package org.radarbase.appserver.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.naming.SizeLimitExceededException;
import org.radarbase.appserver.dto.DataMessageStateEventDto;
import org.radarbase.appserver.entity.DataMessage;
import org.radarbase.appserver.entity.DataMessageStateEvent;
import org.radarbase.appserver.event.state.MessageState;
import org.radarbase.appserver.repository.DataMessageStateEventRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class DataMessageStateEventService {

    private static final Set<MessageState> EXTERNAL_EVENTS;
    private static final int MAX_NUMBER_OF_STATES = 20;

    static {
        EXTERNAL_EVENTS =
                Set.of(
                        MessageState.DELIVERED,
                        MessageState.DISMISSED,
                        MessageState.OPENED,
                        MessageState.UNKNOWN,
                        MessageState.ERRORED);
    }

    private final transient DataMessageStateEventRepository dataMessageStateEventRepository;
    private final transient FcmDataMessageService dataMessageService;
    private final transient ApplicationEventPublisher dataMessageApplicationEventPublisher;
    private final transient ObjectMapper objectMapper;

    public DataMessageStateEventService(
            DataMessageStateEventRepository dataMessageStateEventRepository,
            FcmDataMessageService fcmDataMessageService,
            ApplicationEventPublisher dataMessageApplicationEventPublisher,
            ObjectMapper objectMapper) {
        this.dataMessageStateEventRepository = dataMessageStateEventRepository;
        this.dataMessageService = fcmDataMessageService;
        this.dataMessageApplicationEventPublisher = dataMessageApplicationEventPublisher;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void addDataMessageStateEvent(DataMessageStateEvent dataMessageStateEvent) {
        dataMessageStateEventRepository.save(dataMessageStateEvent);
    }

    @Transactional(readOnly = true)
    public List<DataMessageStateEventDto> getDataMessageStateEvents(
            String projectId, String subjectId, long dataMessageId) {
        dataMessageService.getDataMessageByProjectIdAndSubjectIdAndDataMessageId(
                projectId, subjectId, dataMessageId);
        List<DataMessageStateEvent> stateEvents =
                dataMessageStateEventRepository.findByDataMessageId(dataMessageId);
        return stateEvents.stream()
                .map(
                        ns ->
                                new DataMessageStateEventDto(
                                        ns.getId(),
                                        ns.getDataMessage().getId(),
                                        ns.getState(),
                                        ns.getTime(),
                                        ns.getAssociatedInfo()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<DataMessageStateEventDto> getDataMessageStateEventsByDataMessageId(
            long dataMessageId) {
        List<DataMessageStateEvent> stateEvents =
                dataMessageStateEventRepository.findByDataMessageId(dataMessageId);
        return stateEvents.stream()
                .map(
                        ns ->
                                new DataMessageStateEventDto(
                                        ns.getId(),
                                        ns.getDataMessage().getId(),
                                        ns.getState(),
                                        ns.getTime(),
                                        ns.getAssociatedInfo()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void publishDataMessageStateEventExternal(
            String projectId,
            String subjectId,
            long dataMessageId,
            DataMessageStateEventDto dataMessageStateEventDto)
            throws SizeLimitExceededException {
        checkState(dataMessageId, dataMessageStateEventDto.getState());
        DataMessage dataMessage =
                dataMessageService.getDataMessageByProjectIdAndSubjectIdAndDataMessageId(
                        projectId, subjectId, dataMessageId);

        Map<String, String> additionalInfo = null;
        if (!dataMessageStateEventDto.getAssociatedInfo().isEmpty()) {
            try {
                additionalInfo =
                        objectMapper.readValue(
                                dataMessageStateEventDto.getAssociatedInfo(),
                                new TypeReference<Map<String, String>>() {
                                });
            } catch (IOException exc) {
                throw new IllegalStateException(
                        "Cannot convert additionalInfo to Map<String, String>. Please check its format.");
            }
        }

        org.radarbase.appserver.event.state.DataMessageStateEventDto stateEvent =
                new org.radarbase.appserver.event.state.DataMessageStateEventDto(
                        this,
                        dataMessage,
                        dataMessageStateEventDto.getState(),
                        additionalInfo,
                        dataMessageStateEventDto.getTime());
        dataMessageApplicationEventPublisher.publishEvent(stateEvent);
    }

    private void checkState(long dataMessageId, MessageState state)
            throws SizeLimitExceededException, IllegalStateException {
        if (EXTERNAL_EVENTS.contains(state)) {
            if (dataMessageStateEventRepository.countByDataMessageId(dataMessageId)
                    >= MAX_NUMBER_OF_STATES) {
                throw new SizeLimitExceededException(
                        "The max limit of state changes("
                                + MAX_NUMBER_OF_STATES
                                + ") has been reached. Cannot add new states.");
            }
        } else {
            throw new IllegalStateException(
                    "The state "
                            + state
                            + " is not an external state and cannot be updated by this endpoint.");
        }
    }
}
