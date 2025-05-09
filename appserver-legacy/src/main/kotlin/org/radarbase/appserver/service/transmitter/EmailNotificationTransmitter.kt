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

import org.apache.commons.lang3.ObjectUtils
import org.radarbase.appserver.entity.Notification
import org.radarbase.appserver.exception.EmailMessageTransmitException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(value = ["radar.notification.email.enabled"], havingValue = "true")
class EmailNotificationTransmitter(
    private val emailSender: JavaMailSender,
) : NotificationTransmitter {
    @Value("\${radar.notification.email.from}")
    @Transient
    private val from: String? = null

    @Throws(EmailMessageTransmitException::class)
    override fun send(notification: Notification?) {
        requireNotNull(notification) {
            "Notification to send should not be null"
        }

        if (notification.emailEnabled) {
            try {
                if (notification.user!!.emailAddress.isNullOrBlank()) {
                    logger.warn(
                        "Could not transmit a notification via email because subject {} has no email address.",
                        notification.user!!.subjectId,
                    )
                    return
                }
                emailSender.send(createEmailFromNotification(notification))
            } catch (e: Exception) {
                logger.error("Could not transmit a notification via email", e)
                throw EmailMessageTransmitException("Could not transmit a notification via email", e)
            }
        }
    }

    private fun createEmailFromNotification(notification: Notification): SimpleMailMessage {
        val title = ObjectUtils.defaultIfNull(notification.emailTitle, notification.title)
        val body = ObjectUtils.defaultIfNull(notification.emailBody, notification.body)
        val message = SimpleMailMessage()
        message.from = from
        message.setTo(notification.user!!.emailAddress)
        message.subject = title
        message.text = body
        return message
    }

    companion object {
        private val logger = LoggerFactory.getLogger(EmailNotificationTransmitter::class.java)
    }
}
