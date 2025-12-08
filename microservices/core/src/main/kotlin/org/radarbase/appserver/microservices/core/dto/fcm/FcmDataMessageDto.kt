/*
 * Copyright 2025 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.appserver.microservices.core.dto.fcm

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import kotlinx.serialization.Serializable
import org.radarbase.appserver.microservices.core.entity.DataMessage
import org.radarbase.appserver.microservices.core.serialization.InstantSerializer
import org.radarbase.appserver.microservices.core.utils.equalTo
import java.time.Instant
import java.util.Objects

@Serializable
class FcmDataMessageDto(
    var id: Long? = null,

    @field:NotNull
    @Serializable(with = InstantSerializer::class)
    var scheduledTime: Instant? = null,

    var delivered: Boolean = false,

    var ttlSeconds: Int = 0,

    @field:NotEmpty
    var sourceId: String? = null,

    var fcmMessageId: String? = null,

    var fcmTopic: String? = null,

    // for use with the FCM admin SDK
    var fcmCondition: String? = null,

    @field:NotEmpty
    var appPackage: String? = null,

    @field:NotEmpty
    var sourceType: String? = null,

    var taskId: Long? = null,

    @field:Size(max = 100)
    var dataMap: Map<String?, String?>? = null,

    var priority: String? = null,

    var mutableContent: Boolean = false,

    @Serializable(with = InstantSerializer::class)
    var createdAt: Instant? = null,

    @Serializable(with = InstantSerializer::class)
    var updatedAt: Instant? = null,
) {
    constructor(dataMessage: DataMessage) : this(
        id = dataMessage.id,
        scheduledTime = dataMessage.scheduledTime,
        delivered = dataMessage.delivered,
        ttlSeconds = dataMessage.ttlSeconds,
        sourceId = dataMessage.sourceId,
        fcmMessageId = dataMessage.fcmMessageId,
        fcmTopic = dataMessage.fcmTopic,
        fcmCondition = dataMessage.fcmCondition,
        appPackage = dataMessage.appPackage,
        sourceType = dataMessage.sourceType,
        dataMap = dataMessage.dataMap?.toMap(),
        priority = dataMessage.priority,
        mutableContent = dataMessage.mutableContent,
        createdAt = dataMessage.createdAt?.toInstant(),
        updatedAt = dataMessage.updatedAt?.toInstant(),
    )

    override fun equals(other: Any?): Boolean = equalTo(
        other,
        FcmDataMessageDto::delivered,
        FcmDataMessageDto::ttlSeconds,
        FcmDataMessageDto::scheduledTime,
        FcmDataMessageDto::appPackage,
        FcmDataMessageDto::sourceType,
    )

    override fun hashCode(): Int {
        return Objects.hash(
            scheduledTime,
            delivered,
            ttlSeconds,
            appPackage,
            sourceType,
        )
    }

    override fun toString(): String {
        return "FcmDataMessageDto(id=$id, scheduledTime=$scheduledTime, delivered=$delivered, ttlSeconds=$ttlSeconds, sourceId=$sourceId, fcmMessageId=$fcmMessageId, fcmTopic=$fcmTopic, fcmCondition=$fcmCondition, appPackage=$appPackage, sourceType=$sourceType, dataMap=$dataMap, priority=$priority, mutableContent=$mutableContent, createdAt=$createdAt, updatedAt=$updatedAt)"
    }
}
