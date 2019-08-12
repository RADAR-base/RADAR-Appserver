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
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.service.NotificationStateEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationStateEventListener {

  @Autowired private transient ObjectMapper objectMapper;
  @Autowired private transient NotificationStateEventService notificationStateEventService;
  /**
   * Handle an application event. Async so will return immediately.
   *
   * @param event the event to respond to
   */
  @Async
  @EventListener(value = NotificationStateEvent.class)
  @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
  public void onNotificationStateChange(NotificationStateEvent event) {
    String info = convertMapToString(event.getAdditionalInfo());
    log.debug("ID: {}, STATE: {}", event.getNotification().getId(), event.getState());
    org.radarbase.appserver.entity.NotificationStateEvent eventEntity =
        new org.radarbase.appserver.entity.NotificationStateEvent(
            event.getNotification(), event.getState(), event.getTime(), info);
    notificationStateEventService.addNotificationStateEvent(eventEntity);
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
