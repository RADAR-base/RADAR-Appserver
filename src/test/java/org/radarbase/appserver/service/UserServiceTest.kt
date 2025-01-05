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

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer
import org.radarbase.appserver.controller.FcmNotificationControllerTest
import org.radarbase.appserver.controller.RadarUserControllerTest
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
import java.util.List

@ExtendWith(SpringExtension::class)
@DataJpaTest
internal class UserServiceTest {
    @Autowired
    @Transient
    private val userService: UserService? = null

    @MockBean
    @Transient
    private val userRepository: UserRepository? = null

    @MockBean
    @Transient
    private val projectRepository: ProjectRepository? = null

    @MockBean
    @Transient
    private val scheduleService: QuestionnaireScheduleService? = null

    @Transient
    private val enrolmentDate: Instant? = Instant.now().plus(Duration.ofSeconds(100))

    @BeforeEach
    fun setUp() {
        // given
        val project = Project(1L, FcmNotificationControllerTest.PROJECT_ID)

        Mockito.`when`<Project?>(projectRepository!!.findByProjectId(project.projectId))
            .thenReturn(project)

        val user = User(
            1L,
            FcmNotificationControllerTest.USER_ID,
            null,
            RadarUserControllerTest.FCM_TOKEN_1,
            project,
            enrolmentDate,
            null,
            TIMEZONE,
            "en",
            null
        )

        Mockito.`when`<MutableList<User>?>(userRepository!!.findAll()).thenReturn(List.of<User>(user))

        Mockito.`when`<Optional<User>?>(userRepository.findById(1L)).thenReturn(Optional.of<User>(user))

        Mockito.`when`<User?>(userRepository.findBySubjectId(user.subjectId)).thenReturn(user)

        Mockito.`when`<User?>(userRepository.findBySubjectIdAndProjectId(FcmNotificationControllerTest.USER_ID, 1L))
            .thenReturn(user)

        Mockito.`when`<MutableList<User>?>(userRepository.findByProjectId(1L)).thenReturn(List.of<User>(user))

        val newUser = User(
            null,
            FcmNotificationControllerTest.USER_ID + "-2",
            null,
            RadarUserControllerTest.FCM_TOKEN_1,
            project,
            enrolmentDate,
            null,
            TIMEZONE,
            "es",
            null
        )

        val userMetrics2 = UserMetrics(null, null, null, newUser)
        newUser.usermetrics = userMetrics2

        val newUserWithId = User(
            2L,
            FcmNotificationControllerTest.USER_ID + "-2",
            null,
            RadarUserControllerTest.FCM_TOKEN_1,
            project,
            enrolmentDate,
            null,
            TIMEZONE,
            "es",
            null
        )

        Mockito.`when`<User?>(userRepository.saveAndFlush<User?>(newUser))
            .thenAnswer(Answer { invocation: InvocationOnMock? ->
                val userN = invocation!!.getArgument<User>(0)
                println("CheckPoint UserN: " + userN)
                User(
                    2L,
                    userN.subjectId,
                    userN.emailAddress,
                    userN.fcmToken,
                    userN.project,
                    userN.enrolmentDate,
                    userN.usermetrics,
                    userN.timezone,
                    userN.language,
                    userN.attributes
                )
            })


        val userMetrics = UserMetrics(enrolmentDate, enrolmentDate)

        val updatedUser = User(
            null,
            FcmNotificationControllerTest.USER_ID,
            null,
            "xxxxyyy",
            project,
            enrolmentDate,
            userMetrics,
            TIMEZONE,
            "es",
            null
        )
        val updatedUserWithId = User(
            1L,
            FcmNotificationControllerTest.USER_ID,
            null,
            "xxxxyyy",
            project,
            enrolmentDate,
            userMetrics,
            TIMEZONE,
            "es",
            null
        )
        Mockito.`when`<User?>(userRepository.saveAndFlush<User?>(updatedUser)).thenReturn(updatedUserWithId)
    }

    @get:Test
    val allRadarUsers: Unit
        get() {
            val users = userService!!.getAllRadarUsers()

            Assertions.assertEquals(
                FcmNotificationControllerTest.USER_ID,
                users.users.get(0).subjectId
            )
            Assertions.assertEquals("en", users.users.get(0).language, "en")
            Assertions.assertEquals(
                FcmNotificationControllerTest.PROJECT_ID,
                users.users.get(0).projectId
            )
        }

    @get:Test
    val userById: Unit
        get() {
            val userDto = userService!!.getUserById(1L)

            Assertions.assertEquals(FcmNotificationControllerTest.USER_ID, userDto.subjectId)
            Assertions.assertEquals("en", userDto.language)
            Assertions.assertEquals(FcmNotificationControllerTest.PROJECT_ID, userDto.projectId)
        }

    @get:Test
    val userBySubjectId: Unit
        get() {
            val userDto = userService!!.getUserBySubjectId(FcmNotificationControllerTest.USER_ID)

            Assertions.assertEquals(FcmNotificationControllerTest.USER_ID, userDto.subjectId)
            Assertions.assertEquals("en", userDto.language)
            Assertions.assertEquals(FcmNotificationControllerTest.PROJECT_ID, userDto.projectId)
            Assertions.assertEquals(1L, userDto.id)
        }

    @get:Test
    val usersByProjectId: Unit
        get() {
            val users = userService!!.getUsersByProjectId(FcmNotificationControllerTest.PROJECT_ID)

            Assertions.assertEquals(
                FcmNotificationControllerTest.USER_ID,
                users.users.get(0).subjectId
            )
            Assertions.assertEquals("en", users.users.get(0).language, "en")
            Assertions.assertEquals(
                FcmNotificationControllerTest.PROJECT_ID,
                users.users.get(0).projectId
            )
        }

    @Test
    fun saveUserInProject() {
        val userDtoNew =
            FcmUserDto(
                null,
                FcmNotificationControllerTest.PROJECT_ID,
                FcmNotificationControllerTest.USER_ID + "-2",
                null,
                null,
                null,
                null,
                null,
                enrolmentDate,
                TIMEZONE,
                RadarUserControllerTest.FCM_TOKEN_1,
                "es",
                null
            )

        val userDto = userService!!.saveUserInProject(userDtoNew)

        Assertions.assertEquals(FcmNotificationControllerTest.USER_ID + "-2", userDto.subjectId)
        Assertions.assertEquals("es", userDto.language)
        Assertions.assertEquals(FcmNotificationControllerTest.PROJECT_ID, userDto.projectId)
        Assertions.assertEquals(2L, userDto.id)
    }

    @Test
    fun updateUser() {
        val userDtoNew =
            FcmUserDto(
                null,
                FcmNotificationControllerTest.PROJECT_ID,
                FcmNotificationControllerTest.USER_ID,
                null,
                enrolmentDate,
                enrolmentDate,
                null,
                null,
                enrolmentDate,
                "Europe/Bucharest",
                "xxxxyyy",
                "es",
                HashMap<String?, String?>()
            )

        val userDto = userService!!.updateUser(userDtoNew)

        Assertions.assertEquals(FcmNotificationControllerTest.USER_ID, userDto.subjectId)
        Assertions.assertEquals("es", userDto.language)
        Assertions.assertEquals(FcmNotificationControllerTest.PROJECT_ID, userDto.projectId)
        Assertions.assertEquals("Europe/Bucharest", userDto.timezone)
        Assertions.assertEquals("xxxxyyy", userDto.fcmToken)
        Assertions.assertEquals(1L, userDto.id)
    }

    @TestConfiguration
    internal class UserServiceConfig {
        @Autowired
        @Transient
        private val userRepository: UserRepository? = null

        @Autowired
        @Transient
        private val projectRepository: ProjectRepository? = null

        @Autowired
        @Transient
        private val scheduleService: QuestionnaireScheduleService? = null

        @Transient
        private val userMapper = UserMapper()

        @Bean
        fun userServiceBeanConfig(): UserService {
            return UserService(false, userMapper, userRepository!!, projectRepository!!, scheduleService!!)
        }
    }

    companion object {
        private const val TIMEZONE = "Europe/Bucharest"
    }
}
