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
import org.radarbase.appserver.jersey.entity.Notification
import org.radarbase.appserver.jersey.utils.equalTo
import java.time.Instant
import java.util.Objects

class FcmNotificationDto(notificationEntity: Notification? = null) {
    var id: Long? = notificationEntity?.id

    @field:NotNull
    @field:JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        timezone = "UTC",
    )
    var scheduledTime: Instant? = notificationEntity?.scheduledTime

    var delivered: Boolean = notificationEntity?.delivered == true

    @field:NotEmpty
    var title: String? = notificationEntity?.title

    var body: String? = notificationEntity?.body

    var ttlSeconds: Int = notificationEntity?.ttlSeconds ?: 0

    @field:NotEmpty
    var sourceId: String? = notificationEntity?.sourceId

    var fcmMessageId: String? = notificationEntity?.fcmMessageId

    var fcmTopic: String? = notificationEntity?.fcmTopic

    // for use with the FCM admin SDK
    var fcmCondition: String? = notificationEntity?.fcmCondition

    @field:NotEmpty
    var type: String? = notificationEntity?.type

    @field:NotEmpty
    var appPackage: String? = notificationEntity?.appPackage

    @field:NotEmpty
    var sourceType: String? = notificationEntity?.sourceType

    @field:Size(max = 100)
    var additionalData: Map<String?, String?>? = notificationEntity?.additionalData

    var priority: String? = notificationEntity?.priority

    var sound: String? = notificationEntity?.sound

    // For IOS
    var badge: String? = notificationEntity?.badge

    // For IOS
    var subtitle: String? = notificationEntity?.subtitle

    // For android
    var icon: String? = notificationEntity?.icon

    // For android. Color of the icon
    var color: String? = notificationEntity?.color

    var bodyLocKey: String? = notificationEntity?.bodyLocKey

    var bodyLocArgs: String? = notificationEntity?.bodyLocArgs

    var titleLocKey: String? = notificationEntity?.titleLocKey

    var titleLocArgs: String? = notificationEntity?.titleLocArgs

    // For android
    var androidChannelId: String? = notificationEntity?.androidChannelId

    // For android
    var tag: String? = notificationEntity?.tag

    var clickAction: String? = notificationEntity?.clickAction

    var emailEnabled: Boolean = notificationEntity?.emailEnabled == true

    var emailTitle: String? = notificationEntity?.emailTitle

    var emailBody: String? = notificationEntity?.emailBody

    var mutableContent: Boolean = notificationEntity?.mutableContent == true

    @field:JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        timezone = "UTC",
    )
    var createdAt: Instant? = notificationEntity?.createdAt?.toInstant()

    @field:JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        timezone = "UTC",
    )
    var updatedAt: Instant? = notificationEntity?.updatedAt?.toInstant()

    override fun equals(other: Any?): Boolean = equalTo(
        other,
        FcmNotificationDto::delivered,
        FcmNotificationDto::ttlSeconds,
        FcmNotificationDto::scheduledTime,
        FcmNotificationDto::title,
        FcmNotificationDto::body,
        FcmNotificationDto::type,
        FcmNotificationDto::appPackage,
        FcmNotificationDto::sourceType,
    )

    override fun hashCode(): Int {
        return Objects.hash(
            scheduledTime,
            delivered,
            title,
            body,
            ttlSeconds,
            type,
            appPackage,
            sourceType,
        )
    }

    override fun toString(): String {
        return "FcmNotificationDto(id=$id, scheduledTime=$scheduledTime, delivered=$delivered, title=$title, body=$body, ttlSeconds=$ttlSeconds, sourceId=$sourceId, fcmMessageId=$fcmMessageId, fcmTopic=$fcmTopic, fcmCondition=$fcmCondition, type=$type, appPackage=$appPackage, sourceType=$sourceType, additionalData=$additionalData, priority=$priority, sound=$sound, badge=$badge, subtitle=$subtitle, icon=$icon, color=$color, bodyLocKey=$bodyLocKey, bodyLocArgs=$bodyLocArgs, titleLocKey=$titleLocKey, titleLocArgs=$titleLocArgs, androidChannelId=$androidChannelId, tag=$tag, clickAction=$clickAction, emailEnabled=$emailEnabled, emailTitle=$emailTitle, emailBody=$emailBody, mutableContent=$mutableContent, createdAt=$createdAt, updatedAt=$updatedAt)"
    }
}
