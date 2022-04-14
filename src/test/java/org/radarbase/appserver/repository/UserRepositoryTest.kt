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

import org.hibernate.exception.ConstraintViolationException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.radarbase.appserver.controller.FcmNotificationControllerTest
import org.radarbase.appserver.controller.RadarUserControllerTest
import org.radarbase.appserver.entity.Project
import org.radarbase.appserver.entity.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Instant
import javax.persistence.PersistenceException

@ExtendWith(SpringExtension::class)
@DataJpaTest
@EnableJpaAuditing
internal class UserRepositoryTest {
    @Autowired
    @Transient
    lateinit var entityManager: TestEntityManager

    @Autowired
    @Transient
    lateinit var userRepository: UserRepository

    @Transient
    private var project: Project? = null

    @Transient
    private var projectId: Long? = null

    @Transient
    private var userId: Long? = null

    @BeforeEach
    fun setUp() {
        project = Project().setProjectId("test-project")
        projectId = entityManager.persistAndGetId(project) as Long
        val user = User()
            .setFcmToken(RadarUserControllerTest.FCM_TOKEN_1)
            .setEnrolmentDate(Instant.now())
            .setProject(project)
            .setTimezone(RadarUserControllerTest.TIMEZONE)
            .setLanguage("en")
            .setSubjectId(FcmNotificationControllerTest.USER_ID)
        userId = entityManager.persistAndGetId(user) as Long
        entityManager.flush()
    }

    @Test
    fun whenInsertWithTransientProject_thenThrowException() {
        val user1 = User()
            .setFcmToken(RadarUserControllerTest.FCM_TOKEN_1)
            .setEnrolmentDate(Instant.now())
            .setProject(Project())
            .setTimezone(RadarUserControllerTest.TIMEZONE)
            .setLanguage("en")
            .setSubjectId(FcmNotificationControllerTest.USER_ID)
        val ex = Assertions.assertThrows(
            IllegalStateException::class.java
        ) {
            entityManager.persist(user1)
            entityManager.flush()
        }
        Assertions.assertTrue(ex.message!!.contains("Not-null property references a transient value"))
    }

    @Test
    fun whenFindUserBySubjectId_thenReturnUser() {
        Assertions.assertEquals(
            userRepository.findBySubjectId(FcmNotificationControllerTest.USER_ID)!!.get(),
            entityManager.find(
                User::class.java, userId
            )
        )
    }

    @Test
    fun whenFindByProjectId_thenReturnUsers() {
        Assertions.assertEquals(
            userRepository.findByProjectId(projectId)!![0],
            entityManager.find(User::class.java, userId)
        )
    }

    @Test
    fun whenFindBySubjectIdAndProjectId_thenReturnUser() {
        Assertions.assertEquals(
            userRepository.findBySubjectIdAndProjectId(
                FcmNotificationControllerTest.USER_ID,
                projectId
            )!!.get(),
            entityManager.find(User::class.java, userId)
        )
    }

    @Test
    fun whenFindByFcmToken_thenReturnUser() {
        Assertions.assertEquals(
            userRepository.findByFcmToken(RadarUserControllerTest.FCM_TOKEN_1)!!.get(),
            entityManager.find(User::class.java, userId)
        )
    }

    @Test
    fun whenInsertWithExistingFcmToken_thenThrowException() {
        val user1 = User()
            .setFcmToken(RadarUserControllerTest.FCM_TOKEN_1)
            .setEnrolmentDate(Instant.now())
            .setProject(project)
            .setTimezone(RadarUserControllerTest.TIMEZONE)
            .setLanguage("en")
            .setSubjectId(FcmNotificationControllerTest.USER_ID + "-2")
        val ex = Assertions.assertThrows(
            PersistenceException::class.java
        ) {
            entityManager.persistAndGetId(user1) as Long
            entityManager.flush()
        }
        Assertions.assertEquals(ConstraintViolationException::class.java, ex.cause!!.javaClass)
    }
}