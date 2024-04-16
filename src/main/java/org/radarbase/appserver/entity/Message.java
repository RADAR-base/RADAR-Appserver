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
import java.util.Objects;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.lang.Nullable;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = true, cascade = CascadeType.ALL)
    @JoinColumn(name = "task_id", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private Task task;

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

    private String priority;

    @Column(name = "mutable_content")
    private boolean mutableContent;

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
