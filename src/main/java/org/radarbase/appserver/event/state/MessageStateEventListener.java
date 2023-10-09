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

package org.radarbase.appserver.event.state;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.event.state.dto.DataMessageStateEventDto;
import org.radarbase.appserver.event.state.dto.NotificationStateEventDto;
import org.radarbase.appserver.service.DataMessageStateEventService;
import org.radarbase.appserver.service.NotificationStateEventService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Slf4j
@Component
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class MessageStateEventListener {

    private final transient ObjectMapper objectMapper;
    private final transient NotificationStateEventService notificationStateEventService;
    private final transient DataMessageStateEventService dataMessageStateEventService;

    public MessageStateEventListener(ObjectMapper objectMapper,
                                     NotificationStateEventService notificationStateEventService,
                                     DataMessageStateEventService dataMessageStateEventService) {
        this.objectMapper = objectMapper;
        this.notificationStateEventService = notificationStateEventService;
        this.dataMessageStateEventService = dataMessageStateEventService;
    }


    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(classes = NotificationStateEventDto.class)
    @Async
    public void onNotificationStateChange(NotificationStateEventDto event) {
        String info = convertMapToString(event.getAdditionalInfo());
        log.debug("ID: {}, STATE: {}", event.getNotification().getId(), event.getState());
        org.radarbase.appserver.entity.NotificationStateEvent eventEntity =
                new org.radarbase.appserver.entity.NotificationStateEvent(
                        event.getNotification(), event.getState(), event.getTime(), info);
        notificationStateEventService.addNotificationStateEvent(eventEntity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(value = DataMessageStateEventDto.class)
    @Async
    public void onDataMessageStateChange(DataMessageStateEventDto event) {
        String info = convertMapToString(event.getAdditionalInfo());
        log.debug("ID: {}, STATE: {}", event.getDataMessage().getId(), event.getState());
        org.radarbase.appserver.entity.DataMessageStateEvent eventEntity =
                new org.radarbase.appserver.entity.DataMessageStateEvent(
                        event.getDataMessage(), event.getState(), event.getTime(), info);
        dataMessageStateEventService.addDataMessageStateEvent(eventEntity);
    }

    public String convertMapToString(Map<String, String> additionalInfoMap) {
        String info = null;
        if (additionalInfoMap != null) {
            try {
                info = objectMapper.writeValueAsString(additionalInfoMap);
            } catch (JsonProcessingException exc) {
                log.warn("error processing event's additional info: {}", additionalInfoMap);
            }
        }
        return info;
    }
    // we can add more event listeners by annotating with @EventListener
}
