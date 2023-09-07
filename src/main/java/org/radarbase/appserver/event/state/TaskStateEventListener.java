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
import org.radarbase.appserver.event.state.dto.NotificationStateEventDto;
import org.radarbase.appserver.event.state.dto.TaskStateEventDto;
import org.radarbase.appserver.service.TaskStateEventService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Map;

@Slf4j
@Component
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class TaskStateEventListener {

    private final transient ObjectMapper objectMapper;
    private final transient TaskStateEventService taskStateEventService;

    public TaskStateEventListener(ObjectMapper objectMapper,
                                  TaskStateEventService taskStateEventService) {
        this.objectMapper = objectMapper;
        this.taskStateEventService = taskStateEventService;
    }


    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(classes = TaskStateEventDto.class)
    @Async
    public void onTaskStateChange(TaskStateEventDto event) {
        String info = convertMapToString(event.getAdditionalInfo());
        log.debug("ID: {}, STATE: {}", event.getTask().getId(), event.getState());
        org.radarbase.appserver.entity.TaskStateEvent eventEntity =
                new org.radarbase.appserver.entity.TaskStateEvent(
                        event.getTask(), event.getState(), event.getTime(), info);
        taskStateEventService.addTaskStateEvent(eventEntity);
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
