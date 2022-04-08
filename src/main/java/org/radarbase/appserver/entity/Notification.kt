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

import lombok.ToString
import org.springframework.lang.Nullable
import java.time.Instant
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

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
@ToString
class Notification(
    id: Long? = null,
    user: @NotNull User? = null,
    sourceId: String? = null,
    scheduledTime: @NotNull Instant,
    ttlSeconds: Int = 0,
    fcmMessageId: String? = null,
    fcmTopic: String? = null,
    fcmCondition: String? = null,
    delivered: Boolean = false,
    validated: Boolean = false,
    appPackage: String? = null,
    sourceType: String? = null,
    dryRun: Boolean = false,
    priority: String? = null,
    mutableContent: Boolean = false,

    @Column(nullable = false)
    val title: @NotNull String,
    val body: String? = null,

    // Type of notification. In terms of aRMT - PHQ8, RSES, ESM, etc.
    @Nullable
    val type: String? = null,
    val sound: String? = null,

    // For IOS
    val badge: String? = null,

    // For IOS
    val subtitle: String? = null,

    // For android
    val icon: String? = null,

    // For android. Color of the icon
    val color: String? = null,

    @Column(name = "body_loc_key")
    val bodyLocKey: String? = null,

    @Column(name = "body_loc_args")
    val bodyLocArgs: String? = null,

    @Column(name = "title_loc_key")
    val titleLocKey: String? = null,

    @Column(name = "title_loc_args")
    val titleLocArgs: String? = null,

    // For android
    @Column(name = "android_channel_id")
    val androidChannelId: String? = null,

    // For android
    val tag: String? = null,

    @Column(name = "click_action")
    val clickAction: String? = null,

    @Nullable
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "additional_key", nullable = true)
    @Column(name = "additional_value")
    val additionalData: Map<String, String>? = null,
) : Message(
    id,
    user,
    sourceId,
    scheduledTime,
    ttlSeconds,
    fcmMessageId,
    fcmTopic,
    fcmCondition,
    delivered,
    validated,
    appPackage,
    sourceType,
    dryRun,
    priority,
    mutableContent
) {

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

    companion object {
        private const val serialVersionUID = 6L
    }
}