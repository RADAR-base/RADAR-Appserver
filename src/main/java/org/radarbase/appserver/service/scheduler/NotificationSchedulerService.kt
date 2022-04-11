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
package org.radarbase.appserver.service.scheduler

import org.radarbase.appserver.entity.Notification
import org.radarbase.appserver.service.scheduler.quartz.SchedulerService
import org.radarbase.fcm.downstream.FcmSender
import org.radarbase.fcm.model.FcmNotificationMessage
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import kotlin.random.Random

/**
 * [Service] for scheduling Notifications to be sent through FCM at the [ ] time. It also provided functions for updating/ deleting
 * already scheduled Notification Jobs.
 *
 * @author yatharthranjan
 */
@Service
class NotificationSchedulerService(
    @Autowired @Qualifier("fcmSenderProps") fcmSender: FcmSender?,
    @Autowired schedulerService: SchedulerService?
) : MessageSchedulerService<Notification?>(fcmSender, schedulerService) {
    @Throws(Exception::class)
    override fun send(notification: Notification?) {
        notification?.let {
            fcmSender.send(createMessageFromNotification(it))
        }
    }

    companion object {
        private fun getNotificationMap(notification: Notification): Map<String, Any> {
            val notificationMap: MutableMap<String, Any> = HashMap()
            notificationMap["body"] = notification.body ?: ""
            notificationMap["title"] = notification.title ?: "Alert from RADAR-Base"
            notificationMap["sound"] = "default"
            putIfNotNull(notificationMap, "sound", notification.sound)
            putIfNotNull(notificationMap, "badge", notification.badge)
            putIfNotNull(notificationMap, "click_action", notification.clickAction)
            putIfNotNull(notificationMap, "subtitle", notification.subtitle)
            putIfNotNull(notificationMap, "body_loc_key", notification.bodyLocKey)
            putIfNotNull(notificationMap, "body_loc_args", notification.bodyLocArgs)
            putIfNotNull(notificationMap, "title_loc_key", notification.titleLocKey)
            putIfNotNull(notificationMap, "title_loc_args", notification.titleLocArgs)
            putIfNotNull(notificationMap, "android_channel_id", notification.androidChannelId)
            putIfNotNull(notificationMap, "icon", notification.icon)
            putIfNotNull(notificationMap, "tag", notification.tag)
            putIfNotNull(notificationMap, "color", notification.color)
            return notificationMap
        }

        private fun createMessageFromNotification(notification: Notification): FcmNotificationMessage {
            val to = notification.fcmTopic ?: notification.user?.fcmToken
            ?: throw IllegalArgumentException("FCM Topic or User FCM Token is not set")

            return FcmNotificationMessage(
                to = to,
                condition = notification.fcmCondition,
                priority = notification.priority,
                mutableContent = notification.mutableContent,
                deliveryReceiptRequested = IS_DELIVERY_RECEIPT_REQUESTED,
                messageId = notification.fcmMessageId
                    ?: Random(System.currentTimeMillis()).nextLong().toString(),
                timeToLive = notification.ttlSeconds,
                notification = getNotificationMap(notification),
                data = notification.additionalData,
            )
        }
    }
}