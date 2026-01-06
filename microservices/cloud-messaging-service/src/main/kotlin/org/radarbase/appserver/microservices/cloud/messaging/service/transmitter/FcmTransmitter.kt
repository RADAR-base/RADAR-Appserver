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

package org.radarbase.appserver.microservices.cloud.messaging.service.transmitter

import com.google.firebase.ErrorCode
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.MessagingErrorCode
import jakarta.inject.Inject
import jakarta.ws.rs.core.Response
import org.radarbase.appserver.microservices.cloud.messaging.config.CloudMessagingServiceConfig
import org.radarbase.appserver.microservices.contract.calls.UserServiceContract
import org.radarbase.appserver.microservices.contract.exception.ProxyResponseException
import org.radarbase.appserver.microservices.contract.utils.Utils.deserializeDtoFromContract
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUserDto
import org.radarbase.appserver.microservices.core.entity.DataMessage
import org.radarbase.appserver.microservices.core.entity.Message
import org.radarbase.appserver.microservices.core.entity.Notification
import org.radarbase.appserver.microservices.core.exception.FcmMessageTransmitException
import org.radarbase.appserver.microservices.core.fcm.downstream.FcmSender
import org.radarbase.appserver.microservices.core.fcm.model.FcmDataMessage
import org.radarbase.appserver.microservices.core.fcm.model.FcmNotificationMessage
import org.radarbase.appserver.microservices.core.service.FcmDataMessageService
import org.radarbase.appserver.microservices.core.service.FcmNotificationService
import org.radarbase.appserver.microservices.core.service.UserService
import org.radarbase.appserver.microservices.core.utils.requireNotNullField
import org.slf4j.LoggerFactory
import java.util.Objects

class FcmTransmitter @Inject constructor(
    private val fcmSender: FcmSender,
    private val notificationService: FcmNotificationService,
    private val dataMessageService: FcmDataMessageService,
    config: CloudMessagingServiceConfig,
) : DataMessageTransmitter, NotificationTransmitter {

    private val userServiceUrl = config.contract.user

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
                -> {
            }

            ErrorCode.UNAVAILABLE -> {
                // Could schedule for retry.
                logger.warn("The FCM service is unavailable")
            }
        }
    }

    private suspend fun handleFCMErrorCode(errorCode: MessagingErrorCode?, message: Message) {
        when (errorCode) {
            MessagingErrorCode.INTERNAL, MessagingErrorCode.QUOTA_EXCEEDED, MessagingErrorCode.INVALID_ARGUMENT, MessagingErrorCode.SENDER_ID_MISMATCH, MessagingErrorCode.THIRD_PARTY_AUTH_ERROR -> {}
            MessagingErrorCode.UNAVAILABLE -> {
                // Could schedule for retry.
                logger.warn("The FCM service is unavailable.")
            }

            MessagingErrorCode.UNREGISTERED -> {
                val subjectId = requireNotNullField(message.subjectId, "Subject Id")
                val projectId = requireNotNullField(message.projectId, "Project Id")

                logger.warn("The Device for user {} was unregistered.", message.subjectId)
                notificationService.removeNotificationsForUser(
                    projectId,
                    subjectId,
                )
                dataMessageService.removeDataMessagesForUser(
                    projectId,
                    subjectId,
                )

                val userId = requireNotNullField(message.userId, "Notification's UserId")
                val user = deserializeDtoFromContract<FcmUserDto>(
                    UserServiceContract.getUserUsingId(userId, userServiceUrl),
                ) {
                    "user_not_found ; User with id $userId not found when creating a notification in transmitter"
                }

                UserServiceContract.checkFcmTokenExistsAndReplace(
                    subjectId,
                    user,
                    userServiceUrl,
                ).let {
                    if (it.status !in 200..299) {
                        throw ProxyResponseException(
                            Response.Status.fromStatusCode(it.status),
                            it.body?.decodeToString() ?: "Upstream failed to check and replace user's fcm token",

                            )
                    }
                }
            }
            else -> logger.error("Unknown error occurred when transmitting message")
        }
    }

    private suspend fun createMessageFromNotification(notification: Notification): FcmNotificationMessage {
        val userId = requireNotNullField(notification.userId, "Notification's UserId")
        val user = deserializeDtoFromContract<FcmUserDto>(
            UserServiceContract.getUserUsingId(userId, userServiceUrl),
        ) {
            "user_not_found ; User with id $userId not found when creating a notification in transmitter"
        }

        val to = Objects.requireNonNullElseGet<String>(
            notification.fcmTopic,
            requireNotNullField(user, "Notification's User")::fcmToken,
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

    private suspend fun createMessageFromDataMessage(dataMessage: DataMessage): FcmDataMessage {
        val userId = requireNotNullField(dataMessage.userId, "Notification's UserId")
        val user = deserializeDtoFromContract<FcmUserDto>(
            UserServiceContract.getUserUsingId(userId, userServiceUrl),
        ) {
            "user_not_found ; User with id $userId not found when creating a notification in transmitter"
        }

        val to = Objects.requireNonNullElseGet<String>(
                dataMessage.fcmTopic,
                requireNotNullField(user, "Data Message's User")::fcmToken,
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

    companion object {
        private val logger = LoggerFactory.getLogger(FcmTransmitter::class.java)

        private const val IS_DELIVERY_RECEIPT_REQUESTED: Boolean = true
        private const val DEFAULT_TIME_TO_LIVE: Int = 2419200 // 4 weeks

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
