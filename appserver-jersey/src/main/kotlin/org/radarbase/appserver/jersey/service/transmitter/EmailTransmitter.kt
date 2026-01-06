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

import jakarta.mail.Message
import jakarta.mail.Session
import jakarta.mail.Transport
import jakarta.mail.internet.InternetAddress
import jakarta.mail.internet.MimeMessage
import org.radarbase.appserver.jersey.config.AppserverConfig
import org.radarbase.appserver.jersey.entity.Notification
import org.radarbase.appserver.jersey.exception.EmailMessageTransmitException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class EmailTransmitter(
    private val session: Session,
    private val emailFrom: String,
    config: AppserverConfig,
) {
    private val emailEnabled = config.email.enabled

    fun send(notification: Notification) {
        if (emailEnabled && notification.emailEnabled) {
            val to = notification.user?.emailAddress
            if (to.isNullOrBlank()) {
                logger.warn(
                    "Could not transmit a notification via email because subject {} has no email address",
                    notification.user?.subjectId,
                )
            }
            try {
                logger.info("Sending email to {}", to)
                val message = MimeMessage(session).apply {
                    setFrom(InternetAddress(emailFrom))
                    setRecipients(Message.RecipientType.TO, InternetAddress.parse(to))
                    subject = notification.emailTitle ?: notification.title
                    setText(notification.emailBody ?: notification.body)
                }
                logger.debug("Sending email to {} (subject={})", to, notification.title)
                Transport.send(message)
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
