package org.radarbase.appserver.service

import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.radarbase.appserver.dto.fcm.FcmNotificationDto
import org.radarbase.appserver.dto.fcm.FcmNotifications
import org.radarbase.appserver.entity.Notification
import org.radarbase.appserver.entity.Project
import org.radarbase.appserver.entity.User
import org.radarbase.appserver.entity.UserMetrics
import org.radarbase.appserver.exception.NotFoundException
import org.radarbase.appserver.mapper.NotificationMapper
import org.radarbase.appserver.repository.NotificationRepository
import org.radarbase.appserver.repository.ProjectRepository
import org.radarbase.appserver.repository.UserRepository
import org.radarbase.appserver.service.scheduler.MessageSchedulerService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Duration
import java.time.Instant
import java.util.*

@Suppress("unused")
@ExtendWith(SpringExtension::class)
@DataJpaTest
class FcmNotificationServiceTest {

    private val scheduledTime: Instant = Instant.now().plus(Duration.ofSeconds(100))

    @MockBean
    private lateinit var schedulerService: MessageSchedulerService<*>

    @Autowired
    private lateinit var notificationService: FcmNotificationService

    @MockBean
    private lateinit var notificationRepository: NotificationRepository

    @MockBean
    private lateinit var userRepository: UserRepository

    @MockBean
    private lateinit var projectRepository: ProjectRepository

    private lateinit var user: User

    @BeforeEach
    fun setUp() {
        setUpProjectAndUser()
        setUpNotification1And2()

        val projectNew = Project(2L, PROJECT_ID)
        Mockito.`when`(projectRepository.save(Project(null, PROJECT_ID + NEW_SUFFIX)))
            .thenReturn(projectNew)

        val userMetrics = UserMetrics(null, Instant.now(), Instant.now(), null)
        val userNew = User(
            id = 2L,
            subjectId = USER_ID + NEW_SUFFIX,
            project = projectNew,
            fcmToken = FCM_TOKEN_1,
            enrolmentDate = Instant.now(),
            usermetrics = userMetrics
        )

        Mockito.`when`(userRepository.save(Mockito.any())).thenReturn(userNew)

        val notification3 = Notification.NotificationBuilder()
            .body(NOTIFICATION_BODY)
            .title(NOTIFICATION_TITLE_3)
            .scheduledTime(scheduledTime)
            .sourceId(NOTIFICATION_SOURCE_ID)
            .fcmMessageId("1234567")
            .ttlSeconds(86400)
            .delivered(false)
            .user(user)
            .id(3L)
            .build()
        notification3.createdAt = Date()

        Mockito.`when`(notificationRepository.save(notification3)).thenReturn(notification3)
        Mockito.`when`(notificationRepository.saveAndFlush(notification3)).thenReturn(notification3)

        Mockito.`when`(notificationRepository.findById(3L)).thenReturn(Optional.of(notification3))

        Mockito.`when`(
            notificationRepository
                .existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                    1L,
                    NOTIFICATION_SOURCE_ID,
                    scheduledTime,
                    NOTIFICATION_TITLE_3,
                    NOTIFICATION_BODY,
                    null,
                    86400
                )
        )
            .thenReturn(false)


        val notification4 = Notification.NotificationBuilder()
            .body(NOTIFICATION_BODY)
            .title(NOTIFICATION_TITLE_4)
            .scheduledTime(scheduledTime)
            .sourceId(NOTIFICATION_SOURCE_ID)
            .fcmMessageId("12345678")
            .ttlSeconds(86400)
            .delivered(false)
            .user(userNew)
            .id(4L)
            .build()

        notification4.createdAt = Date()
        notification4.updatedAt = Date()

        Mockito.`when`(notificationRepository.save(notification4)).thenReturn(notification4)
        Mockito.`when`(notificationRepository.saveAndFlush(notification4)).thenReturn(notification4)

        Mockito.`when`(notificationRepository.findById(4L)).thenReturn(Optional.of(notification4))

        Mockito.`when`(
            notificationRepository
                .existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                    2L,
                    NOTIFICATION_SOURCE_ID,
                    scheduledTime,
                    NOTIFICATION_TITLE_4,
                    NOTIFICATION_BODY,
                    null,
                    86400
                )
        )
            .thenReturn(false)

        val notification5 = Notification.NotificationBuilder()
            .body("$NOTIFICATION_BODY Updated")
            .title("Testing 2 Updated")
            .user(user)
            .scheduledTime(scheduledTime)
            .sourceId(NOTIFICATION_SOURCE_ID)
            .fcmMessageId(FCM_MESSAGE_ID)
            .ttlSeconds(86400)
            .delivered(false)
            .id(5L)
            .build()

        notification5.createdAt = Date()
        notification5.updatedAt = Date()

        Mockito.`when`(notificationRepository.save(notification5)).thenReturn(notification5)
        Mockito.`when`(notificationRepository.saveAndFlush(notification5)).thenReturn(notification5)

        Mockito.`when`(userRepository.findByFcmToken(FCM_TOKEN_1)).thenReturn(user)
    }

    private fun setUpProjectAndUser() {
        val project = Project(1L, PROJECT_ID)

        Mockito.`when`(projectRepository.findByProjectId(project.projectId)).thenReturn(project)

        user = User(
            id = 1L,
            subjectId = USER_ID,
            project = project,
            fcmToken = FCM_TOKEN_1,
            enrolmentDate = Instant.now(),
            usermetrics = UserMetrics(Instant.now(), Instant.now()),
            timezone = TIMEZONE,
            language = "en"
        )

        Mockito.`when`(userRepository.findBySubjectId(user.subjectId)).thenReturn(user)

        Mockito.`when`(userRepository.findBySubjectIdAndProjectId(USER_ID, 1L))
            .thenReturn(user)

        Mockito.`when`(userRepository.findByProjectId(1L)).thenReturn(listOf(user))
    }

    private fun setUpNotification1And2() {
        val notification1 = Notification.NotificationBuilder()
            .user(user)
            .body(NOTIFICATION_BODY)
            .title(NOTIFICATION_TITLE)
            .scheduledTime(scheduledTime)
            .sourceId(NOTIFICATION_SOURCE_ID)
            .fcmMessageId(NOTIFICATION_FCM_MESSAGE_ID)
            .ttlSeconds(86400)
            .delivered(false)
            .id(1L)
            .build()

        notification1.createdAt = Date()
        notification1.updatedAt = Date()


        val notification2 = Notification.NotificationBuilder()
            .user(user)
            .body(NOTIFICATION_BODY)
            .title(NOTIFICATION_TITLE_2)
            .scheduledTime(scheduledTime)
            .sourceId(NOTIFICATION_SOURCE_ID)
            .fcmMessageId(FCM_MESSAGE_ID)
            .ttlSeconds(86400)
            .delivered(false)
            .id(2L)
            .build()

        notification2.createdAt = Date()
        notification2.updatedAt = Date()

        Mockito.`when`(notificationRepository.findAll())
            .thenReturn(listOf(notification1, notification2))

        Mockito.`when`(notificationRepository.findByUserId(1L))
            .thenReturn(listOf(notification1, notification2))

        Mockito.`when`(notificationRepository.findByFcmMessageId(NOTIFICATION_FCM_MESSAGE_ID))
            .thenReturn(Optional.of(notification1))

        Mockito.`when`(notificationRepository.findByFcmMessageId(FCM_MESSAGE_ID))
            .thenReturn(Optional.of(notification2))

        Mockito.`when`(notificationRepository.findById(1L)).thenReturn(Optional.of(notification1))

        Mockito.`when`(notificationRepository.findById(2L)).thenReturn(Optional.of(notification2))

        Mockito.`when`(
            notificationRepository
                .existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                    1L,
                    NOTIFICATION_SOURCE_ID,
                    scheduledTime,
                    NOTIFICATION_TITLE,
                    NOTIFICATION_BODY,
                    null,
                    86400
                )
        )
            .thenReturn(true)

        Mockito.`when`(
            notificationRepository
                .existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                    1L,
                    NOTIFICATION_SOURCE_ID,
                    scheduledTime,
                    NOTIFICATION_TITLE_2,
                    NOTIFICATION_BODY,
                    null,
                    86400
                )
        )
            .thenReturn(true)

        Mockito.`when`(
            notificationRepository
                .existsByIdAndUserId(
                    1L,
                    1L
                )
        )
            .thenReturn(true)

        Mockito.`when`(notificationRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(notification1))

    }

    @Test
    fun getAllNotifications() {
        val fcmNotifications = notificationService.getAllNotifications()

        assertEquals(2, fcmNotifications.notifications.size)
        assertEquals(NOTIFICATION_TITLE, fcmNotifications.notifications[0].title)
        assertEquals(NOTIFICATION_TITLE_2, fcmNotifications.notifications[1].title)
    }

    @Test
    fun getNotificationById() {
        val notificationDto = notificationService.getNotificationById(1L)

        assertEquals(NOTIFICATION_TITLE, notificationDto.title)
        assertEquals(86400, notificationDto.ttlSeconds)
        assertEquals(NOTIFICATION_SOURCE_ID, notificationDto.sourceId)
    }

    @Test
    fun getNotificationsBySubjectId() {
        val fcmNotifications = notificationService.getNotificationsBySubjectId(USER_ID)

        assertEquals(2, fcmNotifications.notifications.size)
        assertEquals(NOTIFICATION_TITLE, fcmNotifications.notifications[0].title)
        assertEquals(NOTIFICATION_TITLE_2, fcmNotifications.notifications[1].title)
    }

    @Test
    fun getNotificationsByProjectIdAndSubjectId() {
        val notifications = notificationService.getNotificationsByProjectIdAndSubjectId(PROJECT_ID, USER_ID)

        assertEquals(2, notifications.notifications.size)
        assertEquals(NOTIFICATION_TITLE, notifications.notifications[0].title)
        assertEquals(NOTIFICATION_TITLE_2, notifications.notifications[1].title)
    }

    @Test
    fun getNotificationsByProjectId() {
        val notifications = notificationService.getNotificationsByProjectId(PROJECT_ID)

        assertEquals(2, notifications.notifications.size)
        assertTrue(notifications.notifications.any { it.title == NOTIFICATION_TITLE })
        assertTrue(notifications.notifications.any { it.title == NOTIFICATION_TITLE_2 })
    }

    @Test
    fun checkIfNotificationExists() {
        val notificationDto = FcmNotificationDto()
            .withBody(NOTIFICATION_BODY)
            .withTitle(NOTIFICATION_TITLE_2)
            .withScheduledTime(scheduledTime)
            .withSourceId(NOTIFICATION_SOURCE_ID)
            .withFcmMessageId(FCM_MESSAGE_ID)
            .withTtlSeconds(86400)
            .withDelivered(false)

        assertTrue(notificationService.checkIfNotificationExists(notificationDto, USER_ID))

        // A random notification should not exist
        assertFalse(
            notificationService.checkIfNotificationExists(
                FcmNotificationDto().withScheduledTime(Instant.now()), USER_ID
            )
        )
    }

    @Test
    @Disabled("Not implemented yet")
    fun getFilteredNotifications() {
        assert(true)
    }

    @Test
    fun addNotification() {
        val notificationDto = FcmNotificationDto()
            .withBody(NOTIFICATION_BODY)
            .withTitle(NOTIFICATION_TITLE_3)
            .withScheduledTime(scheduledTime)
            .withSourceId(NOTIFICATION_SOURCE_ID)
            .withFcmMessageId("1234567")
            .withTtlSeconds(86400)
            .withDelivered(false);

        notificationService.addNotification(notificationDto, USER_ID, PROJECT_ID)
        val savedNotification = notificationService.getNotificationById(3L)

        assertEquals(NOTIFICATION_TITLE_3, savedNotification.title)
        assertEquals("1234567", savedNotification.fcmMessageId)
    }

    @Test
    fun addNotificationWhenUserNotFound() {
        val notificationDto = FcmNotificationDto()
            .withBody(NOTIFICATION_BODY)
            .withTitle(NOTIFICATION_TITLE_4)
            .withScheduledTime(Instant.now())
            .withSourceId(NOTIFICATION_SOURCE_ID)
            .withFcmMessageId(FCM_MESSAGE_ID)
            .withTtlSeconds(86400)
            .withDelivered(false)

        val ex = assertThrows(NotFoundException::class.java) {
            notificationService.addNotification(notificationDto, "$USER_ID-2", PROJECT_ID)
        }

        assertTrue(
            ex.message!!.contains("The supplied Subject ID is invalid. No user found. Please Create a User First.")
        )
    }

    @Test
    fun addNotification_whenProjectNotFound() {
        val notificationDto = FcmNotificationDto()
            .withBody(NOTIFICATION_BODY)
            .withTitle(NOTIFICATION_TITLE_4)
            .withScheduledTime(Instant.now())
            .withSourceId(NOTIFICATION_SOURCE_ID)
            .withFcmMessageId(FCM_MESSAGE_ID)
            .withTtlSeconds(86400)
            .withDelivered(false)

        val ex = assertThrows(NotFoundException::class.java) {
            notificationService.addNotification(notificationDto, USER_ID, "$PROJECT_ID-2")
        }

        assertTrue(ex.message!!.contains("Project Id does not exist"))
    }

    @Test
    fun addNotifications() {
        val notificationDto1 = FcmNotificationDto()
            .withBody(NOTIFICATION_BODY)
            .withTitle(NOTIFICATION_TITLE_4)
            .withScheduledTime(scheduledTime)
            .withSourceId(NOTIFICATION_SOURCE_ID)
            .withFcmMessageId("12345678")
            .withTtlSeconds(86400)
            .withDelivered(false)


        val notificationDto2 = FcmNotificationDto()
            .withBody(NOTIFICATION_BODY + "2")
            .withTitle(NOTIFICATION_TITLE_4 + "3")
            .withScheduledTime(scheduledTime)
            .withSourceId(NOTIFICATION_SOURCE_ID)
            .withFcmMessageId("12345678")
            .withTtlSeconds(86400)
            .withDelivered(false)


        notificationService.addNotifications(
            FcmNotifications().withNotifications(listOf(notificationDto1, notificationDto2)),
            USER_ID,
            PROJECT_ID
        )

        val savedNotifications = notificationService.getNotificationsBySubjectId(USER_ID)

        assertEquals(2, savedNotifications.notifications.size)
        assertTrue(savedNotifications.notifications.any { it.body == NOTIFICATION_BODY })
    }

    @Test
    fun updateNotification() {
        val notificationDto = FcmNotificationDto()
            .withBody(NOTIFICATION_BODY + " Updated")
            .withTitle("Testing 2 Updated")
            .withScheduledTime(scheduledTime)
            .withSourceId(NOTIFICATION_SOURCE_ID)
            .withFcmMessageId(FCM_MESSAGE_ID)
            .withTtlSeconds(86400)
            .withDelivered(false)
            .withId(2L)

        val notificationDto1 = notificationService.updateNotification(notificationDto, USER_ID, PROJECT_ID)

        assertEquals("Test notif Updated", notificationDto1.body);
        assertEquals("Testing 2 Updated", notificationDto1.title);
    }

    // If does not throw CustomExceptionHandler then test is valid
    @Test
    fun removeNotificationsForUser() {
        assertDoesNotThrow {
            notificationService.removeNotificationsForUser(PROJECT_ID, USER_ID)
        }
    }

    @Test
    fun updateDeliveryStatus() {
        assertDoesNotThrow {
            notificationService.updateDeliveryStatus("12345", true)
        }
    }

    // Directly calls the repository so no need to assert. Just check that no exception is thrown
    @Test
    fun deleteNotificationByProjectIdAndSubjectIdAndId() {
        assertDoesNotThrow {
            notificationService.deleteNotificationByProjectIdAndSubjectIdAndNotificationId(PROJECT_ID, USER_ID, 1L)
        }
    }

    // If does not throw CustomExceptionHandler then test is valid
    @Test
    fun removeNotificationsForUserUsingFcmToken() {
        assertDoesNotThrow {
            notificationService.removeNotificationsForUserUsingFcmToken(FCM_TOKEN_1)
        }
    }

    @TestConfiguration
    class FcmNotificationServiceTestContextConfiguration {

        private val notificationConverter = NotificationMapper()

        @Autowired
        private lateinit var notificationRepository: NotificationRepository

        @Autowired
        private lateinit var userRepository: UserRepository

        @Autowired
        private lateinit var projectRepository: ProjectRepository

        @Autowired
        private lateinit var schedulerService: MessageSchedulerService<*>

        @Autowired
        private lateinit var eventPublisher: ApplicationEventPublisher

        @Bean
        fun notificationService(): NotificationService {
            return FcmNotificationService(
                notificationRepository,
                userRepository,
                projectRepository,
                schedulerService,
                notificationConverter,
                eventPublisher
            )
        }
    }

    companion object {
        private const val PROJECT_ID = "test-project"
        private const val USER_ID = "test-user"
        private const val NEW_SUFFIX = "-new"
        private const val NOTIFICATION_TITLE_1 = "Testing1"
        private const val NOTIFICATION_TITLE_2 = "Testing2"
        private const val NOTIFICATION_TITLE_3 = "Testing3"
        private const val NOTIFICATION_TITLE_4 = "Testing4"
        private const val TIMEZONE = "Europe/London"
        private const val NOTIFICATION_TITLE = "Testing"
        private const val NOTIFICATION_BODY = "Test notif"
        private const val NOTIFICATION_FCM_MESSAGE_ID = "12345"
        private const val NOTIFICATION_SOURCE_ID = "test"
        private const val FCM_TOKEN_1 = "xxxx"
        private const val FCM_MESSAGE_ID = "123456"
    }
}
