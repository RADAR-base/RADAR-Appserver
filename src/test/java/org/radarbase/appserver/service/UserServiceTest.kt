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

package org.radarbase.appserver.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.radarbase.appserver.dto.fcm.FcmUserDto
import org.radarbase.appserver.entity.Project
import org.radarbase.appserver.entity.User
import org.radarbase.appserver.entity.UserMetrics
import org.radarbase.appserver.mapper.UserMapper
import org.radarbase.appserver.repository.ProjectRepository
import org.radarbase.appserver.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Duration
import java.time.Instant
import java.util.*

@ExtendWith(SpringExtension::class)
@DataJpaTest
class UserServiceTest {

    @Autowired
    private lateinit var userService: UserService

    @MockBean
    private lateinit var userRepository: UserRepository

    @MockBean
    private lateinit var projectRepository: ProjectRepository

    private val enrolmentDate: Instant = Instant.now().plus(Duration.ofSeconds(100))

    @BeforeEach
    fun setUp() {

        val project = Project().apply {
            projectId = PROJECT_ID
            id = 1L
        }

        Mockito.`when`(projectRepository.findByProjectId(project.projectId)).thenReturn(project)

        val user = User().apply {
            fcmToken = FCM_TOKEN_1
            enrolmentDate = this@UserServiceTest.enrolmentDate
            this.project = project
            timezone = TIMEZONE
            language = "en"
            subjectId = USER_ID
            id = 1L
        }

        val userNew = User().apply {
            subjectId = "$USER_ID-2"
            fcmToken = FCM_TOKEN_1
            this.project = project
            enrolmentDate = this@UserServiceTest.enrolmentDate
            language = "es"
            timezone = TIMEZONE
        }

        val userUpdated = User().apply {
            subjectId = USER_ID
            fcmToken = "xxxxyyy"
            this.project = project
            enrolmentDate = this@UserServiceTest.enrolmentDate
            language = "es"
            timezone = TIMEZONE
            usermetrics = UserMetrics().apply {
                lastDelivered = this@UserServiceTest.enrolmentDate
                lastOpened = this@UserServiceTest.enrolmentDate
            }
        }

        Mockito.`when`(userRepository.findAll()).thenReturn(listOf(user))
        Mockito.`when`(userRepository.findById(1L)).thenReturn(Optional.of(user))
        Mockito.`when`(userRepository.findBySubjectId(user.subjectId)).thenReturn(user)
        Mockito.`when`(userRepository.findBySubjectIdAndProjectId(USER_ID, 1L)).thenReturn(user)
        Mockito.`when`(userRepository.findByProjectId(1L)).thenReturn(listOf(user))
        Mockito.`when`(userRepository.saveAndFlush(userNew)).thenReturn(userNew.apply { id = 2L })
        Mockito.`when`(userRepository.saveAndFlush(userUpdated)).thenReturn(userUpdated.apply { id = 1L })
    }

    @Test
    fun getAllRadarUsers() {
        val users = userService.getAllRadarUsers()
        assertEquals(USER_ID, users.users[0].subjectId)
        assertEquals("en", users.users[0].language)
        assertEquals(PROJECT_ID, users.users[0].projectId)
    }

    @Test
    fun getUserById() {
        val userDto = userService.getUserById(1L)
        assertEquals(USER_ID, userDto.subjectId)
        assertEquals("en", userDto.language)
        assertEquals(PROJECT_ID, userDto.projectId)
    }

    @Test
    fun getUserBySubjectId() {
        val userDto = userService.getUserBySubjectId(USER_ID)
        assertEquals(USER_ID, userDto.subjectId)
        assertEquals("en", userDto.language)
        assertEquals(PROJECT_ID, userDto.projectId)
        assertEquals(1L, userDto.id)
    }

    @Test
    fun getUsersByProjectId() {
        val users = userService.getUsersByProjectId(PROJECT_ID)
        assertEquals(USER_ID, users.users[0].subjectId)
        assertEquals("en", users.users[0].language)
        assertEquals(PROJECT_ID, users.users[0].projectId)
    }

    @Test
    fun saveUserInProject() {
        val userDtoNew = FcmUserDto().apply {
            subjectId = "$USER_ID-2"
            fcmToken = FCM_TOKEN_1
            projectId = PROJECT_ID
            enrolmentDate = this@UserServiceTest.enrolmentDate
            language = "es"
            timezone = TIMEZONE
        }


        val userDto = userService.saveUserInProject(userDtoNew)

        assertEquals("$USER_ID-2", userDto.subjectId)
        assertEquals("es", userDto.language)
        assertEquals(PROJECT_ID, userDto.projectId)
        assertEquals(2L, userDto.id)
    }

    @Test
    fun updateUser() {
        val userDtoNew = FcmUserDto().apply {
            subjectId = USER_ID
            fcmToken = "xxxxyyy"
            projectId = PROJECT_ID
            enrolmentDate = this@UserServiceTest.enrolmentDate
            language = "es"
            lastDelivered = this@UserServiceTest.enrolmentDate
            lastOpened = this@UserServiceTest.enrolmentDate
            attributes = HashMap()
            timezone = "Europe/Bucharest"
        }

        val userDto = userService.updateUser(userDtoNew)

        assertEquals(USER_ID, userDto.subjectId)
        assertEquals("es", userDto.language)
        assertEquals(PROJECT_ID, userDto.projectId)
        assertEquals("Europe/Bucharest", userDto.timezone)
        assertEquals("xxxxyyy", userDto.fcmToken)
        assertEquals(1L, userDto.id)
    }

    @TestConfiguration
    class UserServiceConfig {

        @Autowired
        private lateinit var userRepository: UserRepository

        @Autowired
        private lateinit var projectRepository: ProjectRepository

        @Autowired
        private lateinit var scheduleService: QuestionnaireScheduleService

        private val userMapper = UserMapper()

        @Bean
        fun userServiceBeanConfig(): UserService {
            return UserService(false, userMapper, userRepository, projectRepository, scheduleService)
        }
    }

    companion object {
        private const val TIMEZONE = "Europe/Bucharest"
        private const val USER_ID = "test-user"
        private const val PROJECT_ID = "test-project"
        private const val FCM_TOKEN_1 = "xxxx"
    }
}
