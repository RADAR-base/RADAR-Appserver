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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.radarbase.appserver.entity.DataMessage;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

/**
 * @author yatharthranjan
 */
@ToString
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class FcmDataMessageDto implements Serializable {

    private static final long serialVersionUID = 3L;

    private Long id;

    @NotNull
    @DateTimeFormat(iso = ISO.DATE_TIME)
    private Instant scheduledTime;

    private boolean delivered;

    private int ttlSeconds;

    @NotEmpty
    private String sourceId;

    private String fcmMessageId;

    private String fcmTopic;

    // for use with the FCM admin SDK
    private String fcmCondition;

    @NotEmpty
    private String appPackage;

    @NotEmpty
    private String sourceType;

    @Size(max = 100)
    private Map<String, String> dataMap;

    private String priority;

    private boolean mutableContent;

    @DateTimeFormat(iso = ISO.DATE_TIME)
    private Instant createdAt;

    @DateTimeFormat(iso = ISO.DATE_TIME)
    private Instant updatedAt;

    public Long getId() {
        return id;
    }

    public Instant getScheduledTime() {
        return scheduledTime;
    }

    public boolean isDelivered() {
        return delivered;
    }

    public int getTtlSeconds() {
        return ttlSeconds;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getFcmMessageId() {
        return fcmMessageId;
    }

    public String getFcmTopic() {
        return fcmTopic;
    }

    public String getFcmCondition() {
        return fcmCondition;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public String getSourceType() {
        return sourceType;
    }

    public Map<String, String> getDataMap() {
        return dataMap;
    }

    public String getPriority() {
        return priority;
    }

    public boolean isMutableContent() {
        return mutableContent;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public FcmDataMessageDto(DataMessage dataMessageEntity) {
        this.id = dataMessageEntity.getId();
        this.scheduledTime = dataMessageEntity.getScheduledTime();
        this.delivered = dataMessageEntity.getDelivered();
        this.fcmMessageId = dataMessageEntity.getFcmMessageId();
        this.sourceId = dataMessageEntity.getSourceId();
        this.appPackage = dataMessageEntity.getAppPackage();
        this.sourceType = dataMessageEntity.getSourceType();
        this.ttlSeconds = dataMessageEntity.getTtlSeconds();
        this.dataMap = dataMessageEntity.getDataMap();
        if (dataMessageEntity.getCreatedAt() != null) {
            this.createdAt = dataMessageEntity.getCreatedAt().toInstant();
        }
        if (dataMessageEntity.getUpdatedAt() != null) {
            this.updatedAt = dataMessageEntity.getUpdatedAt().toInstant();
        }

        this.fcmTopic = dataMessageEntity.getFcmTopic();
        this.fcmCondition = dataMessageEntity.getFcmCondition();
        this.priority = dataMessageEntity.getPriority();
        this.mutableContent = dataMessageEntity.getMutableContent();
    }

    public FcmDataMessageDto setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public FcmDataMessageDto setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
        return this;
    }

    public FcmDataMessageDto setId(Long id) {
        this.id = id;
        return this;
    }

    public FcmDataMessageDto setScheduledTime(Instant scheduledTime) {
        this.scheduledTime = scheduledTime;
        return this;
    }

    public FcmDataMessageDto setDelivered(boolean delivered) {
        this.delivered = delivered;
        return this;
    }

    public FcmDataMessageDto setTtlSeconds(int ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
        return this;
    }

    public FcmDataMessageDto setFcmMessageId(String fcmMessageId) {
        this.fcmMessageId = fcmMessageId;
        return this;
    }

    public FcmDataMessageDto setSourceId(String sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public FcmDataMessageDto setAppPackage(String appPackage) {
        this.appPackage = appPackage;
        return this;
    }

    public FcmDataMessageDto setSourceType(String sourceType) {
        this.sourceType = sourceType;
        return this;
    }

    public FcmDataMessageDto setDataMap(Map<String, String> dataMap) {
        this.dataMap = dataMap;
        return this;
    }

    public FcmDataMessageDto setFcmTopic(String fcmTopic) {
        this.fcmTopic = fcmTopic;
        return this;
    }

    public FcmDataMessageDto setFcmCondition(String fcmCondition) {
        this.fcmCondition = fcmCondition;
        return this;
    }

    public FcmDataMessageDto setPriority(String priority) {
        this.priority = priority;
        return this;
    }

    public FcmDataMessageDto setMutableContent(boolean mutableContent) {
        this.mutableContent = mutableContent;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FcmDataMessageDto)) return false;
        FcmDataMessageDto that = (FcmDataMessageDto) o;
        return delivered == that.delivered
                && ttlSeconds == that.ttlSeconds
                && Objects.equals(scheduledTime, that.scheduledTime)
                && Objects.equals(appPackage, that.appPackage)
                && Objects.equals(sourceType, that.sourceType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                scheduledTime, delivered, ttlSeconds, appPackage, sourceType);
    }
}
