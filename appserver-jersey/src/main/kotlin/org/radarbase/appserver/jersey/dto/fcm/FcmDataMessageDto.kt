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

package org.radarbase.appserver.jersey.dto.fcm

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.radarbase.appserver.jersey.entity.DataMessage
import java.time.Instant
import java.util.Objects

class FcmDataMessageDto(dataMessageEntity: DataMessage? = null) {
    var id: Long? = dataMessageEntity?.id

    @field:JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        timezone = "UTC",
    )
    var scheduledTime: @NotNull Instant? = dataMessageEntity?.scheduledTime

    var delivered: Boolean = dataMessageEntity?.delivered == true

    var ttlSeconds: Int = dataMessageEntity?.ttlSeconds ?: 0

    @field:NotEmpty
    var sourceId: String? = dataMessageEntity?.sourceId

    var fcmMessageId: String? = dataMessageEntity?.fcmMessageId

    var fcmTopic: String? = dataMessageEntity?.fcmTopic

    // for use with the FCM admin SDK
    var fcmCondition: String? = dataMessageEntity?.fcmCondition

    @field:NotEmpty
    var appPackage: String? = dataMessageEntity?.appPackage

    @field:NotEmpty
    var sourceType: String? = dataMessageEntity?.sourceType

    @field:Size(max = 100)
    var dataMap: MutableMap<String?, String?>? = dataMessageEntity?.dataMap

    var priority: String? = dataMessageEntity?.priority

    var mutableContent: Boolean = dataMessageEntity?.mutableContent == true

    @field:JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        timezone = "UTC",
    )
    var createdAt: Instant? = dataMessageEntity?.createdAt?.toInstant()

    @field:JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        timezone = "UTC",
    )
    var updatedAt: Instant? = dataMessageEntity?.updatedAt?.toInstant()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FcmDataMessageDto) return false
        val that = other
        return delivered == that.delivered && ttlSeconds == that.ttlSeconds && scheduledTime == that.scheduledTime &&
            appPackage == that.appPackage &&
            sourceType == that.sourceType
    }

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
