package org.radarbase.appserver.service.transmitter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.radarbase.appserver.dto.protocol.AssessmentType;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.entity.Project;
import org.radarbase.appserver.entity.Task;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.entity.UserMetrics;
import org.radarbase.appserver.event.state.TaskState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    classes = {EmailNotificationTransmitter.class},
    properties = {
        "radar.notification.email.enabled=true"
    }
)
class EmailNotificationTransmitterTest {
    @Autowired
    private EmailNotificationTransmitter emailNotificationTransmitter;

    @MockBean
    private JavaMailSender javaMailSender;

    @Test
    void testSend() {
        Notification validNotification = buildNotification();
        assertDoesNotThrow(() -> emailNotificationTransmitter.send(validNotification), "Valid Notification should not throw an exception");
        verify(javaMailSender, times(1)).send(isA(SimpleMailMessage.class));
    }

    @Test
    void testExceptionNotThrownForMissingEmail() {
        Notification invalidNotification = buildNotification();
        when(invalidNotification.getUser().getEmailAddress()).thenReturn(null);
        assertDoesNotThrow(() -> emailNotificationTransmitter.send(invalidNotification), "Notification with User w/o an email address should not throw an exception");
    }

    @Test
    void testExceptionNotThrownWithEmailFailure() {
        doThrow(mock(MailException.class)).when(javaMailSender).send(any(SimpleMailMessage.class));
        Notification validNotification = buildNotification();
        assertDoesNotThrow(() -> emailNotificationTransmitter.send(validNotification), "Problems during sending of email notifications should not throw an exception");
    }

    private Notification buildNotification() {
        User user = mock(User.class);
        when(user.getEmailAddress()).thenReturn("hello@here.com");
        Notification notification = mock(Notification.class);
        when(notification.getUser()).thenReturn(user);
        when(notification.getTitle()).thenReturn("Title");
        when(notification.getBody()).thenReturn("Body");
        when(notification.isEmailEnabled()).thenReturn(true);
        return notification;
    }
}
