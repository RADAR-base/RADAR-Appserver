package org.radarbase.appserver.service.transmitter

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.*
import org.radarbase.appserver.entity.Notification
import org.radarbase.appserver.entity.User
import org.radarbase.appserver.exception.EmailMessageTransmitException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.mail.MailException
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [EmailNotificationTransmitter::class],
    properties = ["radar.notification.email.enabled=true"]
)
class EmailNotificationTransmitterTest {

    @Autowired
    private lateinit var emailNotificationTransmitter: EmailNotificationTransmitter

    @MockBean
    private lateinit var javaMailSender: JavaMailSender

    @Test
    fun testSend() {
        val validNotification = buildNotification()
        assertDoesNotThrow("Valid Notification should not throw an exception") {
            emailNotificationTransmitter.send(
                validNotification
            )
        }
        verify(javaMailSender, times(1)).send(any<SimpleMailMessage>())
    }

    @Test
    fun testExceptionNotThrownForMissingEmail() {
        val invalidNotification = buildNotification()
        `when`(invalidNotification.user?.emailAddress).thenReturn(null)
        assertDoesNotThrow("Notification with User w/o an email address should not throw an exception")
        { emailNotificationTransmitter.send(invalidNotification) }

        `when`(invalidNotification.user?.emailAddress).thenReturn("")
        assertDoesNotThrow("Notification with User w/o an email address should not throw an exception")
        { emailNotificationTransmitter.send(invalidNotification) }
    }

    @Test
    fun testExceptionThrownWithEmailFailure() {
        doThrow(mock(MailException::class.java)).`when`(javaMailSender).send(any<SimpleMailMessage>())
        val validNotification = buildNotification()
        assertThrows<EmailMessageTransmitException>("Problems during sending of email notifications should throw an exception")
        { emailNotificationTransmitter.send(validNotification) }

    }

    private fun buildNotification(): Notification {
        val user = mock(User::class.java)
        `when`(user.emailAddress).thenReturn("hello@here.com")
        val notification = mock(Notification::class.java)
        `when`(notification.user).thenReturn(user)
        `when`(notification.title).thenReturn("Title")
        `when`(notification.body).thenReturn("Body")
        `when`(notification.emailEnabled).thenReturn(true)
        return notification
    }
}
