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

package org.radarbase.fcm.dto;

import org.radarbase.appserver.entity.Notification;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author yatharthranjan
 */
public class FcmNotificationDto implements Serializable {

    private static final long serialVersionUID = 3L;

    private Long id;

    private LocalDateTime scheduledTime;

    private boolean delivered;

    private String title;

    private String body;

    private int ttlSeconds;

    private String fcmToken;

    private String fcmMessageId;

    public FcmNotificationDto(Notification notificationEntity) {
        this.id = notificationEntity.getId();
        this.scheduledTime = LocalDateTime.ofInstant(notificationEntity.getScheduledTime(), ZoneOffset.UTC);
        this.title = notificationEntity.getTitle();
        this.body = notificationEntity.getBody();
        this.delivered = notificationEntity.isDelivered();
        this.fcmMessageId = notificationEntity.getFcmMessageId();
        this.fcmToken = notificationEntity.getUser().getFcmToken();
    }
}
