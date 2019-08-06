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

package org.radarbase.appserver.service.fcm;

import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.dto.ProjectDto;
import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.radarbase.appserver.dto.fcm.FcmUserDto;
import org.radarbase.appserver.event.state.NotificationState;
import org.radarbase.appserver.event.state.NotificationStateEvent;
import org.radarbase.appserver.exception.AlreadyExistsException;
import org.radarbase.appserver.exception.InvalidNotificationDetailsException;
import org.radarbase.appserver.exception.NotFoundException;
import org.radarbase.appserver.service.FcmNotificationService;
import org.radarbase.appserver.service.ProjectService;
import org.radarbase.appserver.service.UserService;
import org.radarbase.fcm.upstream.UpstreamMessageHandler;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link UpstreamMessageHandler} for handling messages messages coming through the {@link
 * org.radarbase.fcm.upstream.XmppFcmReceiver}
 *
 * @see org.radarbase.fcm.upstream.XmppFcmReceiver
 * @see UpstreamMessageHandler
 * @author yatharthranjan
 */
@Service
@Slf4j
public class FcmMessageReceiverService implements UpstreamMessageHandler {

  // TODO: Add batching of schedule requests (The database service function is already there)

  private final transient ApplicationEventPublisher notificationStateEventPublisher;
  private transient FcmNotificationService notificationService;
  private transient UserService userService;
  private transient ProjectService projectService;

  public FcmMessageReceiverService(
      FcmNotificationService notificationService,
      UserService userService,
      ProjectService projectService,
      ApplicationEventPublisher notificationStateEventPublisher) {
    this.notificationService = notificationService;
    this.userService = userService;
    this.projectService = projectService;
    this.notificationStateEventPublisher = notificationStateEventPublisher;
  }

  /**
   * Performs {@link Action} based on the value supplied by the {@link
   * org.jivesoftware.smack.packet.Stanza} through the {@link
   * org.radarbase.fcm.upstream.XmppFcmReceiver}.
   *
   * @param jsonMessage The {@link JsonNode} containing the data from an upstream XMPP message
   *     request.
   */
  @Override
  public void handleUpstreamMessage(JsonNode jsonMessage) {
    log.info("Normal Message: {}", jsonMessage.toString());

    Optional<JsonNode> jsonData = Optional.ofNullable(jsonMessage.get("data"));

    jsonData.ifPresentOrElse(
        (JsonNode data) -> {
          Optional<JsonNode> action = Optional.ofNullable(data.get("action"));

          action.ifPresentOrElse(
              act -> {
                switch (Action.valueOf(act.asText())) {
                  case ECHO:
                    log.info("Got an ECHO request");
                    break;

                  case SCHEDULE:
                    log.info("Got a SCHEDULE Request");
                    handleScheduleNotification(
                        notificationDtoMapper(data),
                        userDtoMapper(jsonMessage.get("from").asText(), data),
                        jsonMessage.get("projectId") == null
                            ? "unknown-project"
                            : jsonMessage.get("projectId").asText());
                    break;

                  case CANCEL:
                    log.info("Got a CANCEL Request");
                    break;
                }
              },
              () -> {
                log.warn("No Action provided");
                throw new IllegalStateException(
                    "Action must not be null! Options: 'ECHO', 'SCHEDULE', 'CANCEL'");
              });
        },
        () -> {
          log.warn("No Data provided");
          throw new IllegalStateException("Data must not be null!");
        });
  }

  @Override
  public void handleAckReceipt(JsonNode jsonMessage) {
    log.info("Ack Receipt: {}", jsonMessage.toString());
  }

  @Override
  public void handleNackReceipt(JsonNode jsonMessage) {
    log.warn("Nack Receipt: {}", jsonMessage.toString());

    Optional<String> errorCodeObj = Optional.ofNullable(jsonMessage.get("error").asText());
    if (errorCodeObj.isEmpty()) {
      log.error("Received null FCM Error Code.");
      return;
    }

    final String errorCode = errorCodeObj.get();

    if (errorCode.equals("DEVICE_UNREGISTERED")) {
      log.info("The FCM device unregistered. Removing all notifications: {}", errorCode);
      this.notificationService.removeNotificationsForUserUsingFcmToken(
          jsonMessage.get("from").asText());
    } else {
      Map<String, String> additionalInfo = new HashMap<>();
      additionalInfo.put("error", jsonMessage.get("error").asText());
      additionalInfo.put("error_description", jsonMessage.get("error_description").asText());
      NotificationStateEvent notificationStateEvent =
          new NotificationStateEvent(
              this,
              notificationService.getNotificationByMessageId(
                  jsonMessage.get("message_id").asText()),
              NotificationState.ERRORED,
              null,
              Instant.now());
      notificationStateEventPublisher.publishEvent(notificationStateEvent);
    }
  }

  @Override
  public void handleStatusReceipt(JsonNode jsonMessage) {
    log.info("Status Receipt: {}", jsonMessage.toString());
    Optional<JsonNode> jsonData = Optional.ofNullable(jsonMessage.get("data"));
    if (jsonData.isPresent()) {
      Optional<String> messageStatus =
          Optional.ofNullable(jsonData.get().get("message_status").asText());
      if (messageStatus.isPresent()) {
        if (messageStatus.get().equals("MESSAGE_SENT_TO_DEVICE")) {
          // notificationService.deleteNotificationByFcmMessageId(jsonData.get().get("original_message_id").asText());
          notificationService.updateDeliveryStatus(
              jsonData.get().get("original_message_id").asText(), true);

          NotificationStateEvent notificationStateEvent =
              new NotificationStateEvent(
                  this,
                  notificationService.getNotificationByMessageId(
                      jsonData.get().get("original_message_id").asText()),
                  NotificationState.DELIVERED,
                  null,
                  Instant.now());
          notificationStateEventPublisher.publishEvent(notificationStateEvent);


          userService.updateLastDelivered(
              jsonData.get().get("device_registration_id").asText(), Instant.now());
        }
      }
    }
  }

  @Override
  public void handleControlMessage(JsonNode jsonMessage) {
    log.info("Control Message: {}", jsonMessage.toString());
  }

  @Override
  public void handleOthers(JsonNode jsonMessage) {
    log.debug("Message Type not recognised {}", jsonMessage.toString());
  }

  @SneakyThrows
  private FcmNotificationDto notificationDtoMapper(JsonNode jsonMessage) {

    if (jsonMessage.get("notificationTitle") == null
        || jsonMessage.get("notificationMessage") == null
        || jsonMessage.get("time") == null) {
      throw new InvalidNotificationDetailsException(
          "The notifications details are invalid: " + jsonMessage);
    }

    Instant scheduledTime = Instant.ofEpochSecond(jsonMessage.get("time").asLong() / 1000L);

    if (scheduledTime.isBefore(Instant.now())) {
      throw new InvalidNotificationDetailsException(
          "The notification scheduled " + "time cannot be before current time");
    }

    return new FcmNotificationDto()
        .setTitle(jsonMessage.get("notificationTitle").asText())
        .setBody(jsonMessage.get("notificationMessage").asText())
        .setScheduledTime(scheduledTime)
        .setAppPackage(
            jsonMessage.get("appPackage") == null
                ? "unknown"
                : jsonMessage.get("appPackage").asText())
        .setDelivered(false)
        .setSourceId(
            jsonMessage.get("sourceId") == null ? "unknown" : jsonMessage.get("sourceId").asText())
        .setSourceType(
            jsonMessage.get("sourceType") == null
                ? "unknown"
                : jsonMessage.get("sourceType").asText())
        .setTtlSeconds(
            jsonMessage.get("ttlSeconds") == null ? 86400 : jsonMessage.get("ttlSeconds").asInt())
        .setType(jsonMessage.get("type") == null ? "Unknown" : jsonMessage.get("type").asText());
  }

  private FcmUserDto userDtoMapper(String fcmToken, JsonNode jsonMessage) {
    return new FcmUserDto()
        .setFcmToken(fcmToken)
        .setSubjectId(
            jsonMessage.get("subjectId") == null
                ? "unknown-user"
                : jsonMessage.get("subjectId").asText())
        .setLanguage(
            jsonMessage.get("language") == null ? "en" : jsonMessage.get("subjectId").asText())
        .setEnrolmentDate(
            jsonMessage.get("enrolmentDate") == null
                ? Instant.now()
                : Instant.ofEpochSecond(jsonMessage.get("enrolmentDate").asLong() / 1000L));
  }

  @Transactional
  private void handleScheduleNotification(
      FcmNotificationDto notificationDto, FcmUserDto userDto, String projectId) {
    try {
      notificationService.addNotification(notificationDto, userDto.getSubjectId(), projectId);
    } catch (NotFoundException ex) {
      if (ex.getMessage().contains("Project")) {
        try {
          projectService.addProject(new ProjectDto().setProjectId(projectId));
          userService.saveUserInProject(userDto.setProjectId(projectId));
        } catch (Exception e) {
          log.warn("Exception while adding notification.", ex);
        }
      } else if (ex.getMessage().contains("Subject")) {
        userService.saveUserInProject(userDto.setProjectId(projectId));
      }
      notificationService.addNotification(notificationDto, userDto.getSubjectId(), projectId);
    } catch (AlreadyExistsException ex) {
      log.warn("The Notification Already Exists.", ex);
    }
  }
}
