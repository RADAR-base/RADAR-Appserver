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
package org.radarbase.fcm.downstream

import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.AndroidNotification
import com.google.firebase.messaging.ApnsConfig
import com.google.firebase.messaging.Aps
import com.google.firebase.messaging.ApsAlert
import com.google.firebase.messaging.FcmOptions
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import org.radarbase.fcm.model.FcmDataMessage
import org.radarbase.fcm.model.FcmDownstreamMessage
import org.radarbase.fcm.model.FcmNotificationMessage
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.Instant

/**
 * When authorizing via a service account, you have to set the GOOGLE_APPLICATION_CREDENTIALS
 * environment variable. For More info, see
 * https://firebase.google.com/docs/admin/setup#initialize-sdk
 *
 * @author yatharthranjan
 */
class AdminSdkFcmSender(options: FirebaseOptions) : FcmSender {
    init {
        // TODO also take config from application properties
        try {
            FirebaseApp.initializeApp(options)
        } catch (exc: IllegalStateException) {
            log.warn("Firebase app was already initialised. {}", exc.message)
        }
    }

    @Throws(FirebaseMessagingException::class)
    override fun send(downstreamMessage: FcmDownstreamMessage) {
        // TODO Add support for WebPush as well
        val priority = downstreamMessage.priority

        val message =
            Message.builder()
                .setToken(downstreamMessage.to)
                .setFcmOptions(FcmOptions.builder().build())
                .setCondition(downstreamMessage.condition)

        val ttl = getValidTtlMillis(downstreamMessage.timeToLive!!)

        if (downstreamMessage is FcmNotificationMessage) {
            val notificationMessage = downstreamMessage

            message
                .setAndroidConfig(
                    AndroidConfig.builder()
                        .setCollapseKey(downstreamMessage.collapseKey)
                        .setPriority(
                            if (priority == null) {
                                AndroidConfig.Priority.HIGH
                            } else {
                                AndroidConfig.Priority.valueOf(
                                    priority,
                                )
                            },
                        )
                        .setTtl(ttl.toMillis())
                        .setNotification(getAndroidNotification(notificationMessage))
                        .putAllData(notificationMessage.data)
                        .build(),
                )
                .setApnsConfig(
                    getApnsConfigBuilder(notificationMessage, ttl)!!
                        .putHeader("apns-push-type", "alert")
                        .build(),
                )
                .putAllData(notificationMessage.data)
                .setCondition(notificationMessage.condition)
                .setNotification(
                    Notification.builder()
                        .setBody(
                            notificationMessage.notification!!.getOrDefault("body", "").toString(),
                        )
                        .setTitle(
                            notificationMessage.notification!!.getOrDefault("title", "").toString(),
                        )
                        .setImage(
                            notificationMessage.notification!!.getOrDefault("image_url", "").toString(),
                        )
                        .build(),
                )
        } else if (downstreamMessage is FcmDataMessage) {
            val dataMessage = downstreamMessage
            message
                .setAndroidConfig(
                    AndroidConfig.builder()
                        .setCollapseKey(downstreamMessage.collapseKey)
                        .setPriority(
                            if (priority == null) {
                                AndroidConfig.Priority.NORMAL
                            } else {
                                AndroidConfig.Priority.valueOf(
                                    priority,
                                )
                            },
                        )
                        .setTtl(ttl.toMillis())
                        .putAllData(dataMessage.data)
                        .build(),
                )
                .setApnsConfig(getApnsConfigBuilder(dataMessage, ttl)!!.build())
                .setCondition(dataMessage.condition)
                .putAllData(dataMessage.data)
        } else {
            throw IllegalArgumentException(
                "The Message type is not known." + downstreamMessage.javaClass,
            )
        }

        val response = FirebaseMessaging.getInstance().send(message.build())
        log.info("Message Sent with response : {}", response)
    }

    private fun getAndroidNotification(notificationMessage: FcmNotificationMessage): AndroidNotification? {
        val builder =
            AndroidNotification.builder()
                .setBody(notificationMessage.notification!!.getOrDefault("body", "").toString())
                .setTitle(
                    notificationMessage.notification!!.getOrDefault("title", "").toString(),
                )
                .setChannelId(
                    getString(notificationMessage.notification!!["android_channel_id"]),
                )
                .setColor(getString(notificationMessage.notification!!["color"]))
                .setTag(getString(notificationMessage.notification!!["tag"]))
                .setIcon(getString(notificationMessage.notification!!["icon"]))
                .setSound(getString(notificationMessage.notification!!["sound"]))
                .setClickAction(getString(notificationMessage.notification!!["click_action"]))

        val bodyLocKey = getString(notificationMessage.notification!!["body_loc_key"])
        val titleLocKey = getString(notificationMessage.notification!!["title_loc_key"])

        if (bodyLocKey != null) {
            builder
                .setBodyLocalizationKey(
                    getString(notificationMessage.notification!!["body_loc_key"]),
                )
                .addBodyLocalizationArg(
                    getString(notificationMessage.notification!!["body_loc_args"]),
                )
        }

        if (titleLocKey != null) {
            builder
                .addTitleLocalizationArg(
                    getString(notificationMessage.notification!!["title_loc_args"]),
                )
                .setTitleLocalizationKey(
                    getString(notificationMessage.notification!!["title_loc_key"]),
                )
        }

        return builder.build()
    }

    /**
     * Get the APNS config builder. More info on values and keys -
     * https://developer.apple.com/documentation/usernotifications/setting_up_a_remote_notification_server/sending_notification_requests_to_apns/
     * and
     * https://developer.apple.com/documentation/usernotifications/setting_up_a_remote_notification_server/generating_a_remote_notification
     */
    private fun getApnsConfigBuilder(message: FcmDownstreamMessage, ttl: Duration): ApnsConfig.Builder? {
        val config = ApnsConfig.builder()

        if (message.collapseKey != null) config.putHeader("apns-collapse-id", message.collapseKey)

        // The date at which the notification is no longer valid. This value is a UNIX epoch
        // expressed in seconds (UTC).
        config.putHeader(
            "apns-expiration",
            Instant.now().plus(ttl).epochSecond.toString(),
        )

        if (message is FcmNotificationMessage) {
            val notificationMessage = message
            val apnsData: Map<String?, Any?> = HashMap(notificationMessage.data ?: emptyMap<String, String>())

            val apsAlertBuilder = ApsAlert.builder()
            val title = getString(notificationMessage.notification!!["title"])
            if (title != null) apsAlertBuilder.setTitle(title)

            val body = getString(notificationMessage.notification!!["body"])
            if (body != null) apsAlertBuilder.setBody(body)

            val titleLocKey = getString(notificationMessage.notification!!["title_loc_key"])
            if (titleLocKey != null) apsAlertBuilder.setTitleLocalizationKey(titleLocKey)

            val titleLocArgs = getString(notificationMessage.notification!!["title_loc_args"])
            if (titleLocKey != null && titleLocArgs != null) apsAlertBuilder.addTitleLocalizationArg(titleLocArgs)

            val bodyLocKey = getString(notificationMessage.notification!!["body_loc_key"])
            if (bodyLocKey != null) apsAlertBuilder.setLocalizationKey(bodyLocKey)

            val bodyLocArgs = getString(notificationMessage.notification!!["body_loc_args"])
            if (bodyLocKey != null && bodyLocArgs != null) apsAlertBuilder.addLocalizationArg(bodyLocArgs)

            val apsBuilder = Aps.builder()
            val sound = getString(notificationMessage.notification!!["sound"])
            if (sound != null) apsBuilder.setSound(sound)

            val badge = getString(notificationMessage.notification!!["badge"])
            if (badge != null) apsBuilder.setBadge(badge.toInt())

            val category = getString(notificationMessage.notification!!["category"])
            if (category != null) apsBuilder.setCategory(category)

            val threadId = getString(notificationMessage.notification!!["thread_id"])
            if (threadId != null) apsBuilder.setThreadId(threadId)

            if (notificationMessage.contentAvailable != null) apsBuilder.setContentAvailable(notificationMessage.contentAvailable!!)

            if (notificationMessage.mutableContent != null) apsBuilder.setMutableContent(notificationMessage.mutableContent!!)

            return config
                .putAllCustomData(apnsData)
                .setAps(apsBuilder.setAlert(apsAlertBuilder.build()).build())
                .putHeader("apns-push-type", "alert")
        } else if (message is FcmDataMessage) {
            val dataMessage = message
            val apnsData: Map<String?, Any?> = HashMap(dataMessage.data ?: emptyMap<String, String>())

            return config
                .putAllCustomData(apnsData)
                .setAps(Aps.builder().setContentAvailable(true).setSound("default").build())
                .putHeader("apns-push-type", "background") // No alert is shown
                .putHeader("apns-priority", "5") // 5 required in case of background type
        } else {
            throw IllegalArgumentException("The Message type is not known." + message.javaClass)
        }
    }

    private fun getString(obj: Any?): String? {
        return obj?.toString()
    }

    override fun doesProvideDeliveryReceipt(): Boolean {
        return false
    }

    fun getValidTtlMillis(ttl: Int): Duration {
        // Makes sure ttl is less than 28 days
        val ttlSeconds = if (ttl >= 0 && ttl <= DEFAULT_TIME_TO_LIVE) ttl else DEFAULT_TIME_TO_LIVE
        return Duration.ofSeconds(ttlSeconds.toLong())
    }

    companion object {
        const val DEFAULT_TIME_TO_LIVE: Int = 2419200 // 4 weeks
        private val log: Logger = LoggerFactory.getLogger(AdminSdkFcmSender::class.java)
    }
}
