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

package org.radarbase.appserver.service.transmitter;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.exception.EmailMessageTransmitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(value = "radar.notification.email.enabled", havingValue = "true")
public class EmailNotificationTransmitter implements NotificationTransmitter {

    private final transient JavaMailSender emailSender;

    @Value("${radar.notification.email.from}")
    private transient String from;

    public EmailNotificationTransmitter(
        @Autowired JavaMailSender emailSender
    ) {
        this.emailSender = emailSender;
    }

    @Override
    public void send(Notification notification) throws EmailMessageTransmitException {
        if (notification.getEmailEnabled()) {
            try {
                if (notification.getUser().getEmailAddress() == null || notification.getUser().getEmailAddress().isBlank()) {
                    log.warn("Could not transmit a notification via email because subject {} has no email address.",
                        notification.getUser().getSubjectId());
                    return;
                }
                emailSender.send(createEmailFromNotification(notification));
            } catch (Exception e) {
                log.error("Could not transmit a notification via email", e);
                throw new EmailMessageTransmitException("Could not transmit a notification via email", e);
            }
        }
    }

    private SimpleMailMessage createEmailFromNotification(Notification notification) {
        String title = ObjectUtils.defaultIfNull(notification.getEmailTitle(), notification.getTitle());
        String body = ObjectUtils.defaultIfNull(notification.getEmailBody(), notification.getBody());
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(notification.getUser().getEmailAddress());
        message.setSubject(title);
        message.setText(body);
        return message;
    }
}
