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
package org.radarbase.appserver.dto.fcm

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.radarbase.appserver.entity.DataMessage
import org.springframework.format.annotation.DateTimeFormat
import java.io.Serializable
import java.time.Instant
import java.util.*

/**
 * @author yatharthranjan
 */
@Suppress("unused")
@JsonIgnoreProperties(ignoreUnknown = true)
class FcmDataMessageDto(dataMessageEntity: DataMessage? = null) : Serializable {
    var id: Long? = dataMessageEntity?.id

    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
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

    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var createdAt: Instant? = dataMessageEntity?.createdAt?.toInstant()

    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var updatedAt: Instant? = dataMessageEntity?.updatedAt?.toInstant()

    fun withCreatedAt(createdAt: Instant?): FcmDataMessageDto = apply {
        this.createdAt = createdAt
    }

    fun withUpdatedAt(updatedAt: Instant?): FcmDataMessageDto = apply {
        this.updatedAt = updatedAt
    }

    fun withId(id: Long?): FcmDataMessageDto = apply {
        this.id = id
    }

    fun withScheduledTime(scheduledTime: Instant?): FcmDataMessageDto = apply {
        this.scheduledTime = scheduledTime
    }

    fun withDelivered(delivered: Boolean): FcmDataMessageDto = apply {
        this.delivered = delivered
    }

    fun withTtlSeconds(ttlSeconds: Int): FcmDataMessageDto = apply {
        this.ttlSeconds = ttlSeconds
    }

    fun withFcmMessageId(fcmMessageId: String?): FcmDataMessageDto = apply {
        this.fcmMessageId = fcmMessageId
    }

    fun withSourceId(sourceId: String?): FcmDataMessageDto = apply {
        this.sourceId = sourceId
    }

    fun withAppPackage(appPackage: String?): FcmDataMessageDto = apply {
        this.appPackage = appPackage
    }

    fun withSourceType(sourceType: String?): FcmDataMessageDto = apply {
        this.sourceType = sourceType
    }

    fun withDataMap(dataMap: MutableMap<String?, String?>?): FcmDataMessageDto = apply {
        this.dataMap = dataMap
    }

    fun withFcmTopic(fcmTopic: String?): FcmDataMessageDto = apply {
        this.fcmTopic = fcmTopic
    }

    fun withFcmCondition(fcmCondition: String?): FcmDataMessageDto = apply {
        this.fcmCondition = fcmCondition
    }

    fun withPriority(priority: String?): FcmDataMessageDto = apply {
        this.priority = priority
    }

    fun withMutableContent(mutableContent: Boolean): FcmDataMessageDto = apply {
        this.mutableContent = mutableContent
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o !is FcmDataMessageDto) return false
        val that = o
        return delivered == that.delivered && ttlSeconds == that.ttlSeconds && scheduledTime == that.scheduledTime
                && appPackage == that.appPackage
                && sourceType == that.sourceType
    }

    override fun hashCode(): Int {
        return Objects.hash(
            scheduledTime, delivered, ttlSeconds, appPackage, sourceType
        )
    }

    override fun toString(): String {
        return "FcmDataMessageDto(id=$id, scheduledTime=$scheduledTime, delivered=$delivered, ttlSeconds=$ttlSeconds, sourceId=$sourceId, fcmMessageId=$fcmMessageId, fcmTopic=$fcmTopic, fcmCondition=$fcmCondition, appPackage=$appPackage, sourceType=$sourceType, dataMap=$dataMap, priority=$priority, mutableContent=$mutableContent, createdAt=$createdAt, updatedAt=$updatedAt)"
    }


    companion object {
        private const val serialVersionUID = 3L
    }
}
