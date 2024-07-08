package org.radarbase.appserver.service.transmitter;

import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.entity.Notification;
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
    public void send(Notification notification) {
        if (notification.getUser().getEmailAddress() == null || notification.getUser().getEmailAddress().isBlank()) {
            log.error("Could not transmit a notification via email because subject {} has no email address.",
                notification.getUser().getSubjectId());
            return;
        }
        if (notification.isEmailEnabled()) {
            try {
                emailSender.send(createEmailFromNotification(notification));
            } catch (Exception e) {
                // Note: the EmailNotificationTransmitter does not emit Exceptions caught by the JobExecutionException.
                // As a result, email notifications are not influencing the job success/failure status.
                log.error("Could not transmit a notification via email", e);
            }
        } else {
            log.debug("Email notification is not enabled for: {}", notification.toString());
        }
    }

    private static SimpleMailMessage createEmailFromNotification(Notification notification) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(notification.getUser().getEmailAddress());
        message.setTo(notification.getUser().getEmailAddress());
        message.setSubject(notification.getTitle());
        message.setText(notification.getBody());
        return message;
    }
}
