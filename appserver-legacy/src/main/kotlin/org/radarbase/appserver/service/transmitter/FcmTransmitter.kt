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
package org.radarbase.appserver.service.transmitter

import com.google.firebase.ErrorCode
import com.google.firebase.messaging.FirebaseMessagingException
import com.google.firebase.messaging.MessagingErrorCode
import org.radarbase.appserver.dto.fcm.FcmUserDto
import org.radarbase.appserver.entity.DataMessage
import org.radarbase.appserver.entity.Message
import org.radarbase.appserver.entity.Notification
import org.radarbase.appserver.exception.FcmMessageTransmitException
import org.radarbase.appserver.service.FcmDataMessageService
import org.radarbase.appserver.service.FcmNotificationService
import org.radarbase.appserver.service.UserService
import org.radarbase.fcm.downstream.FcmSender
import org.radarbase.fcm.model.FcmDataMessage
import org.radarbase.fcm.model.FcmNotificationMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.util.*

@Component
class FcmTransmitter(
    @param:Qualifier("fcmSenderProps") val fcmSender: FcmSender,
    private val notificationService: FcmNotificationService,
    private val dataMessageService: FcmDataMessageService,
    private val userService: UserService,
) : NotificationTransmitter, DataMessageTransmitter {

    @Throws(FcmMessageTransmitException::class)
    override fun send(notification: Notification?) {
        requireNotNull(notification) {
            "Notification to send should not be null"
        }
        try {
            fcmSender.send(createMessageFromNotification(notification))
        } catch (exc: FirebaseMessagingException) {
            handleFcmException(exc, notification)
        } catch (exc: Exception) {
            throw FcmMessageTransmitException("Could not transmit a notification through Fcm", exc)
        }
    }

    @Throws(FcmMessageTransmitException::class)
    override fun send(dataMessage: DataMessage?) {
        requireNotNull(dataMessage) {
            "DataMessage to send should not be null"
        }

        try {
            fcmSender.send(createMessageFromDataMessage(dataMessage))
        } catch (exc: FirebaseMessagingException) {
            handleFcmException(exc, dataMessage)
        } catch (exc: Exception) {
            throw FcmMessageTransmitException("Could not transmit a data message through Fcm", exc)
        }
    }

    private fun handleFcmException(exc: FirebaseMessagingException, message: Message?) {
        log.error("Error occurred when sending downstream message.", exc)
        if (message != null) {
            handleErrorCode(exc.errorCode, message)
            handleFCMErrorCode(exc.messagingErrorCode, message)
        }
    }

    fun handleErrorCode(errorCode: ErrorCode, message: Message?) {
        // More info on ErrorCode: https://firebase.google.com/docs/reference/fcm/rest/v1/ErrorCode
        when (errorCode) {
            ErrorCode.INVALID_ARGUMENT, ErrorCode.INTERNAL, ErrorCode.ABORTED, ErrorCode.CONFLICT, ErrorCode.CANCELLED, ErrorCode.DATA_LOSS, ErrorCode.NOT_FOUND, ErrorCode.OUT_OF_RANGE, ErrorCode.ALREADY_EXISTS, ErrorCode.DEADLINE_EXCEEDED, ErrorCode.PERMISSION_DENIED, ErrorCode.RESOURCE_EXHAUSTED, ErrorCode.FAILED_PRECONDITION, ErrorCode.UNAUTHENTICATED, ErrorCode.UNKNOWN -> {}
            ErrorCode.UNAVAILABLE -> // TODO: Could schedule for retry.
                log.warn("The FCM service is unavailable.")
        }
    }

    fun handleFCMErrorCode(errorCode: MessagingErrorCode, message: Message) {
        when (errorCode) {
            MessagingErrorCode.INTERNAL, MessagingErrorCode.QUOTA_EXCEEDED, MessagingErrorCode.INVALID_ARGUMENT, MessagingErrorCode.SENDER_ID_MISMATCH, MessagingErrorCode.THIRD_PARTY_AUTH_ERROR -> {}
            MessagingErrorCode.UNAVAILABLE -> // TODO: Could schedule for retry.
                log.warn("The FCM service is unavailable.")

            MessagingErrorCode.UNREGISTERED -> {
                val userDto = FcmUserDto(message.user!!)
                log.warn("The Device for user {} was unregistered.", userDto.subjectId)
                notificationService.removeNotificationsForUser(
                    userDto.projectId,
                    userDto.subjectId,
                )
                dataMessageService.removeDataMessagesForUser(
                    userDto.projectId,
                    userDto.subjectId,
                )
                userService.checkFcmTokenExistsAndReplace(userDto)
            }
        }
    }

    companion object {

        private val log = LoggerFactory.getLogger(FcmTransmitter::class.java)

        private const val IS_DELIVERY_RECEIPT_REQUESTED: Boolean = true
        private const val DEFAULT_TIME_TO_LIVE: Int = 2419200 // 4 weeks

        private fun createMessageFromNotification(notification: Notification): FcmNotificationMessage {
            val to =
                Objects.requireNonNullElseGet<String>(
                    notification.fcmTopic,
                    notification.user!!::fcmToken,
                )
            return FcmNotificationMessage.Builder()
                .to(to)
                .condition(notification.fcmCondition)
                .priority(notification.priority)
                .mutableContent(notification.mutableContent)
                .deliveryReceiptRequested(IS_DELIVERY_RECEIPT_REQUESTED)
                .messageId(notification.fcmMessageId.toString())
                .timeToLive(Objects.requireNonNullElse<Int?>(notification.ttlSeconds, DEFAULT_TIME_TO_LIVE))
                .notification(getNotificationMap(notification))
                .data(notification.additionalData)
                .build()
        }

        private fun createMessageFromDataMessage(dataMessage: DataMessage): FcmDataMessage {
            val to =
                Objects.requireNonNullElseGet<String>(
                    dataMessage.fcmTopic,
                    dataMessage.user!!::fcmToken,
                )
            return FcmDataMessage.Builder()
                .to(to)
                .condition(dataMessage.fcmCondition)
                .priority(dataMessage.priority)
                .mutableContent(dataMessage.mutableContent)
                .deliveryReceiptRequested(IS_DELIVERY_RECEIPT_REQUESTED)
                .messageId(dataMessage.fcmMessageId.toString())
                .timeToLive(Objects.requireNonNullElse<Int?>(dataMessage.ttlSeconds, DEFAULT_TIME_TO_LIVE))
                .data(dataMessage.dataMap)
                .build()
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
