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
package org.radarbase.appserver.entity

import org.springframework.lang.Nullable
import java.time.Instant
import java.util.*
import javax.persistence.*

/**
 * [Entity] for persisting notifications. The corresponding DTO is [FcmNotificationDto].
 * This also includes information for scheduling the notification through the Firebase Cloud
 * Messaging(FCM) system.
 *
 * @author yatharthranjan
 * @see Scheduled
 *
 * @see org.radarbase.appserver.service.scheduler.NotificationSchedulerService
 */
@Table(
    name = "notifications",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "source_id", "scheduled_time", "title", "body", "type", "ttl_seconds", "delivered", "dry_run"])]
)
@Entity
class Notification : Message() {

    @Column(nullable = false)
    var title: String? = null
    var body: String? = null

    // Type of notification. In terms of aRMT - PHQ8, RSES, ESM, etc.
    @Nullable
    var type: String? = null
    var sound: String? = null

    // For IOS
    var badge: String? = null

    // For IOS
    var subtitle: String? = null

    // For android
    var icon: String? = null

    // For android. Color of the icon
    var color: String? = null

    @Column(name = "body_loc_key")
    var bodyLocKey: String? = null

    @Column(name = "body_loc_args")
    var bodyLocArgs: String? = null

    @Column(name = "title_loc_key")
    var titleLocKey: String? = null

    @Column(name = "title_loc_args")
    var titleLocArgs: String? = null

    // For android
    @Column(name = "android_channel_id")
    var androidChannelId: String? = null

    // For android
    var tag: String? = null

    @Column(name = "click_action")
    var clickAction: String? = null

    @Nullable
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "additional_key", nullable = true)
    @Column(name = "additional_value")
    var additionalData: Map<String, String>? = null

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Notification) {
            return false
        }
        return (super.equals(other)
                && title == other.title
                && body == other.body
                && type == other.type)
    }

    override fun hashCode(): Int {
        return Objects.hash(
            super.hashCode(),
            title,
            body,
            type
        )
    }

    @JvmOverloads
    fun copy(
        id: Long? = this.id,
        user: User? = this.user,
        scheduledTime: Instant? = this.scheduledTime,
        sourceId: String? = this.sourceId,
        sourceType: String? = this.sourceType,
        ttlSeconds: Int = this.ttlSeconds,
        fcmMessageId: String? = this.fcmMessageId,
        fcmTopic: String? = this.fcmTopic,
        fcmCondition: String? = this.fcmCondition,
        appPackage: String? = this.appPackage,
        title: String? = this.title,
        body: String? = this.body,
        type: String? = this.type,
        sound: String? = this.sound,
        badge: String? = this.badge,
        subtitle: String? = this.subtitle,
        icon: String? = this.icon,
        color: String? = this.color,
        bodyLocKey: String? = this.bodyLocKey,
        bodyLocArgs: String? = this.bodyLocArgs,
        titleLocKey: String? = this.titleLocKey,
        titleLocArgs: String? = this.titleLocArgs,
        androidChannelId: String? = this.androidChannelId,
        tag: String? = this.tag,
        clickAction: String? = this.clickAction,
        additionalData: Map<String, String>? = this.additionalData,
        dryRun: Boolean = this.dryRun,
        delivered: Boolean = this.delivered,
        validated: Boolean = this.validated,
        priority: String? = this.priority,
        mutableContent: Boolean = this.mutableContent,
        createdAt: Date = this.createdAt,
        updatedAt: Date = this.updatedAt,
    ): Notification {
        return Notification().apply {
            this.id = id
            this.user = user
            this.scheduledTime = scheduledTime
            this.sourceId = sourceId
            this.sourceType = sourceType
            this.ttlSeconds = ttlSeconds
            this.fcmMessageId = fcmMessageId
            this.fcmTopic = fcmTopic
            this.fcmCondition = fcmCondition
            this.appPackage = appPackage
            this.title = title
            this.body = body
            this.type = type
            this.sound = sound
            this.badge = badge
            this.subtitle = subtitle
            this.icon = icon
            this.color = color
            this.bodyLocKey = bodyLocKey
            this.bodyLocArgs = bodyLocArgs
            this.titleLocKey = titleLocKey
            this.titleLocArgs = titleLocArgs
            this.androidChannelId = androidChannelId
            this.tag = tag
            this.clickAction = clickAction
            this.additionalData = additionalData
            this.dryRun = dryRun
            this.delivered = delivered
            this.validated = validated
            this.priority = priority
            this.mutableContent = mutableContent
            this.createdAt = createdAt
            this.updatedAt = updatedAt
        }
    }

    override fun toString(): String {
        return "Notification(id=$id, title=$title, body=$body, type=$type, sound=$sound, " +
                "badge=$badge, subtitle=$subtitle, icon=$icon, color=$color, " +
                "bodyLocKey=$bodyLocKey, bodyLocArgs=$bodyLocArgs, titleLocKey=$titleLocKey," +
                " titleLocArgs=$titleLocArgs, androidChannelId=$androidChannelId, tag=$tag," +
                " clickAction=$clickAction, additionalData=$additionalData)"
    }

    companion object {
        private const val serialVersionUID = 6L
    }
}