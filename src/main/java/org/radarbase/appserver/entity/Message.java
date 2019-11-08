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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
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
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class Message extends AuditModel implements Serializable, Scheduled {

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
    @Nullable
    private boolean delivered;

    @Nullable
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
    @Nullable
    private boolean dryRun;

    private String priority;

    @Column(name = "mutable_content")
    private boolean mutableContent;

    public static class MessageBuilder<T extends MessageBuilder<T>> {
        transient Long id;
        @NotNull transient User user;
        @Nullable
        transient String sourceId;
        @NotNull transient Instant scheduledTime;
        transient int ttlSeconds;
        transient String fcmMessageId;
        @Nullable
        transient String fcmTopic;
        @Nullable
        transient String fcmCondition;
        transient boolean delivered;
        transient boolean validated;
        @Nullable
        transient String appPackage;
        @Nullable
        transient String sourceType;
        transient boolean dryRun;
        transient String priority;
        transient boolean mutableContent;

        MessageBuilder() {
        }

        public T id(Long id) {
            this.id = id;
            return (T) this;
        }

        public T user(User user) {
            this.user = user;
            return (T) this;
        }

        public T sourceId(String sourceId) {
            this.sourceId = sourceId;
            return (T) this;
        }

        public T scheduledTime(Instant scheduledTime) {
            this.scheduledTime = scheduledTime;
            return (T) this;
        }

        public T ttlSeconds(int ttlSeconds) {
            this.ttlSeconds = ttlSeconds;
            return (T) this;
        }

        public T fcmMessageId(String fcmMessageId) {
            this.fcmMessageId = fcmMessageId;
            return (T) this;
        }

        public T fcmTopic(String fcmTopic) {
            this.fcmTopic = fcmTopic;
            return (T) this;
        }

        public T fcmCondition(String fcmCondition) {
            this.fcmCondition = fcmCondition;
            return (T) this;
        }

        public T delivered(boolean delivered) {
            this.delivered = delivered;
            return (T) this;
        }

        public T appPackage(String appPackage) {
            this.appPackage = appPackage;
            return (T) this;
        }

        public T sourceType(String sourceType) {
            this.sourceType = sourceType;
            return (T) this;
        }

        public T dryRun(boolean dryRun) {
            this.dryRun = dryRun;
            return (T) this;
        }

        public T priority(String priority) {
            this.priority = priority;
            return (T) this;
        }

        public T mutableContent(boolean mutableContent) {
            this.mutableContent = mutableContent;
            return (T) this;
        }


        public Message build() {
            Message message = new Message();
            message.setId(this.id);
            message.setUser(this.user);
            message.setSourceId(this.sourceId);
            message.setScheduledTime(this.scheduledTime);
            message.setTtlSeconds(this.ttlSeconds);
            message.setFcmMessageId(this.fcmMessageId);
            message.setFcmTopic(this.fcmTopic);
            message.setFcmCondition(this.fcmCondition);
            message.setDelivered(this.delivered);
            message.setValidated(this.validated);
            message.setAppPackage(this.appPackage);
            message.setSourceType(this.sourceType);
            message.setDryRun(this.dryRun);
            message.setMutableContent(this.mutableContent);

            return message;
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Message)) {
            return false;
        }
        Message that = (Message) o;
        return getTtlSeconds() == that.getTtlSeconds()
                && isDelivered() == that.isDelivered()
                && isDryRun() == that.isDryRun()
                && Objects.equals(getUser(), that.getUser())
                && Objects.equals(getSourceId(), that.getSourceId())
                && Objects.equals(getScheduledTime(), that.getScheduledTime())
                && Objects.equals(getAppPackage(), that.getAppPackage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                getUser(),
                getSourceId(),
                getScheduledTime(),
                getTtlSeconds(),
                isDelivered(),
                isDryRun(),
                getAppPackage(),
                getSourceType());
    }
}
