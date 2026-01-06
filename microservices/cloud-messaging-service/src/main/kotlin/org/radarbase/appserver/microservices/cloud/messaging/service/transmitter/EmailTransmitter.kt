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

import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.radarbase.appserver.microservices.cloud.messaging.config.CloudMessagingServiceConfig
import org.radarbase.appserver.microservices.contract.calls.UserServiceContract
import org.radarbase.appserver.microservices.contract.utils.Utils.deserializeDtoFromContract
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUserDto
import org.radarbase.appserver.microservices.core.entity.Notification
import org.radarbase.appserver.microservices.core.exception.EmailMessageTransmitException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class EmailTransmitter(
    private val session: Session,
    private val emailFrom: String,
    config: CloudMessagingServiceConfig,
) {
    private val emailEnabled = config.email.enabled
    private val userServiceUrl = config.contract.user

    suspend fun send(notification: Notification) {
        if (emailEnabled && notification.emailEnabled) {
            val userId = requireNotNull(notification.userId) {
                "Notification user's id is null when sending email"
            }
            val user: FcmUserDto = deserializeDtoFromContract<FcmUserDto>(
                UserServiceContract.getUserUsingId(userId, userServiceUrl),
            ) {
                "user_not_found ; User with id $userId not found when creating a notification in transmitter"
            }

            val to = user.email

            if (to.isNullOrBlank()) {
                logger.warn(
                    "Could not transmit a notification via email because subject {} has no email address",
                    user.subjectId
                )
            }

            try {
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(emailFrom))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                    subject = notification.emailTitle ?: notification.title
                    setText(notification.emailBody ?: notification.body)
                }
                Transport.send(message)
                logger.debug("Email sent to {} (subject={})", to, notification.title)
            } catch (ex: Exception) {
                logger.error("Could not transmit a notification via email", ex)
                throw EmailMessageTransmitException("Could not transmit a notification via email. ${ex.message}")
            }
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(EmailTransmitter::class.java)
    }
}
