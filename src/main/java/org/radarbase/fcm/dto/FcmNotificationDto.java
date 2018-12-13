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

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.radarbase.appserver.entity.Notification;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * @author yatharthranjan
 */
@Getter
@EqualsAndHashCode
@ToString
public class FcmNotificationDto implements Serializable {

    private static final long serialVersionUID = 3L;

    private Long id;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime scheduledTime;

    private boolean delivered;

    @NotNull
    private String title;

    private String body;

    private int ttlSeconds;

    @NotNull
    private String sourceId;

    private String fcmMessageId;

    @NotNull
    private String type;

    @NotNull
    private String appPackage;

    @NotNull
    private String sourceType;

    public FcmNotificationDto(Notification notificationEntity) {
        this.id = notificationEntity.getId();
        this.scheduledTime = LocalDateTime.ofInstant(notificationEntity.getScheduledTime(), ZoneOffset.UTC);
        this.title = notificationEntity.getTitle();
        this.body = notificationEntity.getBody();
        this.delivered = notificationEntity.isDelivered();
        this.fcmMessageId = notificationEntity.getFcmMessageId();
        this.sourceId = notificationEntity.getSourceId();
        this.type = notificationEntity.getType();
        this.appPackage = notificationEntity.getAppPackage();
        this.sourceType = notificationEntity.getSourceType();
        this.ttlSeconds = notificationEntity.getTtlSeconds();
    }

    public FcmNotificationDto() {

    }

    public FcmNotificationDto setId(Long id) {
        this.id = id;
        return this;
    }

    public FcmNotificationDto setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
        return this;
    }

    public FcmNotificationDto setDelivered(boolean delivered) {
        this.delivered = delivered;
        return this;
    }

    public FcmNotificationDto setTitle(String title) {
        this.title = title;
        return this;
    }

    public FcmNotificationDto setBody(String body) {
        this.body = body;
        return this;
    }

    public FcmNotificationDto setTtlSeconds(int ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
        return this;
    }

    public FcmNotificationDto setFcmMessageId(String fcmMessageId) {
        this.fcmMessageId = fcmMessageId;
        return this;
    }

    public FcmNotificationDto setSourceId(String sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public FcmNotificationDto setType(String type) {
        this.type = type;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FcmNotificationDto)) return false;
        FcmNotificationDto that = (FcmNotificationDto) o;
        return delivered == that.delivered &&
                ttlSeconds == that.ttlSeconds &&
                Objects.equals(scheduledTime, that.scheduledTime) &&
                Objects.equals(title, that.title) &&
                Objects.equals(body, that.body) &&
                Objects.equals(type, that.type) &&
                Objects.equals(appPackage, that.appPackage) &&
                Objects.equals(sourceType, that.sourceType);
    }

    public String getAppPackage() {
        return appPackage;
    }

    public FcmNotificationDto setAppPackage(String appPackage) {
        this.appPackage = appPackage;
        return this;
    }

    public String getSourceType() {
        return sourceType;
    }

    public FcmNotificationDto setSourceType(String sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(scheduledTime, delivered, title, body, ttlSeconds, type, appPackage, sourceType);
    }
}
