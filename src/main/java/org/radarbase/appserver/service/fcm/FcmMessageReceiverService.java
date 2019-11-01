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
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.radarbase.appserver.dto.fcm.FcmUserDto;
import org.radarbase.appserver.event.state.NotificationState;
import org.radarbase.appserver.event.state.NotificationStateEvent;
import org.radarbase.appserver.exception.InvalidNotificationDetailsException;
import org.radarbase.appserver.service.FcmNotificationService;
import org.radarbase.appserver.service.ProjectService;
import org.radarbase.appserver.service.UserService;
import org.radarbase.fcm.upstream.UpstreamMessageHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

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
  private transient ScheduleNotificationHandler scheduleNotificationHandler;
  private static final String MESSAGE_STATUS_DELIVERED = "MESSAGE_SENT_TO_DEVICE";

  public FcmMessageReceiverService(
      FcmNotificationService notificationService,
      UserService userService,
      ApplicationEventPublisher notificationStateEventPublisher,
      ScheduleNotificationHandler scheduleNotificationHandler) {
    this.notificationService = notificationService;
    this.userService = userService;
    this.notificationStateEventPublisher = notificationStateEventPublisher;
    this.scheduleNotificationHandler = scheduleNotificationHandler;
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
                    scheduleNotificationHandler.handleScheduleNotification(
                        notificationDtoMapper(data),
                        userDtoMapper(jsonMessage.get("from").asText(), data),
                        data.get("projectId") == null
                            ? "unknown-project"
                            : data.get("projectId").asText());
                    break;

                  case CANCEL:
                    log.info("Got a CANCEL Request");
                    // TODO: Don't delete but change the state to CANCELLED.
                    this.notificationService.removeNotificationsForUserUsingFcmToken(
                        jsonMessage.get("from").asText());
                    break;

                  default:
                    throw new IllegalStateException(
                        "Action not Supported! Options: " + Arrays.toString(Action.values()));
                }
              },
              () -> {
                log.warn("No Action provided");
                throw new IllegalStateException(
                    "Action must not be null! Options: " + Arrays.toString(Action.values()));
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

    if ("DEVICE_UNREGISTERED".equals(errorCode)) {
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
              additionalInfo,
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
      if (messageStatus.isPresent() && MESSAGE_STATUS_DELIVERED.equals(messageStatus.get())) {
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

    checkRequiredExistElseThrow(jsonMessage);

    Instant scheduledTime = Instant.ofEpochSecond(jsonMessage.get("time").asLong() / 1000L);
    checkValidTimeElseThrow(scheduledTime);

    return new FcmNotificationDto()
        .setTitle(jsonMessage.get("notificationTitle").asText())
        .setBody(jsonMessage.get("notificationMessage").asText())
        .setScheduledTime(scheduledTime)
        .setAppPackage(getTextIfExistsElseUnknown(jsonMessage.get("appPackage")))
        .setDelivered(false)
        .setSourceId(getTextIfExistsElseUnknown(jsonMessage.get("sourceId")))
        .setSourceType(getTextIfExistsElseUnknown(jsonMessage.get("sourceType")))
        .setTtlSeconds(
            jsonMessage.get("ttlSeconds") == null ? 86400 : jsonMessage.get("ttlSeconds").asInt())
        .setType(getTextIfExistsElseUnknown(jsonMessage.get("type")));
  }

  private void checkRequiredExistElseThrow(JsonNode jsonMessage) {
    if (jsonMessage.get("notificationTitle") == null
        || jsonMessage.get("notificationMessage") == null
        || jsonMessage.get("time") == null) {
      throw new InvalidNotificationDetailsException(
          "The notifications details are invalid: " + jsonMessage);
    }
  }

  private void checkValidTimeElseThrow(Instant scheduledTime) {
    if (scheduledTime.isBefore(Instant.now())) {
      throw new InvalidNotificationDetailsException(
          "The notification scheduled " + "time cannot be before current time");
    }
  }

  private String getTextIfExistsElseUnknown(JsonNode jsonNode) {
    return jsonNode == null ? "unknown" : jsonNode.asText();
  }

  private FcmUserDto userDtoMapper(String fcmToken, JsonNode jsonMessage) {
    return new FcmUserDto()
        .setFcmToken(fcmToken)
        .setSubjectId(
            jsonMessage.get("subjectId") == null
                ? "unknown-user"
                : jsonMessage.get("subjectId").asText())
        .setLanguage(
            jsonMessage.get("language") == null ? "en" : jsonMessage.get("language").asText())
        .setEnrolmentDate(
            jsonMessage.get("enrolmentDate") == null
                ? Instant.now()
                : Instant.ofEpochSecond(jsonMessage.get("enrolmentDate").asLong() / 1000L));
  }

  @Configuration
  public static class ScheduleNotificationHandlerConfig {

    private final transient FcmNotificationService notificationService;
    private final transient UserService userService;
    private final transient ProjectService projectService;
    @Value("${fcm.xmpp.schedule.batch.maxSize:100}")
    private transient int maxSize;
    @Value("${fcm.xmpp.schedule.batch.expiry.seconds:50}")
    private transient int expiryInSeconds;
    @Value("${fcm.xmpp.schedule.batch.flushAfter.seconds:120}")
    private transient int flushAfterSeconds;

    public ScheduleNotificationHandlerConfig(
        FcmNotificationService notificationService,
        UserService userService,
        ProjectService projectService) {
      this.notificationService = notificationService;
      this.userService = userService;
      this.projectService = projectService;
    }

    @Bean
    public ScheduleNotificationHandler getScheduleNotificationHandler() {

      Duration expiry = Duration.ofSeconds(expiryInSeconds);
      //      return new SimpleScheduleNotificationHandler(
      //          notificationService, projectService, userService);
      return new BatchedScheduleNotificationHandler(
          notificationService, projectService, userService, maxSize, expiry, flushAfterSeconds);
    }
  }
}
