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
package org.radarbase.appserver.repository

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.radarbase.appserver.controller.RadarUserControllerTest
import org.radarbase.appserver.entity.Notification
import org.radarbase.appserver.entity.Project
import org.radarbase.appserver.entity.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Duration
import java.time.Instant
import javax.persistence.PersistenceException

@ExtendWith(SpringExtension::class)
@DataJpaTest
@EnableJpaAuditing
@EnableJpaRepositories("org.radarbase.appserver.repository")
class NotificationRepositoryTest {
    @Autowired
    @Transient
    lateinit var entityManager: TestEntityManager

    @Autowired
    @Transient
    lateinit var notificationRepository: NotificationRepository

    @Transient
    private var id: Long? = null

    @Transient
    private lateinit var user: User

    @Transient
    private lateinit var scheduledTime: Instant

    /**
     * Insert a Notification Before each test.
     */
    @BeforeEach
    fun initNotification() {
        // given
        val project = Project().setProjectId("test-project")
        entityManager.persist(project)
        user = User()
            .setFcmToken("xxxx")
            .setEnrolmentDate(Instant.now())
            .setProject(project)
            .setTimezone(RadarUserControllerTest.TIMEZONE)
            .setLanguage("en")
            .setSubjectId("test-user")
        entityManager.persist(user)
        scheduledTime = Instant.now().plus(Duration.ofSeconds(100))
        val notification = Notification()
        notification.body = NOTIFICATION_BODY
        notification.title = NOTIFICATION_TITLE
        notification.fcmMessageId = NOTIFICATION_FCM_MESSAGE_ID
        notification.sourceId = NOTIFICATION_SOURCE_ID
        notification.scheduledTime = scheduledTime
        notification.user = user
        notification.ttlSeconds = 86400
        notification.delivered = false
        id = entityManager.persistAndGetId(notification) as Long
        entityManager.flush()
    }

    @Test
    fun whenInsertWithTransientUser_thenThrowException() {
        // given
        val notification = Notification()
        notification.body = NOTIFICATION_BODY
        notification.title = NOTIFICATION_TITLE
        notification.fcmMessageId = NOTIFICATION_FCM_MESSAGE_ID
        notification.sourceId = NOTIFICATION_SOURCE_ID
        notification.scheduledTime = Instant.now().plus(Duration.ofSeconds(100))
        notification.user = User()
        notification.ttlSeconds = 86400
        notification.delivered = false
        val ex = Assertions.assertThrows(
            IllegalStateException::class.java
        ) {
            entityManager.persist(notification)
            entityManager.flush()
        }
        Assertions.assertTrue(ex.message!!.contains("Not-null property references a transient value"))
    }

    @Test
    fun whenInsertWithoutUser_thenThrowException() {
        // given
        val notification = Notification()
        notification.body = NOTIFICATION_BODY
        notification.title = NOTIFICATION_TITLE
        notification.fcmMessageId = NOTIFICATION_FCM_MESSAGE_ID
        notification.sourceId = NOTIFICATION_SOURCE_ID
        notification.scheduledTime = Instant.now().plus(Duration.ofSeconds(100))
        notification.ttlSeconds = 86400
        notification.delivered = false
        Assertions.assertThrows(
            PersistenceException::class.java
        ) {
            entityManager.persist(notification)
            entityManager.flush()
        }
    }

    @Test
    fun whenInsertWithUserButTransientProject_thenThrowException() {
        // given
        val user = User()
            .setFcmToken("xxxx")
            .setEnrolmentDate(Instant.now())
            .setProject(Project())
            .setTimezone(RadarUserControllerTest.TIMEZONE)
            .setLanguage("en")
            .setSubjectId("test-user")
        var ex = Assertions.assertThrows(
            IllegalStateException::class.java
        ) {
            entityManager.persist(user)
            entityManager.flush()
        }
        Assertions.assertTrue(ex.message!!.contains("Not-null property references a transient value"))
        val notification = Notification()
        notification.body = NOTIFICATION_BODY
        notification.title = NOTIFICATION_TITLE
        notification.fcmMessageId = NOTIFICATION_FCM_MESSAGE_ID
        notification.sourceId = NOTIFICATION_SOURCE_ID
        notification.scheduledTime = Instant.now().plus(Duration.ofSeconds(100))
        notification.user = user
        notification.ttlSeconds = 86400
        notification.delivered = false
        ex = Assertions.assertThrows(
            IllegalStateException::class.java
        ) {
            entityManager.persist(notification)
            entityManager.flush()
        }
        Assertions.assertTrue(ex.message!!.contains("Not-null property references a transient value"))
    }

    @Test
    fun whenExists_thenReturnTrue() {
        // when
        val exists = notificationRepository
            .existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                user.id,
                NOTIFICATION_SOURCE_ID,
                scheduledTime,
                NOTIFICATION_TITLE,
                NOTIFICATION_BODY,
                null,
                86400
            )

        // then
        Assertions.assertTrue(exists)
        Assertions.assertTrue(notificationRepository.existsById(id))
    }

    @Test
    fun whenDeleteNotificationById_thenExistsFalse() {
        // when
        notificationRepository.deleteById(id)

        // then
        val notification = entityManager.find(
            Notification::class.java, id
        )
        Assertions.assertNull(notification)
    }

    @Test
    fun whenDeleteNotificationByUserId_thenExistsFalse() {
        // when
        notificationRepository.deleteByUserId(user.id)

        // then
        val notification = entityManager.find(
            Notification::class.java, id
        )
        Assertions.assertNull(notification)
    }

    companion object {
        const val NOTIFICATION_BODY = "Test notif"
        const val NOTIFICATION_TITLE = "Testing"
        const val NOTIFICATION_FCM_MESSAGE_ID = "12345"
        const val NOTIFICATION_SOURCE_ID = "test"
    }
}