/*
 *
 *  *  Copyright 2018 King's College London
 *  *
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *
 */

package org.radarbase.appserver.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.radarbase.appserver.controller.FcmDataMessageControllerTest.Companion.USER_ID
import org.radarbase.appserver.controller.RadarProjectControllerTest.Companion.ID_JSON_PATH
import org.radarbase.appserver.controller.RadarProjectControllerTest.Companion.PROJECT_ID
import org.radarbase.appserver.dto.fcm.FcmUserDto
import org.radarbase.appserver.dto.fcm.FcmUsers
import org.radarbase.appserver.exception.InvalidUserDetailsException
import org.radarbase.appserver.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.net.URI
import java.time.Duration
import java.time.Instant

@ExtendWith(SpringExtension::class)
@WebMvcTest(RadarUserController::class)
@AutoConfigureMockMvc(addFilters = false)
class RadarUserControllerTest {

    companion object {
        const val FCM_TOKEN_1 = "xxxx"
        private const val FCM_TOKEN_2 = "xxxxyyy"
        private const val FCM_TOKEN_3 = "xxxxyyyzzz"
        private const val FCM_TOKEN_JSON_PATH = "$.fcmToken"
        private const val LANGUAGE_JSON_PATH = "$.language"
        private const val ENROLMENT_DATE_JSON_PATH = "$.enrolmentDate"
        const val TIMEZONE = "Europe/London"
    }

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var userService: UserService

    private val enrolmentDate: Instant = Instant.now().plus(Duration.ofSeconds(100))

    @BeforeEach
    fun setUp() {
        val userDto = FcmUserDto()
            .apply {
                subjectId = USER_ID
                fcmToken = FCM_TOKEN_1
                projectId = PROJECT_ID
                enrolmentDate = this@RadarUserControllerTest.enrolmentDate
                language = "es"
                timezone = TIMEZONE
            }

        given(userService.getAllRadarUsers())
            .willReturn(FcmUsers(listOf(userDto.apply { id = 1L })))

        given(userService.getUserById(1L)).willReturn(userDto.apply { id = 1L })
        given(userService.getUserBySubjectId(USER_ID)).willReturn(userDto.apply { id = 1L })
        given(userService.getUsersByProjectId(PROJECT_ID))
            .willReturn(FcmUsers(listOf(userDto.apply { id = 1L })))

        val userDtoNew = FcmUserDto().apply {
            subjectId = "$USER_ID-new"
            fcmToken = FCM_TOKEN_2
            projectId = PROJECT_ID
            enrolmentDate = this@RadarUserControllerTest.enrolmentDate
            language = "en"
            timezone = TIMEZONE
            id = 2L
        }

        given(userService.saveUserInProject(userDtoNew)).willReturn(userDtoNew.apply { id = 2 })

        val userDtoSameToken = FcmUserDto().apply {
            subjectId = "$USER_ID-new-user"
            fcmToken = FCM_TOKEN_2
            projectId = PROJECT_ID
            enrolmentDate = this@RadarUserControllerTest.enrolmentDate
            language = "en"
            timezone = TIMEZONE
            id = 5L
        }

        given(userService.saveUserInProject(userDtoSameToken)).willThrow(InvalidUserDetailsException("User already exists"))

        val userUpdated = FcmUserDto().apply {
            subjectId = "$USER_ID-updated"
            fcmToken = FCM_TOKEN_3
            projectId = PROJECT_ID
            enrolmentDate = this@RadarUserControllerTest.enrolmentDate
            language = "da"
            timezone = TIMEZONE
            id = 1L
        }

        given(userService.updateUser(userUpdated)).willReturn(userUpdated)
    }

    @Test
    fun addUserToProject() {
        val userDtoNew = FcmUserDto().apply {
            subjectId = "$USER_ID-new"
            fcmToken = FCM_TOKEN_2
            enrolmentDate = this@RadarUserControllerTest.enrolmentDate
            language = "en"
            timezone = TIMEZONE
            id = 2L
        }

        mockMvc.perform(
            MockMvcRequestBuilders.post(URI("/projects/test-project/users"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDtoNew)),
        )
            .andExpect { status().isCreated }
            .andExpect { jsonPath(FCM_TOKEN_JSON_PATH).value(FCM_TOKEN_2) }
            .andExpect { jsonPath(LANGUAGE_JSON_PATH).value("en") }
            .andExpect { jsonPath(ENROLMENT_DATE_JSON_PATH).value(enrolmentDate.toString()) }
            .andExpect { jsonPath(ID_JSON_PATH).value(2) }

        val userDtoSameToken = FcmUserDto().apply {
            subjectId = "$USER_ID-new-user"
            fcmToken = FCM_TOKEN_2
            enrolmentDate = this@RadarUserControllerTest.enrolmentDate
            language = "en"
            timezone = TIMEZONE
            id = 5L
        }

        mockMvc.perform(
            MockMvcRequestBuilders.post(URI("/projects/test-project/users"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDtoSameToken)),
        ).andExpect(status().isExpectationFailed)
    }

    @Test
    fun updateUserInProject() {
        val userDtoUpdated = FcmUserDto().apply {
            subjectId = "test-user-updated"
            fcmToken = FCM_TOKEN_3
            enrolmentDate = this@RadarUserControllerTest.enrolmentDate
            language = "da"
            timezone = TIMEZONE
            id = 1L
        }

        mockMvc.perform(
            MockMvcRequestBuilders.put(URI("/projects/test-project/users/test-user-updated"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDtoUpdated)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath(FCM_TOKEN_JSON_PATH).value(FCM_TOKEN_3))
            .andExpect(jsonPath(LANGUAGE_JSON_PATH).value("da"))
            .andExpect(jsonPath(ENROLMENT_DATE_JSON_PATH).value(enrolmentDate.toString()))
            .andExpect(jsonPath(ID_JSON_PATH).value(1))
    }

    @Test
    fun getAllRadarUsers() {
        mockMvc.perform(MockMvcRequestBuilders.get(URI("/users")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.users[0].fcmToken").value(FCM_TOKEN_1))
            .andExpect(jsonPath("$.users[0].language").value("es"))
            .andExpect(jsonPath("$.users[0].enrolmentDate").value(enrolmentDate.toString()))
    }

    @Test
    fun getRadarUserUsingId() {
        mockMvc.perform(MockMvcRequestBuilders.get(URI("/users/user?id=1")))
            .andExpect(status().isOk())
            .andExpect(jsonPath(FCM_TOKEN_JSON_PATH).value(FCM_TOKEN_1))
            .andExpect(jsonPath(LANGUAGE_JSON_PATH).value("es"))
            .andExpect(jsonPath(ENROLMENT_DATE_JSON_PATH).value(enrolmentDate.toString()))
    }

    @Test
    fun getRadarUserUsingSubjectId() {
        mockMvc
            .perform(MockMvcRequestBuilders.get(URI("/users/test-user")))
            .andExpect(status().isOk())
            .andExpect(jsonPath(FCM_TOKEN_JSON_PATH).value(FCM_TOKEN_1))
            .andExpect(jsonPath(LANGUAGE_JSON_PATH).value("es"))
            .andExpect(jsonPath(ENROLMENT_DATE_JSON_PATH).value(enrolmentDate.toString()))
    }

    @Test
    fun getUsersUsingProjectId() {
        mockMvc
            .perform(MockMvcRequestBuilders.get(URI("/projects/test-project/users")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.users[0].fcmToken").value(FCM_TOKEN_1))
            .andExpect(jsonPath("$.users[0].language").value("es"))
            .andExpect(jsonPath("$.users[0].enrolmentDate").value(enrolmentDate.toString()))
    }
}
