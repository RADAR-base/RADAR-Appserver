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

package org.radarbase.appserver.dto.fcm;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.radarbase.appserver.entity.Notification;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

/** @author yatharthranjan */
@Getter
@ToString
@NoArgsConstructor
public class FcmNotificationDto implements Serializable {

  private static final long serialVersionUID = 3L;

  private Long id;

  @NotNull
  @DateTimeFormat(iso = ISO.DATE_TIME)
  private Instant scheduledTime;

  private boolean delivered;

  @NotEmpty private String title;

  private String body;

  private int ttlSeconds;

  @NotEmpty private String sourceId;

  private String fcmMessageId;

  @NotEmpty private String type;

  @NotEmpty private String appPackage;

  @NotEmpty private String sourceType;

  private Map<String, String> additionalData;

  @DateTimeFormat(iso = ISO.DATE_TIME)
  private Instant createdAt;

  @DateTimeFormat(iso = ISO.DATE_TIME)
  private Instant updatedAt;

  public FcmNotificationDto(Notification notificationEntity) {
    this.id = notificationEntity.getId();
    this.scheduledTime = notificationEntity.getScheduledTime();
    this.title = notificationEntity.getTitle();
    this.body = notificationEntity.getBody();
    this.delivered = notificationEntity.isDelivered();
    this.fcmMessageId = notificationEntity.getFcmMessageId();
    this.sourceId = notificationEntity.getSourceId();
    this.type = notificationEntity.getType();
    this.appPackage = notificationEntity.getAppPackage();
    this.sourceType = notificationEntity.getSourceType();
    this.ttlSeconds = notificationEntity.getTtlSeconds();
    this.additionalData = notificationEntity.getAdditionalData();
    if (notificationEntity.getCreatedAt() != null) {
      this.createdAt = notificationEntity.getCreatedAt().toInstant();
    }
    if (notificationEntity.getUpdatedAt() != null) {
      this.updatedAt = notificationEntity.getUpdatedAt().toInstant();
    }
  }

  public FcmNotificationDto setCreatedAt(Instant createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  public FcmNotificationDto setUpdatedAt(Instant updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  public FcmNotificationDto setId(Long id) {
    this.id = id;
    return this;
  }

  public FcmNotificationDto setScheduledTime(Instant scheduledTime) {
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

  public FcmNotificationDto setAppPackage(String appPackage) {
    this.appPackage = appPackage;
    return this;
  }

  public FcmNotificationDto setSourceType(String sourceType) {
    this.sourceType = sourceType;
    return this;
  }

  public FcmNotificationDto setAdditionalData(Map<String, String> additionalData) {
    this.additionalData = additionalData;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FcmNotificationDto)) return false;
    FcmNotificationDto that = (FcmNotificationDto) o;
    return delivered == that.delivered
        && ttlSeconds == that.ttlSeconds
        && Objects.equals(scheduledTime, that.scheduledTime)
        && Objects.equals(title, that.title)
        && Objects.equals(body, that.body)
        && Objects.equals(type, that.type)
        && Objects.equals(appPackage, that.appPackage)
        && Objects.equals(sourceType, that.sourceType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        scheduledTime, delivered, title, body, ttlSeconds, type, appPackage, sourceType);
  }
}