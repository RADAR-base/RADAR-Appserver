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
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.radarbase.appserver.dto.fcm.FcmDataMessageDto;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/**
 * {@link Entity} for persisting data messages. The corresponding DTO is {@link FcmDataMessageDto}.
 * This also includes information for scheduling the data message through the Firebase Cloud
 * Messaging(FCM) system.
 *
 * @author yatharthranjan
 * @see Scheduled
 * @see org.radarbase.appserver.service.scheduler.DataMessageSchedulerService
 * @see org.radarbase.appserver.service.fcm.FcmMessageReceiverService
 */
@Entity
@Table(
        name = "data_messages",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {
                                "user_id",
                                "source_id",
                                "scheduled_time",
                                "ttl_seconds",
                                "delivered",
                                "dry_run"
                        })
        })
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
@ToString
@NoArgsConstructor
@SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
public class DataMessage extends Message {
    private static final long serialVersionUID = 4L;

    @Nullable
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "data_message_map")
    @MapKeyColumn(name = "key", nullable = true)
    @Column(name = "value")
    private Map<String, String> dataMap;

    @NoArgsConstructor
    public static class DataMessageBuilder {
        transient Long id;
        transient User user;
        transient String sourceId;
        transient Instant scheduledTime;
        transient int ttlSeconds;
        transient String fcmMessageId;
        transient String fcmTopic;
        transient String fcmCondition;
        transient boolean delivered;
        transient boolean validated;
        transient String appPackage;
        transient String sourceType;
        transient boolean dryRun;
        transient String priority;
        transient boolean mutableContent;
        transient Map<String, String> dataMap;

        public DataMessageBuilder(DataMessage dataMessage) {
            this.id = dataMessage.getId();
            this.user = dataMessage.getUser();
            this.sourceId = dataMessage.getSourceId();
            this.scheduledTime = dataMessage.getScheduledTime();
            this.ttlSeconds = dataMessage.getTtlSeconds();
            this.fcmMessageId = dataMessage.getFcmMessageId();
            this.fcmTopic = dataMessage.getFcmTopic();
            this.fcmCondition = dataMessage.getFcmCondition();
            this.delivered = dataMessage.isDelivered();
            this.validated = dataMessage.isValidated();
            this.appPackage = dataMessage.getAppPackage();
            this.sourceType = dataMessage.getSourceType();
            this.dryRun = dataMessage.isDryRun();
            this.mutableContent = dataMessage.isMutableContent();
            this.dataMap = dataMessage.getDataMap();
        }

        public DataMessageBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public DataMessageBuilder user(User user) {
            this.user = user;
            return this;
        }

        public DataMessageBuilder sourceId(String sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public DataMessageBuilder scheduledTime(Instant scheduledTime) {
            this.scheduledTime = scheduledTime;
            return this;
        }

        public DataMessageBuilder ttlSeconds(int ttlSeconds) {
            this.ttlSeconds = ttlSeconds;
            return this;
        }

        public DataMessageBuilder fcmMessageId(String fcmMessageId) {
            this.fcmMessageId = fcmMessageId;
            return this;
        }

        public DataMessageBuilder fcmTopic(String fcmTopic) {
            this.fcmTopic = fcmTopic;
            return this;
        }

        public DataMessageBuilder fcmCondition(String fcmCondition) {
            this.fcmCondition = fcmCondition;
            return this;
        }

        public DataMessageBuilder delivered(boolean delivered) {
            this.delivered = delivered;
            return this;
        }

        public DataMessageBuilder appPackage(String appPackage) {
            this.appPackage = appPackage;
            return this;
        }

        public DataMessageBuilder sourceType(String sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        public DataMessageBuilder dryRun(boolean dryRun) {
            this.dryRun = dryRun;
            return this;
        }

        public DataMessageBuilder priority(String priority) {
            this.priority = priority;
            return this;
        }

        public DataMessageBuilder mutableContent(boolean mutableContent) {
            this.mutableContent = mutableContent;
            return this;
        }


        public DataMessageBuilder dataMap(Map<String, String> dataMap) {
            this.dataMap = dataMap;
            return this;
        }

        public DataMessage build() {
            DataMessage dataMessage = new DataMessage();
            dataMessage.setId(this.id);
            dataMessage.setUser(this.user);
            dataMessage.setSourceId(this.sourceId);
            dataMessage.setScheduledTime(this.scheduledTime);
            dataMessage.setTtlSeconds(this.ttlSeconds);
            dataMessage.setFcmMessageId(this.fcmMessageId);
            dataMessage.setFcmTopic(this.fcmTopic);
            dataMessage.setFcmCondition(this.fcmCondition);
            dataMessage.setDelivered(this.delivered);
            dataMessage.setValidated(this.validated);
            dataMessage.setAppPackage(this.appPackage);
            dataMessage.setSourceType(this.sourceType);
            dataMessage.setDryRun(this.dryRun);
            dataMessage.setPriority(this.priority);
            dataMessage.setMutableContent(this.mutableContent);
            dataMessage.setDataMap(this.dataMap);

            return dataMessage;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DataMessage)) {
            return false;
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                getDataMap());
    }

}
