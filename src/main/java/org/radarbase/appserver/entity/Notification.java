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

import lombok.*;
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
 * @author yatharthranjan
 * @see Scheduled
 * @see org.radarbase.appserver.service.scheduler.NotificationSchedulerService
 * @see org.radarbase.appserver.service.fcm.FcmMessageReceiverService
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
@NoArgsConstructor
@Setter
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class Notification extends Message {

    private static final long serialVersionUID = 6L;

    @NotNull
    @Column(nullable = false)
    private String title;

    private String body;

    // Type of notification. In terms of aRMT - PHQ8, RSES, ESM, etc.
    @Nullable
    private String type;

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

    public static class NotificationBuilder extends Message.MessageBuilder<NotificationBuilder> {
        @NotNull transient String title;
        transient String body;
        @Nullable
        transient String type;
        transient String sound;
        transient String badge;
        transient String subtitle;
        transient String icon;
        transient String color;
        transient String bodyLocKey;
        transient String bodyLocArgs;
        transient String titleLocKey;
        transient String titleLocArgs;
        transient String androidChannelId;
        transient String tag;
        transient String clickAction;
        transient Map<String, String> additionalData;


        public NotificationBuilder() {
        }

        public NotificationBuilder(Notification notification) {
            this.id = notification.getId();
            this.user = notification.getUser();
            this.sourceId = notification.getSourceId();
            this.scheduledTime = notification.getScheduledTime();
            this.ttlSeconds = notification.getTtlSeconds();
            this.fcmMessageId = notification.getFcmMessageId();
            this.fcmTopic = notification.getFcmTopic();
            this.fcmCondition = notification.getFcmCondition();
            this.delivered = notification.isDelivered();
            this.validated = notification.isValidated();
            this.appPackage = notification.getAppPackage();
            this.sourceType = notification.getSourceType();
            this.dryRun = notification.isDryRun();
            this.mutableContent = notification.isMutableContent();
            this.additionalData = notification.getAdditionalData();
            this.title = notification.getTitle();
            this.body = notification.getBody();
            this.type = notification.getType();
            this.sound = notification.getSound();
            this.badge = notification.getBadge();
            this.subtitle = notification.getSubtitle();
            this.icon = notification.getIcon();
            this.color = notification.getColor();
            this.bodyLocKey = notification.getBodyLocKey();
            this.bodyLocArgs = notification.getBodyLocArgs();
            this.titleLocKey = notification.getTitleLocKey();
            this.titleLocArgs = notification.getTitleLocArgs();
            this.androidChannelId = notification.getAndroidChannelId();
            this.tag = notification.getTag();
            this.clickAction = notification.getClickAction();
            this.additionalData = notification.getAdditionalData();
        }

        public NotificationBuilder title(String title) {
            this.title = title;
            return this;
        }

        public NotificationBuilder body(String body) {
            this.body = body;
            return this;
        }

        public NotificationBuilder type(String type) {
            this.type = type;
            return this;
        }

        public NotificationBuilder sound(String sound) {
            this.sound = sound;
            return this;
        }

        public NotificationBuilder badge(String badge) {
            this.badge = badge;
            return this;
        }

        public NotificationBuilder subtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public NotificationBuilder icon(String icon) {
            this.icon = icon;
            return this;
        }

        public NotificationBuilder color(String color) {
            this.color = color;
            return this;
        }

        public NotificationBuilder bodyLocKey(String bodyLocKey) {
            this.bodyLocKey = bodyLocKey;
            return this;
        }

        public NotificationBuilder bodyLocArgs(String bodyLocArgs) {
            this.bodyLocArgs = bodyLocArgs;
            return this;
        }

        public NotificationBuilder titleLocKey(String titleLocKey) {
            this.titleLocKey = titleLocKey;
            return this;
        }

        public NotificationBuilder titleLocArgs(String titleLocArgs) {
            this.titleLocArgs = titleLocArgs;
            return this;
        }

        public NotificationBuilder androidChannelId(String androidChannelId) {
            this.androidChannelId = androidChannelId;
            return this;
        }

        public NotificationBuilder tag(String tag) {
            this.tag = tag;
            return this;
        }

        public NotificationBuilder clickAction(String clickAction) {
            this.clickAction = clickAction;
            return this;
        }

        public NotificationBuilder additionalData(Map<String, String> additionalData) {
            this.additionalData = additionalData;
            return this;
        }

        public Notification build() {
            Notification notification = new Notification();
            notification.setId(this.id);
            notification.setUser(this.user);
            notification.setSourceId(this.sourceId);
            notification.setScheduledTime(this.scheduledTime);
            notification.setTtlSeconds(this.ttlSeconds);
            notification.setFcmMessageId(this.fcmMessageId);
            notification.setFcmTopic(this.fcmTopic);
            notification.setFcmCondition(this.fcmCondition);
            notification.setDelivered(this.delivered);
            notification.setValidated(this.validated);
            notification.setAppPackage(this.appPackage);
            notification.setSourceType(this.sourceType);
            notification.setDryRun(this.dryRun);
            notification.setMutableContent(this.mutableContent);
            notification.setAdditionalData(this.additionalData);
            notification.setTitle(this.title);
            notification.setBody(this.body);
            notification.setType(this.type);
            notification.setSound(this.sound);
            notification.setBadge(this.badge);
            notification.setSubtitle(this.subtitle);
            notification.setIcon(this.icon);
            notification.setColor(this.color);
            notification.setBodyLocKey(this.bodyLocKey);
            notification.setBodyLocArgs(this.bodyLocArgs);
            notification.setTitleLocKey(this.titleLocKey);
            notification.setTitleLocArgs(this.titleLocArgs);
            notification.setAndroidChannelId(this.androidChannelId);
            notification.setTag(this.tag);
            notification.setClickAction(this.clickAction);
            notification.setAdditionalData(this.additionalData);

            return notification;
        }
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
