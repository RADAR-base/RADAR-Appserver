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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.naming.SizeLimitExceededException;
import org.radarbase.appserver.dto.NotificationStateEventDto;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.entity.NotificationStateEvent;
import org.radarbase.appserver.event.state.NotificationState;
import org.radarbase.appserver.repository.NotificationStateEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class NotificationStateEventService {

  private static final Set<NotificationState> EXTERNAL_EVENTS = new HashSet<>();
  private static final int MAX_NUMBER_OF_STATES = 20;

  static {
    EXTERNAL_EVENTS.add(NotificationState.DELIVERED);
    EXTERNAL_EVENTS.add(NotificationState.DISMISSED);
    EXTERNAL_EVENTS.add(NotificationState.OPENED);
    EXTERNAL_EVENTS.add(NotificationState.UNKNOWN);
    EXTERNAL_EVENTS.add(NotificationState.ERRORED);
  }

  private final transient NotificationStateEventRepository notificationStateEventRepository;
  private final transient FcmNotificationService notificationService;
  private final transient ApplicationEventPublisher notificationApplicationEventPublisher;
  private final transient ObjectMapper objectMapper;

  public NotificationStateEventService(
      NotificationStateEventRepository notificationStateEventRepository,
      FcmNotificationService fcmNotificationService,
      ApplicationEventPublisher notificationApplicationEventPublisher,
      ObjectMapper objectMapper) {
    this.notificationStateEventRepository = notificationStateEventRepository;
    this.notificationService = fcmNotificationService;
    this.notificationApplicationEventPublisher = notificationApplicationEventPublisher;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public void addNotificationStateEvent(NotificationStateEvent notificationStateEvent) {
    notificationStateEventRepository.save(notificationStateEvent);
  }

  @Transactional(readOnly = true)
  public List<NotificationStateEventDto> getNotificationStateEvents(
      String projectId, String subjectId, long notificationId) {
    notificationService.getNotificationByProjectIdAndSubjectIdAndNotificationId(
        projectId, subjectId, notificationId);
    List<NotificationStateEvent> stateEvents =
        notificationStateEventRepository.findByNotificationId(notificationId);
    return stateEvents.stream()
        .map(
            ns ->
                new NotificationStateEventDto(
                    ns.getId(),
                    ns.getNotification().getId(),
                    ns.getState(),
                    ns.getTime(),
                    ns.getAssociatedInfo()))
        .collect(Collectors.toList());
  }

  @Transactional(readOnly = true)
  public List<NotificationStateEventDto> getNotificationStateEventsByNotificationId(
      long notificationId) {
    List<NotificationStateEvent> stateEvents =
        notificationStateEventRepository.findByNotificationId(notificationId);
    return stateEvents.stream()
        .map(
            ns ->
                new NotificationStateEventDto(
                    ns.getId(),
                    ns.getNotification().getId(),
                    ns.getState(),
                    ns.getTime(),
                    ns.getAssociatedInfo()))
        .collect(Collectors.toList());
  }

  @Transactional
  public void publishNotificationStateEventExternal(
      String projectId,
      String subjectId,
      long notificationId,
      NotificationStateEventDto notificationStateEventDto)
      throws SizeLimitExceededException {
    if (EXTERNAL_EVENTS.contains(notificationStateEventDto.getState())) {
      if (notificationStateEventRepository.countByNotificationId(notificationId)
          >= MAX_NUMBER_OF_STATES) {
        throw new SizeLimitExceededException(
            "The max limit of state changes("
                + MAX_NUMBER_OF_STATES
                + ") has been reached. Cannot add new states.");
      }
      Notification notification =
          notificationService.getNotificationByProjectIdAndSubjectIdAndNotificationId(
              projectId, subjectId, notificationId);

      Map<String, String> additionalInfo = null;
      if (!notificationStateEventDto.getAssociatedInfo().isEmpty()) {
        try {
          additionalInfo =
              objectMapper.readValue(
                  notificationStateEventDto.getAssociatedInfo(),
                  new TypeReference<Map<String, String>>() {});
        } catch (IOException exc) {
          throw new IllegalStateException(
              "Cannot convert additionalInfo to Map<String, String>. Please check its format.");
        }
      }

      org.radarbase.appserver.event.state.NotificationStateEvent stateEvent =
          new org.radarbase.appserver.event.state.NotificationStateEvent(
              this,
              notification,
              notificationStateEventDto.getState(),
              additionalInfo,
              notificationStateEventDto.getTime());
      notificationApplicationEventPublisher.publishEvent(stateEvent);
    } else {
      throw new IllegalStateException(
          "The state "
              + notificationStateEventDto.getState()
              + " is not an external state and cannot be updated by this endpoint.");
    }
  }
}
