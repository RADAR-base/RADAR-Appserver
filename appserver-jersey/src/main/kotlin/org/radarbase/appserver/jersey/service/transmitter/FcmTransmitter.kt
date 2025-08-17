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

package org.radarbase.appserver.jersey.service.transmitter

import com.google.firebase.ErrorCode
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.MessagingErrorCode
import jakarta.inject.Inject
import org.radarbase.appserver.jersey.dto.fcm.FcmUserDto
import org.radarbase.appserver.jersey.entity.DataMessage
import org.radarbase.appserver.jersey.entity.Message
import org.radarbase.appserver.jersey.entity.Notification
import org.radarbase.appserver.jersey.exception.FcmMessageTransmitException
import org.radarbase.appserver.jersey.fcm.downstream.FcmSender
import org.radarbase.appserver.jersey.fcm.model.FcmDataMessage
import org.radarbase.appserver.jersey.fcm.model.FcmNotificationMessage
import org.radarbase.appserver.jersey.service.FcmDataMessageService
import org.radarbase.appserver.jersey.service.FcmNotificationService
import org.radarbase.appserver.jersey.service.UserService
import org.radarbase.appserver.jersey.utils.requireNotNullField
import org.slf4j.LoggerFactory
import java.util.Objects

class FcmTransmitter @Inject constructor(
    private val fcmSender: FcmSender,
    private val notificationService: FcmNotificationService,
    private val dataMessageService: FcmDataMessageService,
    private val userService: UserService,
) : DataMessageTransmitter, NotificationTransmitter {

    override suspend fun send(dataMessage: DataMessage) {
        try {
            fcmSender.send(createMessageFromDataMessage(dataMessage))
        } catch (exc: FirebaseMessagingException) {
            handleFcmException(exc, dataMessage)
        } catch (exc: Exception) {
            throw FcmMessageTransmitException("Could not transmit a data message through Fcm. ${exc.message}")
        }
    }

    override suspend fun send(notification: Notification) {
        try {
            fcmSender.send(createMessageFromNotification(notification))
        } catch (exc: FirebaseMessagingException) {
            handleFcmException(exc, notification)
        } catch (exc: Exception) {
            throw FcmMessageTransmitException("Could not transmit a notification through Fcm. ${exc.message}")
        }
    }

    private suspend fun handleFcmException(exc: FirebaseMessagingException, message: Message?) {
        logger.error("Error occurred when sending downstream message.", exc)
        if (message != null) {
            handleErrorCode(exc.errorCode, message)
            handleFCMErrorCode(exc.messagingErrorCode, message)
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun handleErrorCode(errorCode: ErrorCode, message: Message?) {
        // More info on ErrorCode: https://firebase.google.com/docs/reference/fcm/rest/v1/ErrorCode
        when (errorCode) {
            ErrorCode.INVALID_ARGUMENT,
            ErrorCode.INTERNAL,
            ErrorCode.ABORTED,
            ErrorCode.CONFLICT,
            ErrorCode.CANCELLED,
            ErrorCode.DATA_LOSS,
            ErrorCode.NOT_FOUND,
            ErrorCode.OUT_OF_RANGE,
            ErrorCode.ALREADY_EXISTS,
            ErrorCode.DEADLINE_EXCEEDED,
            ErrorCode.PERMISSION_DENIED,
            ErrorCode.RESOURCE_EXHAUSTED,
            ErrorCode.FAILED_PRECONDITION,
            ErrorCode.UNAUTHENTICATED,
            ErrorCode.UNKNOWN,
            -> {}
            ErrorCode.UNAVAILABLE -> {
                // Could schedule for retry.
                logger.warn("The FCM service is unavailable")
            }
        }
    }

    private suspend fun handleFCMErrorCode(errorCode: MessagingErrorCode, message: Message) {
        when (errorCode) {
            MessagingErrorCode.INTERNAL, MessagingErrorCode.QUOTA_EXCEEDED, MessagingErrorCode.INVALID_ARGUMENT, MessagingErrorCode.SENDER_ID_MISMATCH, MessagingErrorCode.THIRD_PARTY_AUTH_ERROR -> {}
            MessagingErrorCode.UNAVAILABLE -> {
                // Could schedule for retry.
                logger.warn("The FCM service is unavailable.")
            }

            MessagingErrorCode.UNREGISTERED -> {
                val userDto = FcmUserDto(requireNotNull(message.user) { "User cannot be null" })
                val subjectId = requireNotNullField(userDto.subjectId, "Subject Id")
                val projectId = requireNotNullField(userDto.projectId, "Project Id")

                logger.warn("The Device for user {} was unregistered.", userDto.subjectId)
                notificationService.removeNotificationsForUser(
                    projectId,
                    subjectId,
                )
                dataMessageService.removeDataMessagesForUser(
                    projectId,
                    subjectId,
                )
                userService.checkFcmTokenExistsAndReplace(userDto)
            }
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FcmTransmitter::class.java)

        private const val IS_DELIVERY_RECEIPT_REQUESTED: Boolean = true
        private const val DEFAULT_TIME_TO_LIVE: Int = 2419200 // 4 weeks

        private fun createMessageFromNotification(notification: Notification): FcmNotificationMessage {
            val to = Objects.requireNonNullElseGet<String>(
                notification.fcmTopic,
                requireNotNullField(notification.user, "Notification's User")::fcmToken,
            )
            return FcmNotificationMessage().apply {
                this.to = to
                this.condition = notification.fcmCondition
                this.priority = notification.priority
                this.mutableContent = notification.mutableContent
                this.deliveryReceiptRequested = IS_DELIVERY_RECEIPT_REQUESTED
                this.data = notification.additionalData
                this.messageId = notification.fcmMessageId.toString()
                this.timeToLive = Objects.requireNonNullElse(notification.ttlSeconds, DEFAULT_TIME_TO_LIVE)
                this.notification = getNotificationMap(notification)
            }
        }

        private fun createMessageFromDataMessage(dataMessage: DataMessage): FcmDataMessage {
            val to =
                Objects.requireNonNullElseGet<String>(
                    dataMessage.fcmTopic,
                    requireNotNullField(dataMessage.user, "Data Message's User")::fcmToken,
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
            val notificationMap: MutableMap<String, Any> = HashMap()
            notificationMap["body"] = notification.body ?: ""
            notificationMap["title"] = requireNotNullField(notification.title, "Notification's Title")
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

        fun putIfNotNull(map: MutableMap<String, Any>, key: String, value: Any?) {
            if (value != null) {
                map[key] = value
            }
        }
    }
}
