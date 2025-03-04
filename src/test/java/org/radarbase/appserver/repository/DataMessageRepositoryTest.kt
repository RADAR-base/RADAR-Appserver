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
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.radarbase.appserver.entity.DataMessage
import org.radarbase.appserver.entity.Project
import org.radarbase.appserver.entity.User
import org.slf4j.LoggerFactory
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
class DataMessageRepositoryTest {

    companion object {
        private val logger = LoggerFactory.getLogger(DataMessageRepositoryTest::class.java)
        const val DATA_MESSAGE_FCM_MESSAGE_ID = "12345"
        const val DATA_MESSAGE_SOURCE_ID = "test"
        private const val TIMEZONE = "Europe/London"
    }

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var dataMessageRepository: DataMessageRepository

    private var id: Long? = null
    private lateinit var user: User
    private lateinit var scheduledTime: Instant

    @BeforeEach
    fun initDataMessage() {
        val project = Project(null, "test-project")
        entityManager.persist(project)

        user = User(null, "test-user", null, "xxxx", project, Instant.now(), null, TIMEZONE, "en", null)
        entityManager.persist(user)

        scheduledTime = Instant.now().plus(Duration.ofSeconds(100))

        val dataMessage = DataMessage.DataMessageBuilder()
            .user(user)
            .fcmMessageId(DATA_MESSAGE_FCM_MESSAGE_ID)
            .scheduledTime(scheduledTime)
            .sourceId(DATA_MESSAGE_SOURCE_ID)
            .ttlSeconds(86400)
            .delivered(false)
            .build()

        id = entityManager.persistAndGetId(dataMessage) as Long
        entityManager.flush()
    }

    @Test
    fun `when insert with transient user then throw exception`() {
        val dataMessage = DataMessage.DataMessageBuilder()
            .user(User())
            .fcmMessageId(DATA_MESSAGE_FCM_MESSAGE_ID)
            .scheduledTime(Instant.now().plus(Duration.ofSeconds(100)))
            .sourceId(DATA_MESSAGE_SOURCE_ID)
            .ttlSeconds(86400)
            .delivered(false)
            .build()

        val ex = assertThrows(IllegalStateException::class.java) {
            entityManager.persist(dataMessage)
            entityManager.flush()
        }

        assertTrue(ex.message!!.contains("Not-null property references a transient value"))
    }

    @Test
    fun `when insert without user then throw exception`() {
        val dataMessage = DataMessage.DataMessageBuilder()
            .fcmMessageId(DATA_MESSAGE_FCM_MESSAGE_ID)
            .scheduledTime(Instant.now().plus(Duration.ofSeconds(100)))
            .sourceId(DATA_MESSAGE_SOURCE_ID)
            .ttlSeconds(86400)
            .delivered(false)
            .build()

        assertThrows(ConstraintViolationException::class.java) {
            entityManager.persist(dataMessage)
            entityManager.flush()
        }
    }

    @Test
    fun `when insert with user but transient project then throw exception`() {
        val invalidUser = User(null, "test-user", null, "xxxx", Project(), Instant.now(), null, TIMEZONE, "en", null)

        val ex1 = assertThrows(IllegalStateException::class.java) {
            entityManager.persist(invalidUser)
            entityManager.flush()
        }

        assertTrue(ex1.message!!.contains("Not-null property references a transient value"))

        val dataMessage = DataMessage.DataMessageBuilder()
            .user(invalidUser)
            .fcmMessageId(DATA_MESSAGE_FCM_MESSAGE_ID)
            .scheduledTime(Instant.now().plus(Duration.ofSeconds(100)))
            .sourceId(DATA_MESSAGE_SOURCE_ID)
            .ttlSeconds(86400)
            .delivered(false)
            .build()

        val ex2 = assertThrows(IllegalStateException::class.java) {
            entityManager.persist(dataMessage)
            entityManager.flush()
        }

        assertTrue(ex2.suppressed.isNotEmpty())
        assertTrue(ex2.suppressed[0].message!!.contains("Not-null property references a transient value"))
    }

    @Test
    fun `when exists then return true`() {
        val exists = dataMessageRepository.existsByUserIdAndSourceIdAndScheduledTimeAndTtlSeconds(
            user.id!!, DATA_MESSAGE_SOURCE_ID, scheduledTime, 86400)

        assertTrue(exists)
        assertTrue(dataMessageRepository.existsById(id!!))
    }

    @Test
    fun `when delete data message by id then exists false`() {
        dataMessageRepository.deleteById(id!!)

        val dataMessage = entityManager.find(DataMessage::class.java, id)
        assertNull(dataMessage)
    }

    @Test
    fun `when delete data message by user id then exists false`() {
        dataMessageRepository.deleteByUserId(user.id!!)

        val dataMessage = entityManager.find(DataMessage::class.java, id)
        assertNull(dataMessage)
    }
}
