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
import kotlinx.serialization.Serializable
import org.radarbase.appserver.jersey.entity.Notification
import org.radarbase.appserver.jersey.serialization.InstantSerializer
import org.radarbase.appserver.jersey.utils.equalTo
import java.time.Instant
import java.util.Objects

@Serializable
class FcmNotificationDto(
    var id: Long? = null,

    @field:NotNull
    @Serializable(with = InstantSerializer::class)
    var scheduledTime: Instant? = null,

    var delivered: Boolean = false,

    @field:NotEmpty
    var title: String? = null,

    var body: String? = null,

    var ttlSeconds: Int = 0,

    @field:NotEmpty
    var sourceId: String? = null,

    var fcmMessageId: String? = null,

    var fcmTopic: String? = null,

    // for use with the FCM admin SDK
    var fcmCondition: String? = null,

    @field:NotEmpty
    var type: String? = null,

    @field:NotEmpty
    var appPackage: String? = null,

    @field:NotEmpty
    var sourceType: String? = null,

    @field:Size(max = 100)
    var additionalData: Map<String?, String?>? = null,

    var priority: String? = null,

    var sound: String? = null,

    // For IOS
    var badge: String? = null,

    // For IOS
    var subtitle: String? = null,

    // For android
    var icon: String? = null,

    // For android. Color of the icon
    var color: String? = null,

    var bodyLocKey: String? = null,

    var bodyLocArgs: String? = null,

    var titleLocKey: String? = null,

    var titleLocArgs: String? = null,

    // For android
    var androidChannelId: String? = null,

    // For android
    var tag: String? = null,

    var clickAction: String? = null,

    var emailEnabled: Boolean = false,

    var emailTitle: String? = null,

    var emailBody: String? = null,

    var mutableContent: Boolean = false,

    @Serializable(with = InstantSerializer::class)
    var createdAt: Instant? = null,

    @Serializable(with = InstantSerializer::class)
    var updatedAt: Instant? = null,
) {
    constructor(notification: Notification) : this(
        id = notification.id,
        scheduledTime = notification.scheduledTime,
        delivered = notification.delivered,
        title = notification.title,
        body = notification.body,
        ttlSeconds = notification.ttlSeconds,
        sourceId = notification.sourceId,
        fcmMessageId = notification.fcmMessageId,
        fcmTopic = notification.fcmTopic,
        fcmCondition = notification.fcmCondition,
        type = notification.type,
        appPackage = notification.appPackage,
        sourceType = notification.sourceType,
        additionalData = notification.additionalData,
        priority = notification.priority,
        sound = notification.sound,
        badge = notification.badge,
        subtitle = notification.subtitle,
        icon = notification.icon,
        color = notification.color,
        bodyLocKey = notification.bodyLocKey,
        bodyLocArgs = notification.bodyLocArgs,
        titleLocKey = notification.titleLocKey,
        titleLocArgs = notification.titleLocArgs,
        androidChannelId = notification.androidChannelId,
        tag = notification.tag,
        clickAction = notification.clickAction,
        emailEnabled = notification.emailEnabled,
        emailTitle = notification.emailTitle,
        emailBody = notification.emailBody,
        mutableContent = notification.mutableContent,
        createdAt = notification.createdAt?.toInstant(),
        updatedAt = notification.updatedAt?.toInstant(),
    )

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
