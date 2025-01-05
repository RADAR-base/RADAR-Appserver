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

import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.MapKeyColumn
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.validation.constraints.NotNull
import org.springframework.lang.Nullable
import java.io.Serial
import java.time.Instant
import java.util.*

@Table(
    name = "notifications",
    uniqueConstraints = [UniqueConstraint(
        columnNames = [
            "user_id",
            "source_id",
            "scheduled_time",
            "title",
            "body",
            "type",
            "ttl_seconds",
            "delivered",
            "dry_run"
        ]
    )]
)
@Entity
class Notification : Message() {

    @field:NotNull
    @Column(nullable = false)
    var title: String? = null

    var body: String? = null

    // Type of notification. In terms of aRMT - PHQ8, RSES, ESM, etc.
    @field:Nullable
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

    @Column(name = "email_enabled")
    var emailEnabled: Boolean = false

    @Column(name = "email_title")
    var emailTitle: String? = null

    @Column(name = "email_body")
    var emailBody: String? = null

    @Nullable
    @ElementCollection(fetch = FetchType.EAGER)
    @MapKeyColumn(name = "additional_key", nullable = true)
    @Column(name = "additional_value")
    var additionalData: Map<String?, String?>? = null

    class NotificationBuilder(notification: Notification? = null) {
        @Transient
        private var id: Long? = notification?.id

        @Transient
        private var user: User? = notification?.user

        @Transient
        private var sourceId: String? = notification?.sourceId

        @Transient
        private var scheduledTime: Instant? = notification?.scheduledTime

        @Transient
        private var ttlSeconds: Int = notification?.ttlSeconds ?: 0

        @Transient
        private var fcmMessageId: String? = notification?.fcmMessageId

        @Transient
        private var fcmTopic: String? = notification?.fcmTopic

        @Transient
        private var fcmCondition: String? = notification?.fcmCondition

        @Transient
        private var delivered: Boolean = notification?.delivered == true

        @Transient
        private var validated: Boolean = notification?.validated == true

        @Transient
        private var appPackage: String? = notification?.appPackage

        @Transient
        private var sourceType: String? = notification?.sourceType

        @Transient
        private var dryRun: Boolean = notification?.dryRun == true

        @Transient
        private var priority: String? = notification?.priority

        @Transient
        private var mutableContent: Boolean = notification?.mutableContent == true

        @Transient
        private var title: String? = notification?.title

        @Transient
        private var body: String? = notification?.body

        @Transient
        private var type: String? = notification?.type

        @Transient
        private var sound: String? = notification?.sound

        @Transient
        private var badge: String? = notification?.badge

        @Transient
        private var subtitle: String? = notification?.subtitle

        @Transient
        private var icon: String? = notification?.icon

        @Transient
        private var color: String? = notification?.color

        @Transient
        private var bodyLocKey: String? = notification?.bodyLocKey

        @Transient
        private var bodyLocArgs: String? = notification?.bodyLocArgs

        @Transient
        private var titleLocKey: String? = notification?.titleLocKey

        @Transient
        private var titleLocArgs: String? = notification?.titleLocArgs

        @Transient
        private var androidChannelId: String? = notification?.androidChannelId

        @Transient
        private var tag: String? = notification?.tag

        @Transient
        private var clickAction: String? = notification?.clickAction

        @Transient
        private var emailEnabled: Boolean = notification?.emailEnabled == true

        @Transient
        private var additionalData: Map<String?, String?>? = notification?.additionalData

        @Transient
        private var task: Task? = notification?.task

        @Transient
        private var emailTitle: String? = notification?.emailTitle

        @Transient
        private var emailBody: String? = notification?.emailBody


        fun id(id: Long?): NotificationBuilder = apply {
            this.id = id
            return this
        }

        fun user(user: User?): NotificationBuilder = apply {
            this.user = user
            return this
        }

        fun sourceId(sourceId: String?): NotificationBuilder = apply {
            this.sourceId = sourceId
            return this
        }

        fun scheduledTime(scheduledTime: Instant?): NotificationBuilder = apply {
            this.scheduledTime = scheduledTime
            return this
        }

        fun ttlSeconds(ttlSeconds: Int): NotificationBuilder = apply {
            this.ttlSeconds = ttlSeconds
            return this
        }

        fun fcmMessageId(fcmMessageId: String?): NotificationBuilder = apply {
            this.fcmMessageId = fcmMessageId
            return this
        }

        fun fcmTopic(fcmTopic: String?): NotificationBuilder = apply {
            this.fcmTopic = fcmTopic
            return this
        }

        fun fcmCondition(fcmCondition: String?): NotificationBuilder = apply {
            this.fcmCondition = fcmCondition
            return this
        }

        fun delivered(delivered: Boolean): NotificationBuilder = apply {
            this.delivered = delivered
            return this
        }

        fun appPackage(appPackage: String?): NotificationBuilder = apply {
            this.appPackage = appPackage
            return this
        }

        fun sourceType(sourceType: String?): NotificationBuilder = apply {
            this.sourceType = sourceType
            return this
        }

        fun dryRun(dryRun: Boolean): NotificationBuilder = apply {
            this.dryRun = dryRun
            return this
        }

        fun priority(priority: String?): NotificationBuilder = apply {
            this.priority = priority
            return this
        }

        fun mutableContent(mutableContent: Boolean): NotificationBuilder = apply {
            this.mutableContent = mutableContent
            return this
        }


        fun title(title: String?): NotificationBuilder = apply {
            this.title = title
            return this
        }

        fun body(body: String?): NotificationBuilder = apply {
            this.body = body
            return this
        }

        fun type(type: String?): NotificationBuilder = apply {
            this.type = type
            return this
        }

        fun sound(sound: String?): NotificationBuilder = apply {
            this.sound = sound
            return this
        }

        fun badge(badge: String?): NotificationBuilder = apply {
            this.badge = badge
            return this
        }

        fun subtitle(subtitle: String?): NotificationBuilder = apply {
            this.subtitle = subtitle
            return this
        }

        fun icon(icon: String?): NotificationBuilder = apply {
            this.icon = icon
            return this
        }

        fun color(color: String?): NotificationBuilder = apply {
            this.color = color
            return this
        }

        fun bodyLocKey(bodyLocKey: String?): NotificationBuilder = apply {
            this.bodyLocKey = bodyLocKey
            return this
        }

        fun bodyLocArgs(bodyLocArgs: String?): NotificationBuilder = apply {
            this.bodyLocArgs = bodyLocArgs
            return this
        }

        fun titleLocKey(titleLocKey: String?): NotificationBuilder = apply {
            this.titleLocKey = titleLocKey
            return this
        }

        fun titleLocArgs(titleLocArgs: String?): NotificationBuilder = apply {
            this.titleLocArgs = titleLocArgs
            return this
        }

        fun androidChannelId(androidChannelId: String?): NotificationBuilder = apply {
            this.androidChannelId = androidChannelId
            return this
        }

        fun tag(tag: String?): NotificationBuilder = apply {
            this.tag = tag
            return this
        }

        fun clickAction(clickAction: String?): NotificationBuilder = apply {
            this.clickAction = clickAction
            return this
        }

        fun emailEnabled(emailEnabled: Boolean): NotificationBuilder = apply {
            this.emailEnabled = emailEnabled
            return this
        }

        fun emailTitle(title: String?): NotificationBuilder = apply {
            this.emailTitle = title
            return this
        }

        fun emailBody(body: String?): NotificationBuilder = apply {
            this.emailBody = body
            return this
        }

        fun additionalData(additionalData: Map<String?, String?>?): NotificationBuilder = apply {
            this.additionalData = additionalData
            return this
        }

        fun task(task: Task?): NotificationBuilder = apply {
            this.task = task
            return this
        }

        fun build(): Notification {
            val notification = Notification()
            notification.id = this.id
            notification.user = this.user
            notification.sourceId = this.sourceId
            notification.scheduledTime = this.scheduledTime
            notification.ttlSeconds = this.ttlSeconds
            notification.fcmMessageId = this.fcmMessageId
            notification.fcmTopic = this.fcmTopic
            notification.fcmCondition = this.fcmCondition
            notification.delivered = this.delivered
            notification.validated = this.validated
            notification.appPackage = this.appPackage
            notification.sourceType = this.sourceType
            notification.dryRun = this.dryRun
            notification.priority = this.priority
            notification.mutableContent = this.mutableContent
            notification.additionalData = this.additionalData
            notification.title = this.title
            notification.body = this.body
            notification.type = this.type
            notification.sound = this.sound
            notification.badge = this.badge
            notification.subtitle = this.subtitle
            notification.icon = this.icon
            notification.color = this.color
            notification.bodyLocKey = this.bodyLocKey
            notification.bodyLocArgs = this.bodyLocArgs
            notification.titleLocKey = this.titleLocKey
            notification.titleLocArgs = this.titleLocArgs
            notification.androidChannelId = this.androidChannelId
            notification.tag = this.tag
            notification.clickAction = this.clickAction
            notification.emailEnabled = this.emailEnabled
            notification.emailTitle = this.emailTitle
            notification.emailBody = this.emailBody
            notification.additionalData = this.additionalData
            notification.task = this.task

            return notification
        }
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o !is Notification) {
            return false
        }
        val that = o
        return super.equals(o)
                && title == that.title
                && body == that.body
                && type == that.type
    }

    override fun hashCode(): Int {
        return Objects.hash(
            super.hashCode(),
            title,
            body,
            type
        )
    }

    override fun toString(): String {
        return "Notification(title=$title, body=$body, type=$type, sound=$sound, badge=$badge, subtitle=$subtitle, icon=$icon, color=$color, bodyLocKey=$bodyLocKey, bodyLocArgs=$bodyLocArgs, titleLocKey=$titleLocKey, titleLocArgs=$titleLocArgs, androidChannelId=$androidChannelId, tag=$tag, clickAction=$clickAction, emailEnabled=$emailEnabled, emailTitle=$emailTitle, emailBody=$emailBody, additionalData=$additionalData)"
    }


    companion object {
        @Serial
        private const val serialVersionUID = 6L
    }
}