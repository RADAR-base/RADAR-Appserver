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

import jakarta.validation.ConstraintViolationException
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.radarbase.appserver.entity.Notification
import org.radarbase.appserver.entity.Project
import org.radarbase.appserver.entity.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Duration
import java.time.Instant

@ExtendWith(SpringExtension::class)
@DataJpaTest
@EnableJpaAuditing
class NotificationRepositoryTest {

    companion object {
        const val NOTIFICATION_BODY = "Test notif"
        const val NOTIFICATION_TITLE = "Testing"
        const val NOTIFICATION_FCM_MESSAGE_ID = "12345"
        const val NOTIFICATION_SOURCE_ID = "test"
        private const val TIMEZONE = "Europe/London"
    }

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var notificationRepository: NotificationRepository

    private var id: Long? = null
    private lateinit var user: User
    private lateinit var scheduledTime: Instant

    @BeforeEach
    fun initNotification() {
        val project = Project(null, "test-project")
        entityManager.persist(project)

        user = User(null, "test-user", null, "xxxx", project, Instant.now(), null, TIMEZONE, "en", null)
        entityManager.persist(user)

        scheduledTime = Instant.now().plus(Duration.ofSeconds(100))

        val notification = Notification.NotificationBuilder()
            .user(user)
            .body(NOTIFICATION_BODY)
            .title(NOTIFICATION_TITLE)
            .fcmMessageId(NOTIFICATION_FCM_MESSAGE_ID)
            .scheduledTime(scheduledTime)
            .sourceId(NOTIFICATION_SOURCE_ID)
            .ttlSeconds(86400)
            .delivered(false)
            .build()

        id = entityManager.persistAndGetId(notification) as Long
        entityManager.flush()
    }

    @Test
    fun `when insert with transient user then throw exception`() {
        val notification = Notification.NotificationBuilder()
            .user(User())
            .body(NOTIFICATION_BODY)
            .title(NOTIFICATION_TITLE)
            .fcmMessageId(NOTIFICATION_FCM_MESSAGE_ID)
            .scheduledTime(Instant.now().plus(Duration.ofSeconds(100)))
            .sourceId(NOTIFICATION_SOURCE_ID)
            .ttlSeconds(86400)
            .delivered(false)
            .build()

        val ex = assertThrows<IllegalStateException> {
            entityManager.persist(notification)
            entityManager.flush()
        }

        assertTrue(ex.message!!.contains("Not-null property references a transient value"))
    }

    @Test
    fun `when insert without user then throw exception`() {
        val notification = Notification.NotificationBuilder()
            .body(NOTIFICATION_BODY)
            .title(NOTIFICATION_TITLE)
            .fcmMessageId(NOTIFICATION_FCM_MESSAGE_ID)
            .scheduledTime(Instant.now().plus(Duration.ofSeconds(100)))
            .sourceId(NOTIFICATION_SOURCE_ID)
            .ttlSeconds(86400)
            .delivered(false)
            .build()

        assertThrows<ConstraintViolationException> {
            entityManager.persist(notification)
            entityManager.flush()
        }
    }

    @Test
    fun `when insert with user but transient project then throw exception`() {
        val user = User(null, "test-user", null, "xxxx", Project(), Instant.now(), null, TIMEZONE, "en", null)

        val ex = assertThrows<IllegalStateException> {
            entityManager.persist(user)
            entityManager.flush()
        }

        assertTrue(ex.message!!.contains("Not-null property references a transient value"))

        val notification = Notification.NotificationBuilder()
            .user(user)
            .body(NOTIFICATION_BODY)
            .title(NOTIFICATION_TITLE)
            .fcmMessageId(NOTIFICATION_FCM_MESSAGE_ID)
            .scheduledTime(Instant.now().plus(Duration.ofSeconds(100)))
            .sourceId(NOTIFICATION_SOURCE_ID)
            .ttlSeconds(86400)
            .delivered(false)
            .build()

        val ex2 = assertThrows<IllegalStateException> {
            entityManager.persist(notification)
            entityManager.flush()
        }

        assertTrue(ex2.suppressed.isNotEmpty())
        assertTrue(ex2.suppressed[0].message!!.contains("Not-null property references a transient value"))
    }

    @Test
    fun `when exists then return true`() {
        val exists = notificationRepository.existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
            user.id!!, NOTIFICATION_SOURCE_ID, scheduledTime, NOTIFICATION_TITLE, NOTIFICATION_BODY, null, 86400
        )

        assertTrue(exists)
        assertTrue(notificationRepository.existsById(id))
    }

    @Test
    fun `when delete notification by id then exists false`() {
        notificationRepository.deleteById(id)
        val notification = entityManager.find(Notification::class.java, id)
        assertNull(notification)
    }

    @Test
    fun `when delete notification by user id then exists false`() {
        notificationRepository.deleteByUserId(user.id!!)
        val notification = entityManager.find(Notification::class.java, id)
        assertNull(notification)
    }
}

