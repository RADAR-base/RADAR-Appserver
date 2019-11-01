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
import java.io.Serializable;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
@Builder(toBuilder = true)
@NoArgsConstructor
@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public class Notification extends AuditModel implements Serializable, Scheduled {

  private static final long serialVersionUID = -367424816328519L;

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

  // for use with the FCM admin SDK
  @Column(name = "fcm_topic")
  @Nullable
  private String fcmTopic;

  // for use with the FCM admin SDK
  @Column(name = "fcm_condition")
  @Nullable
  private String fcmCondition;

  // TODO: REMOVE DELIVERED AND VALIDATED. These can be handled by state lifecycle.
  private boolean delivered;

  private boolean validated;

  @Nullable
  @Column(name = "app_package")
  private String appPackage;

  // Source Type from the Management Portal
  @Nullable
  @Column(name = "source_type")
  private String sourceType;

  @Column(name = "dry_run")
  // for use with the FCM admin SDK
  private boolean dryRun;

  @Nullable
  @ElementCollection(fetch = FetchType.EAGER)
  @MapKeyColumn(name = "additional_key", nullable = true)
  @Column(name = "additional_value")
  private Map<String, String> additionalData;

  private String priority;

  private String sound;

  // For IOS
  private String badge;

  // For IOS
  private String subtitle;

  // For android
  private String icon;

  // For android. Color of the icon
  private String color;

  @Column(name = "body_loc_key")
  private String bodyLocKey;

  @Column(name = "body_loc_args")
  private String bodyLocArgs;

  @Column(name = "title_loc_key")
  private String titleLocKey;

  @Column(name = "title_loc_args")
  private String titleLocArgs;

  // For android
  @Column(name = "android_channel_id")
  private String androidChannelId;

  // For android
  private String tag;

  @Column(name = "click_action")
  private String clickAction;

  @Column(name = "mutable_content")
  private boolean mutableContent;

  private Notification(
      Long id,
      @NotNull User user,
      @Nullable String sourceId,
      @NotNull Instant scheduledTime,
      @NotNull String title,
      String body,
      @Nullable String type,
      int ttlSeconds,
      String fcmMessageId,
      @Nullable String fcmTopic,
      @Nullable String fcmCondition,
      boolean delivered,
      boolean validated,
      @Nullable String appPackage,
      @Nullable String sourceType,
      boolean dryRun,
      @Nullable Map<String, String> additionalData,
      String priority,
      String sound,
      String badge,
      String subtitle,
      String icon,
      String color,
      String bodyLocKey,
      String bodyLocArgs,
      String titleLocKey,
      String titleLocArgs,
      String androidChannelId,
      String tag,
      String clickAction,
      boolean mutableContent) {
    this.id = id;
    this.user = user;
    this.sourceId = sourceId;
    this.scheduledTime = scheduledTime;
    this.title = title;
    this.body = body;
    this.type = type;
    this.ttlSeconds = ttlSeconds;
    this.fcmMessageId = fcmMessageId;
    this.fcmTopic = fcmTopic;
    this.fcmCondition = fcmCondition;
    this.delivered = delivered;
    this.validated = validated;
    this.appPackage = appPackage;
    this.sourceType = sourceType;
    this.dryRun = dryRun;
    this.additionalData = additionalData;
    this.priority = priority;
    this.sound = sound;
    this.badge = badge;
    this.subtitle = subtitle;
    this.icon = icon;
    this.color = color;
    this.bodyLocKey = bodyLocKey;
    this.bodyLocArgs = bodyLocArgs;
    this.titleLocKey = titleLocKey;
    this.titleLocArgs = titleLocArgs;
    this.androidChannelId = androidChannelId;
    this.tag = tag;
    this.clickAction = clickAction;
    this.mutableContent = mutableContent;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Notification)) {
      return false;
    }
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
        && Objects.equals(getAppPackage(), that.getAppPackage());
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
