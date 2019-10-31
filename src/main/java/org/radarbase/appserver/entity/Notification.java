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

import lombok.Getter;
import lombok.ToString;
import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

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
public class Notification extends Message<Notification> {

  @NotNull
  @Column(nullable = false)
  private String title;

  private String body;

  // Type of notification. In terms of aRMT - PHQ8, RSES, ESM, etc.
  @Nullable private String type;

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

  @Nullable
  @ElementCollection(fetch = FetchType.EAGER)
  @MapKeyColumn(name = "additional_key", nullable = true)
  @Column(name = "additional_value")
  private Map<String, String> additionalData;

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

  public Notification setSound(String sound) {
    this.sound = sound;
    return this;
  }

  public Notification setBadge(String badge) {
    this.badge = badge;
    return this;
  }

  public Notification setSubtitle(String subtitle) {
    this.subtitle = subtitle;
    return this;
  }

  public Notification setIcon(String icon) {
    this.icon = icon;
    return this;
  }

  public Notification setColor(String color) {
    this.color = color;
    return this;
  }

  public Notification setBodyLocKey(String bodyLocKey) {
    this.bodyLocKey = bodyLocKey;
    return this;
  }

  public Notification setBodyLocArgs(String bodyLocArgs) {
    this.bodyLocArgs = bodyLocArgs;
    return this;
  }

  public Notification setTitleLocKey(String titleLocKey) {
    this.titleLocKey = titleLocKey;
    return this;
  }

  public Notification setTitleLocArgs(String titleLocArgs) {
    this.titleLocArgs = titleLocArgs;
    return this;
  }

  public Notification setAndroidChannelId(String androidChannelId) {
    this.androidChannelId = androidChannelId;
    return this;
  }

  public Notification setTag(String tag) {
    this.tag = tag;
    return this;
  }

  public Notification setClickAction(String clickAction) {
    this.clickAction = clickAction;
    return this;
  }

  public Notification setAdditionalData(Map<String, String> additionalData) {
    this.additionalData = additionalData;
    return this;
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
    return super.equals(o)
        && Objects.equals(getTitle(), that.getTitle())
        && Objects.equals(getBody(), that.getBody())
        && Objects.equals(getType(), that.getType());
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
