package org.radarbase.appserver.jersey.service.transmitter

import org.radarbase.appserver.jersey.entity.DataMessage
import org.radarbase.appserver.jersey.entity.Notification
import org.radarbase.appserver.jersey.fcm.model.FcmDataMessage
import org.radarbase.appserver.jersey.fcm.model.FcmNotificationMessage
import org.slf4j.LoggerFactory
import java.util.Objects

class FcmTransmitter : DataMessageTransmitter, NotificationTransmitter {
    override fun send(dataMessage: DataMessage) {
        TODO("Not yet implemented")
    }

    override fun send(notification: Notification) {
        TODO("Not yet implemented")
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FcmTransmitter::class.java)

        private const val IS_DELIVERY_RECEIPT_REQUESTED: Boolean = true
        private const val DEFAULT_TIME_TO_LIVE: Int = 2419200 // 4 weeks

        private fun createMessageFromNotification(notification: Notification): FcmNotificationMessage {
            val to = Objects.requireNonNullElseGet<String>(
                notification.fcmTopic, notification.user!!::fcmToken,
            )
            return FcmNotificationMessage().apply {
                this.to = to
                this.condition = notification.fcmCondition
                this.priority = notification.priority
                this.mutableContent = notification.mutableContent
                this.deliveryReceiptRequested = IS_DELIVERY_RECEIPT_REQUESTED
                this.messageId = notification.fcmMessageId.toString()
                this.timeToLive = Objects.requireNonNullElse<Int?>(notification.ttlSeconds, DEFAULT_TIME_TO_LIVE)
                this.notification = getNotificationMap(notification)
                this.data = notification.additionalData
            }
        }

        private fun createMessageFromDataMessage(dataMessage: DataMessage): FcmDataMessage {
            val to =
                Objects.requireNonNullElseGet<String>(
                    dataMessage.fcmTopic, dataMessage.user!!::fcmToken,
                )
            return FcmDataMessage().apply {
                this.to = to
                this.condition = dataMessage.fcmCondition
                this.priority = dataMessage.priority
                this.mutableContent = dataMessage.mutableContent
                this.deliveryReceiptRequested = IS_DELIVERY_RECEIPT_REQUESTED
                this.messageId = dataMessage.fcmMessageId.toString()
                this.timeToLive = Objects.requireNonNullElse(dataMessage.ttlSeconds, DEFAULT_TIME_TO_LIVE)
                this.data = dataMessage.dataMap
            }
        }

        private fun getNotificationMap(notification: Notification): Map<String, Any> {
            val notificationMap: MutableMap<String, Any> = HashMap<String, Any>()
            notificationMap.put("body", notification.body ?: "")
            notificationMap.put("title", notification.title!!)
            notificationMap.put("sound", "default")

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

        fun putIfNotNull(map: MutableMap<String, Any>, key: String, value: Any?) {
            if (value != null) {
                map.put(key, value)
            }
        }


    }
}
