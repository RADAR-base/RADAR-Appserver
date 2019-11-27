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

package org.radarbase.appserver.service.scheduler;

import java.util.*;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.radarbase.appserver.entity.Message;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.service.scheduler.quartz.*;
import org.radarbase.fcm.downstream.FcmSender;
import org.radarbase.fcm.model.FcmNotificationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * {@link Service} for scheduling Notifications to be sent through FCM at the {@link
 * org.radarbase.appserver.entity.Scheduled} time. It also provided functions for updating/ deleting
 * already scheduled Notification Jobs.
 *
 * @author yatharthranjan
 */
@Service
@Slf4j
public class NotificationSchedulerService extends MessageSchedulerService {

    public NotificationSchedulerService(
            @Autowired @Qualifier("fcmSenderProps") FcmSender fcmSender,
            @Autowired SchedulerService schedulerService) {
        super(fcmSender, schedulerService);
    }

    private static Map getNotificationMap(Notification notification) {
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

    private static FcmNotificationMessage createMessageFromNotification(Notification notification) {

        String to =
                Objects.requireNonNullElseGet(
                        notification.getFcmTopic(), notification.getUser()::getFcmToken);
        return FcmNotificationMessage.builder()
                .to(to)
                .condition(notification.getFcmCondition())
                .priority(notification.getPriority())
                .mutableContent(notification.isMutableContent())
                .deliveryReceiptRequested(IS_DELIVERY_RECEIPT_REQUESTED)
                .messageId(String.valueOf(notification.getFcmMessageId()))
                .timeToLive(Objects.requireNonNullElse(notification.getTtlSeconds(), 2_419_200))
                .notification(getNotificationMap(notification))
                .data(notification.getAdditionalData())
                .build();
    }


    public void send(Message notification) throws Exception {
        if (notification instanceof Notification)
            fcmSender.send(createMessageFromNotification((Notification) notification));
    }
}
