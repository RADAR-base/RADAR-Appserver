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

import jakarta.persistence.PersistenceException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.radarbase.appserver.controller.FcmDataMessageControllerTest.Companion.USER_ID
import org.radarbase.appserver.controller.RadarUserControllerTest.Companion.FCM_TOKEN_1
import org.radarbase.appserver.controller.RadarUserControllerTest.Companion.TIMEZONE
import org.radarbase.appserver.entity.Project
import org.radarbase.appserver.entity.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.sql.SQLIntegrityConstraintViolationException
import java.time.Instant


@ExtendWith(SpringExtension::class)
@DataJpaTest
@EnableJpaAuditing
class UserRepositoryTest {

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Autowired
    private lateinit var userRepository: UserRepository

    private lateinit var project: Project
    private var projectId: Long? = null
    private var userId: Long? = null

    @BeforeEach
    fun setUp() {
        this.project = Project(null, "test-project")
        this.projectId = entityManager.persistAndGetId(project) as Long

        val user = User(
            null, USER_ID, null, FCM_TOKEN_1, project,
            Instant.now(), null, TIMEZONE, "en", null
        )

        this.userId = entityManager.persistAndGetId(user) as Long
        entityManager.flush()
    }

    @Test
    fun `when insert with transient project then throw exception`() {
        val user1 = User(
            null, USER_ID, null, FCM_TOKEN_1, Project(),
            Instant.now(), null, TIMEZONE, "en", null
        )

        val ex = assertThrows<IllegalStateException> {
            entityManager.persist(user1)
            entityManager.flush()
        }

        assertTrue(ex.message!!.contains("Not-null property references a transient value"))
    }

    @Test
    fun `when find user by subject id then return user`() {
        assertEquals(userRepository.findBySubjectId(USER_ID), entityManager.find(User::class.java, this.userId))
    }

    @Test
    fun `when find by project id then return users`() {
        assertEquals(userRepository.findByProjectId(this.projectId!!)[0], entityManager.find(User::class.java, this.userId))
    }

    @Test
    fun `when find by subject id and project id then return user`() {
        assertEquals(
            userRepository.findBySubjectIdAndProjectId(USER_ID, this.projectId!!),
            entityManager.find(User::class.java, this.userId)
        )
    }

    @Test
    fun `when find by FCM token then return user`() {
        assertEquals(userRepository.findByFcmToken(FCM_TOKEN_1), entityManager.find(User::class.java, this.userId))
    }

    @Test
    fun `when insert with existing FCM token then throw exception`() {
        val user1 = User(
            null, "$USER_ID-2", null, FCM_TOKEN_1, this.project,
            Instant.now(), null, TIMEZONE, "en", null
        )

        val ex = assertThrows<PersistenceException> {
            entityManager.persistAndGetId(user1)
            entityManager.flush()
        }

        assertEquals(SQLIntegrityConstraintViolationException::class.java, ex.cause!!::class.java)
    }
}

