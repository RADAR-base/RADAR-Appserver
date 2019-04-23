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

package org.radarbase.appserver.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.springframework.lang.Nullable;

/**
 * {@link Entity} for persisting notifications. The corresponding DTO is {@link FcmNotificationDto}.
 * This also includes information for scheduling the notification through the Firebase Cloud
 * Messaging(FCM) system.
 *
 * @see Scheduled
 * @see org.radarbase.appserver.service.scheduler.NotificationSchedulerService
 * @see org.radarbase.appserver.service.fcm.FcmMessageReceiverService
 * @author yatharthranjan
 */
@Table(
    name = "notifications",
    uniqueConstraints = {
      @UniqueConstraint(
          columnNames = {
            "user_id",
            "source_id",
            "scheduled_time",
            "title",
            "body",
            "type",
            "ttl_seconds",
            "delivered",
            "dry_run"
          })
    })
@Entity
@Getter
@ToString
public class Notification extends AuditModel implements Scheduled {

  // TODO Add STATUS as enum of (ADDED, SCHEDULED, CANCELLED, EXECUTED, DELIVERED, ERROR)

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @NotNull
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  @OnDelete(action = OnDeleteAction.CASCADE)
  @JsonIgnore
  private User user;

  @Nullable
  @Column(name = "source_id")
  private String sourceId;

  @NotNull
  @Column(name = "scheduled_time", nullable = false)
  private Instant scheduledTime;

  @NotNull
  @Column(nullable = false)
  private String title;

  private String body;

  // Type of notification. In terms of aRMT - PHQ8, RSES, ESM, etc.
  @Nullable private String type;

  @Column(name = "ttl_seconds")
  private int ttlSeconds;

  @Column(name = "fcm_message_id", unique = true)
  private String fcmMessageId;

  @Column(name = "fcm_topic")
  // for use with the FCM admin SDK
  @Nullable
  private String fcmTopic;

  @Nullable private boolean delivered;

  @Nullable private boolean validated;

  @Nullable
  @Column(name = "app_package")
  private String appPackage;

  // Source Type from the Management Portal
  @Nullable
  @Column(name = "source_type")
  private String sourceType;

  @Column(name = "dry_run")
  // for use with the FCM admin SDK
  @Nullable
  private boolean dryRun;

  @Nullable
  @ElementCollection
  @MapKeyColumn(name = "additional_key")
  @Column(name = "additional_value")
  private Map<String, String> additionalData;

  public Notification setUser(User user) {
    this.user = user;
    return this;
  }

  public Notification setSourceId(String sourceId) {
    this.sourceId = sourceId;
    return this;
  }

  public Notification setScheduledTime(Instant scheduledTime) {
    this.scheduledTime = scheduledTime;
    return this;
  }

  public Notification setTitle(String title) {
    this.title = title;
    return this;
  }

  public Notification setBody(String body) {
    this.body = body;
    return this;
  }

  public Notification setType(String type) {
    this.type = type;
    return this;
  }

  public Notification setTtlSeconds(int ttlSeconds) {
    this.ttlSeconds = ttlSeconds;
    return this;
  }

  public Notification setFcmMessageId(String fcmMessageId) {
    this.fcmMessageId = fcmMessageId;
    return this;
  }

  public Notification setFcmTopic(String fcmTopic) {
    this.fcmTopic = fcmTopic;
    return this;
  }

  public Notification setDelivered(boolean delivered) {
    this.delivered = delivered;
    return this;
  }

  public Notification setDryRun(boolean dryRun) {
    this.dryRun = dryRun;
    return this;
  }

  public Notification setValidated(boolean validated) {
    this.validated = validated;
    return this;
  }

  public Notification setAppPackage(String appPackage) {
    this.appPackage = appPackage;
    return this;
  }

  public Notification setSourceType(String sourceType) {
    this.sourceType = sourceType;
    return this;
  }

  public Notification setAdditionalData(Map<String, String> additionalData) {
    this.additionalData = additionalData;
    return this;
  }

  public Notification setId(Long id) {
    this.id = id;
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Notification)) return false;
    Notification that = (Notification) o;
    return getTtlSeconds() == that.getTtlSeconds()
        && isDelivered() == that.isDelivered()
        && isDryRun() == that.isDryRun()
        && Objects.equals(getUser(), that.getUser())
        && Objects.equals(getSourceId(), that.getSourceId())
        && Objects.equals(getScheduledTime(), that.getScheduledTime())
        && Objects.equals(getTitle(), that.getTitle())
        && Objects.equals(getBody(), that.getBody())
        && Objects.equals(getType(), that.getType())
        && Objects.equals(getAppPackage(), that.getAppPackage())
        && Objects.equals(getSourceType(), that.getSourceType());
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getUser(),
        getSourceId(),
        getScheduledTime(),
        getTitle(),
        getBody(),
        getType(),
        getTtlSeconds(),
        isDelivered(),
        isDryRun(),
        getAppPackage(),
        getSourceType());
  }
}
