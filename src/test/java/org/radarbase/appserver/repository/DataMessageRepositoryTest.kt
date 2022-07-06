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
import org.radarbase.appserver.entity.DataMessage
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
class DataMessageRepositoryTest {
    @Autowired
    @Transient
    lateinit var entityManager: TestEntityManager

    @Autowired
    @Transient
    lateinit var dataMessageRepository: DataMessageRepository

    @Transient
    private var id: Long? = null

    @Transient
    private lateinit var user: User

    @Transient
    private lateinit var scheduledTime: Instant

    /**
     * Insert a DataMessage Before each test.
     */
    @BeforeEach
    fun initDataMessage() {
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
        val dataMessage = DataMessage()
        dataMessage.fcmMessageId = DATA_MESSAGE_FCM_MESSAGE_ID
        dataMessage.sourceId = DATA_MESSAGE_SOURCE_ID
        dataMessage.scheduledTime = scheduledTime
        dataMessage.user = user
        dataMessage.ttlSeconds = 86400
        dataMessage.delivered = false
        id = entityManager.persistAndGetId(dataMessage) as Long
        entityManager.flush()
    }

    @Test
    fun whenInsertWithTransientUser_thenThrowException() {
        // given
        val dataMessage = DataMessage()
        dataMessage.user = User()
        dataMessage.sourceId = DATA_MESSAGE_SOURCE_ID
        dataMessage.scheduledTime = Instant.now().plus(Duration.ofSeconds(100))
        dataMessage.ttlSeconds = 86400
        dataMessage.delivered = false
        dataMessage.fcmMessageId = DATA_MESSAGE_FCM_MESSAGE_ID
        val ex = Assertions.assertThrows(
            IllegalStateException::class.java
        ) {
            entityManager.persist(dataMessage)
            entityManager.flush()
        }
        Assertions.assertTrue(ex.message!!.contains("Not-null property references a transient value"))
    }

    @Test
    fun whenInsertWithoutUser_thenThrowException() {
        // given
        val dataMessage = DataMessage()
        dataMessage.sourceId = DATA_MESSAGE_SOURCE_ID
        dataMessage.scheduledTime = Instant.now().plus(Duration.ofSeconds(100))
        dataMessage.ttlSeconds = 86400
        dataMessage.delivered = false
        dataMessage.fcmMessageId = DATA_MESSAGE_FCM_MESSAGE_ID
        Assertions.assertThrows(
            PersistenceException::class.java
        ) {
            entityManager.persist(dataMessage)
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
        val dataMessage = DataMessage()
        dataMessage.user = user
        dataMessage.sourceId = DATA_MESSAGE_SOURCE_ID
        dataMessage.scheduledTime = Instant.now().plus(Duration.ofSeconds(100))
        dataMessage.ttlSeconds = 86400
        dataMessage.delivered = false
        dataMessage.fcmMessageId = DATA_MESSAGE_FCM_MESSAGE_ID
        ex = Assertions.assertThrows(
            IllegalStateException::class.java
        ) {
            entityManager.persist(dataMessage)
            entityManager.flush()
        }
        Assertions.assertTrue(ex.message!!.contains("Not-null property references a transient value"))
    }

    @Test
    fun whenExists_thenReturnTrue() {
        // when
        val exists = dataMessageRepository
            .existsByUserIdAndSourceIdAndScheduledTimeAndTtlSeconds(
                user.id,
                DATA_MESSAGE_SOURCE_ID,
                scheduledTime,
                86400
            )

        // then
        Assertions.assertTrue(exists)
        Assertions.assertTrue(dataMessageRepository.existsById(id))
    }

    @Test
    fun whenDeleteDataMessageById_thenExistsFalse() {
        // when
        dataMessageRepository.deleteById(id)

        // then
        val dataMessage = entityManager.find(DataMessage::class.java, id)
        Assertions.assertNull(dataMessage)
    }

    @Test
    fun whenDeleteDataMessageByUserId_thenExistsFalse() {
        // when
        dataMessageRepository.deleteByUserId(user.id)

        // then
        val dataMessage = entityManager.find(DataMessage::class.java, id)
        Assertions.assertNull(dataMessage)
    }

    companion object {
        const val DATA_MESSAGE_FCM_MESSAGE_ID = "12345"
        const val DATA_MESSAGE_SOURCE_ID = "test"
    }
}