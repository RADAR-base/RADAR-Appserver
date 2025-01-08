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

package org.radarbase.appserver.service.transmitter;

import com.google.firebase.ErrorCode;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.dto.fcm.FcmUserDto;
import org.radarbase.appserver.entity.DataMessage;
import org.radarbase.appserver.entity.Message;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.exception.FcmMessageTransmitException;
import org.radarbase.appserver.service.FcmDataMessageService;
import org.radarbase.appserver.service.FcmNotificationService;
import org.radarbase.appserver.service.UserService;
import org.radarbase.fcm.downstream.FcmSender;
import org.radarbase.fcm.model.FcmDataMessage;
import org.radarbase.fcm.model.FcmNotificationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Component
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class FcmTransmitter implements NotificationTransmitter, DataMessageTransmitter {

    protected static final boolean IS_DELIVERY_RECEIPT_REQUESTED = true;
    static final int DEFAULT_TIME_TO_LIVE = 2_419_200; // 4 weeks

    protected final transient FcmSender fcmSender;
    private final transient FcmNotificationService notificationService;
    private final transient FcmDataMessageService dataMessageService;
    private final transient UserService userService;

    public FcmTransmitter(
        @Autowired @Qualifier("fcmSenderProps") FcmSender fcmSender,
        @Autowired FcmNotificationService notificationService,
        @Autowired FcmDataMessageService dataMessageService,
        @Autowired UserService userService
        ) {
        this.fcmSender = fcmSender;
        this.notificationService = notificationService;
        this.dataMessageService = dataMessageService;
        this.userService = userService;
    }

    @Override
    @SneakyThrows
    public void send(Notification notification) {
        try {
            fcmSender.send(createMessageFromNotification(notification));
        }  catch (FirebaseMessagingException exc) {
            handleFcmException(exc, notification);
        } catch (Exception exc) {
            throw new FcmMessageTransmitException("Could not transmit a notification through Fcm", exc);
        }
    }

    @Override
    @SneakyThrows
    public void send(DataMessage dataMessage) {
        try {
            fcmSender.send(createMessageFromDataMessage(dataMessage));
        } catch (FirebaseMessagingException exc) {
            handleFcmException(exc, dataMessage);
        } catch (Exception exc) {
            throw new FcmMessageTransmitException("Could not transmit a data message through Fcm", exc);
        }
    }

    private void handleFcmException(FirebaseMessagingException exc, Message message) {
        log.error("Error occurred when sending downstream message.", exc);
        if (message != null) {
            handleErrorCode(exc.getErrorCode(), message);
            handleFCMErrorCode(exc.getMessagingErrorCode(), message);
        }
    }

    private static FcmNotificationMessage createMessageFromNotification(Notification notification) {
        String to =
            Objects.requireNonNullElseGet(
                notification.getFcmTopic(), notification.getUser()::getFcmToken);
        return new FcmNotificationMessage.Builder()
            .to(to)
            .condition(notification.getFcmCondition())
            .priority(notification.getPriority())
            .mutableContent(notification.getMutableContent())
            .deliveryReceiptRequested(IS_DELIVERY_RECEIPT_REQUESTED)
            .messageId(String.valueOf(notification.getFcmMessageId()))
            .timeToLive(Objects.requireNonNullElse(notification.getTtlSeconds(), DEFAULT_TIME_TO_LIVE))
            .notification(getNotificationMap(notification))
            .data(notification.getAdditionalData())
            .build();
    }

    private static FcmDataMessage createMessageFromDataMessage(DataMessage dataMessage) {
        String to =
            Objects.requireNonNullElseGet(
                dataMessage.getFcmTopic(), dataMessage.getUser()::getFcmToken);
        return new FcmDataMessage.Builder()
            .to(to)
            .condition(dataMessage.getFcmCondition())
            .priority(dataMessage.getPriority())
            .mutableContent(dataMessage.getMutableContent())
            .deliveryReceiptRequested(IS_DELIVERY_RECEIPT_REQUESTED)
            .messageId(String.valueOf(dataMessage.getFcmMessageId()))
            .timeToLive(Objects.requireNonNullElse(dataMessage.getTtlSeconds(), DEFAULT_TIME_TO_LIVE))
            .data(dataMessage.getDataMap())
            .build();
    }

    private static Map<String, Object> getNotificationMap(Notification notification) {
        Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("body", notification.getBody());
        notificationMap.put("title", notification.getTitle());
        notificationMap.put("sound", "default");

        putIfNotNull(notificationMap, "sound", notification.getSound());
        putIfNotNull(notificationMap, "badge", notification.getBadge());
        putIfNotNull(notificationMap, "click_action", notification.getClickAction());
        putIfNotNull(notificationMap, "subtitle", notification.getSubtitle());
        putIfNotNull(notificationMap, "body_loc_key", notification.getBodyLocKey());
        putIfNotNull(notificationMap, "body_loc_args", notification.getBodyLocArgs());
        putIfNotNull(notificationMap, "title_loc_key", notification.getTitleLocKey());
        putIfNotNull(notificationMap, "title_loc_args", notification.getTitleLocArgs());
        putIfNotNull(notificationMap, "android_channel_id", notification.getAndroidChannelId());
        putIfNotNull(notificationMap, "icon", notification.getIcon());
        putIfNotNull(notificationMap, "tag", notification.getTag());
        putIfNotNull(notificationMap, "color", notification.getColor());

        return notificationMap;
    }

    protected static void putIfNotNull(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    protected void handleErrorCode(ErrorCode errorCode, Message message) {
        // More info on ErrorCode: https://firebase.google.com/docs/reference/fcm/rest/v1/ErrorCode
        switch (errorCode) {
            case INVALID_ARGUMENT:
            case INTERNAL:
            case ABORTED:
            case CONFLICT:
            case CANCELLED:
            case DATA_LOSS:
            case NOT_FOUND:
            case OUT_OF_RANGE:
            case ALREADY_EXISTS:
            case DEADLINE_EXCEEDED:
            case PERMISSION_DENIED:
            case RESOURCE_EXHAUSTED:
            case FAILED_PRECONDITION:
            case UNAUTHENTICATED:
            case UNKNOWN:
                break;
            case UNAVAILABLE:
                // TODO: Could schedule for retry.
                log.warn("The FCM service is unavailable.");
                break;
        }
    }

    protected void handleFCMErrorCode(MessagingErrorCode errorCode, Message message) {
        switch (errorCode) {
            case INTERNAL:
            case QUOTA_EXCEEDED:
            case INVALID_ARGUMENT:
            case SENDER_ID_MISMATCH:
            case THIRD_PARTY_AUTH_ERROR:
                break;
            case UNAVAILABLE:
                // TODO: Could schedule for retry.
                log.warn("The FCM service is unavailable.");
                break;
            case UNREGISTERED:
                FcmUserDto userDto = new FcmUserDto(message.getUser());
                log.warn("The Device for user {} was unregistered.", userDto.getSubjectId());
                notificationService.removeNotificationsForUser(
                    userDto.getProjectId(), userDto.getSubjectId());
                dataMessageService.removeDataMessagesForUser(
                    userDto.getProjectId(), userDto.getSubjectId());
                userService.checkFcmTokenExistsAndReplace(userDto);
                break;
        }
    }

}
