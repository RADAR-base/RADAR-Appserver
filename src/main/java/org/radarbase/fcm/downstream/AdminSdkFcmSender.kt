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

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.*
import org.radarbase.fcm.model.FcmDownstreamMessage
import org.radarbase.fcm.model.FcmNotificationMessage
import org.slf4j.LoggerFactory

/**
 * When authorizing via a service account, you have to set the GOOGLE_APPLICATION_CREDENTIALS
 * environment variable. For More info, see
 * https://firebase.google.com/docs/admin/setup#initialize-sdk
 *
 * @author yatharthranjan
 */
class AdminSdkFcmSender : FcmSender {
    init {
        // TODO also take config from application properties
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.getApplicationDefault())
            .build()
        try {
            FirebaseApp.initializeApp(options)
        } catch (exc: IllegalStateException) {
            logger.warn("Firebase app was already initialised. {}", exc.message)
        }
    }

    @Throws(FirebaseMessagingException::class)
    override fun send(downstreamMessage: FcmDownstreamMessage) {
        // TODO Add support for WebPush as well
        val message = Message.builder()
            .setToken(downstreamMessage.to)
            .setFcmOptions(FcmOptions.builder().build())
            .setCondition(downstreamMessage.condition)
            .setAndroidConfig(downstreamMessage.getAndroidConfig())
            .setApnsConfig(downstreamMessage.getApnsConfig())
            .putAllData(downstreamMessage.data)
            .setCondition(downstreamMessage.condition)

        if (downstreamMessage is FcmNotificationMessage) {
            message
                .setNotification(
                    Notification.builder()
                        .setBody(
                            downstreamMessage.notification.getOrDefault("body", "").toString()
                        )
                        .setTitle(
                            downstreamMessage.notification.getOrDefault("title", "").toString()
                        )
                        .setImage(
                            downstreamMessage.notification.getOrDefault("image_url", "")
                                .toString()
                        )
                        .build()
                )
        }

        val response = FirebaseMessaging.getInstance().send(message.build())
        logger.info("Message Sent with response : {}", response)
    }

    override fun doesProvideDeliveryReceipt(): Boolean {
        return false
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AdminSdkFcmSender::class.java)
    }
}