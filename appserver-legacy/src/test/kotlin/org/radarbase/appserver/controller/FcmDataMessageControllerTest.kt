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

package org.radarbase.appserver.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.doAnswer
import org.radarbase.appserver.dto.fcm.FcmDataMessageDto
import org.radarbase.appserver.dto.fcm.FcmDataMessages
import org.radarbase.appserver.service.FcmDataMessageService
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
@WebMvcTest(FcmDataMessageController::class)
@AutoConfigureMockMvc(addFilters = false)
class FcmDataMessageControllerTest {

    companion object {
        const val FCM_MESSAGE_ID = "123456"
        const val PROJECT_ID = "test-project"
        const val USER_ID = "test-user"
        private const val SOURCE_TYPE = "aRMT"
        private const val SOURCE_ID = "test"
        private const val DATA_MESSAGES_JSON_PATH = "\$.dataMessages"
        private const val DATA_MESSAGE_FCMID_JSON_PATH = "\$.dataMessages[0].fcmMessageId"
    }

    private val scheduledTime: Instant = Instant.now().plus(Duration.ofSeconds(100))

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var dataMessageService: FcmDataMessageService

    @BeforeEach
    fun setUp() {
        val dataMessageDto = FcmDataMessageDto().apply {
            scheduledTime = this@FcmDataMessageControllerTest.scheduledTime
            sourceId = SOURCE_ID
            fcmMessageId = FCM_MESSAGE_ID
            ttlSeconds = 86400
            delivered = false
            id = 1L
        }

        given(dataMessageService.getAllDataMessages())
            .willReturn(FcmDataMessages().apply { withDataMessages(listOf(dataMessageDto)) })

        given(dataMessageService.getDataMessageById(1L)).willReturn(dataMessageDto)

        given(dataMessageService.getDataMessagesByProjectIdAndSubjectId(PROJECT_ID, USER_ID))
            .willReturn(FcmDataMessages().apply { withDataMessages(listOf(dataMessageDto)) })

        given(dataMessageService.getDataMessagesByProjectId(PROJECT_ID))
            .willReturn(FcmDataMessages().apply { withDataMessages(listOf(dataMessageDto)) })

        given(dataMessageService.getDataMessagesBySubjectId(USER_ID))
            .willReturn(FcmDataMessages().apply { withDataMessages(listOf(dataMessageDto)) })

        val dataMessageDto2 = FcmDataMessageDto().apply {
            scheduledTime = this@FcmDataMessageControllerTest.scheduledTime
            sourceId = SOURCE_ID
            fcmMessageId = FCM_MESSAGE_ID + "7"
            sourceType = SOURCE_TYPE
            appPackage = SOURCE_TYPE
            ttlSeconds = 86400
            delivered = false
            id = 2L
        }

        given(dataMessageService.addDataMessage(dataMessageDto2, USER_ID, PROJECT_ID))
            .willReturn(dataMessageDto2)

        val dataMessageDto3 = FcmDataMessageDto().apply {
            scheduledTime = this@FcmDataMessageControllerTest.scheduledTime
            sourceId = SOURCE_ID
            fcmMessageId = FCM_MESSAGE_ID + "7"
            sourceType = SOURCE_TYPE
            appPackage = SOURCE_TYPE
            ttlSeconds = 86400
            delivered = false
        }

        val fcmDataMessages = FcmDataMessages().apply {
            withDataMessages(listOf(dataMessageDto2, dataMessageDto3))
        }

        given(dataMessageService.addDataMessages(fcmDataMessages, USER_ID, PROJECT_ID))
            .willReturn(fcmDataMessages)

        given(dataMessageService.getDataMessageById(2L)).willReturn(dataMessageDto2)

        doAnswer {
            val projectId = it.getArgument<String>(0)
            val subjectId = it.getArgument<String>(1)

            assertEquals(PROJECT_ID, projectId)
            assertEquals(USER_ID, subjectId)
            null
        }.`when`(dataMessageService).removeDataMessagesForUser(any(), any())
    }

    @Test
    fun getAllDataMessages() {
        mockMvc.perform(
            MockMvcRequestBuilders.get(URI.create("/${PathsUtil.MESSAGING_DATA_PATH}")),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath(DATA_MESSAGES_JSON_PATH, hasSize<Any>(1)))
            .andExpect(jsonPath(DATA_MESSAGE_FCMID_JSON_PATH, `is`(FCM_MESSAGE_ID)))
    }

    @Test
    fun getDataMessageUsingId() {
        mockMvc.perform(
            MockMvcRequestBuilders.get(URI.create("/${PathsUtil.MESSAGING_DATA_PATH}/1")),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.fcmMessageId", `is`(FCM_MESSAGE_ID)))
    }

    @Test
    @Disabled("Not implemented yet")
    fun getFilteredDataMessages() {
        // TODO
    }

    @Test
    fun getDataMessagesUsingProjectIdAndSubjectId() {
        mockMvc.perform(
            MockMvcRequestBuilders.get(
                URI.create(
                    "/${PathsUtil.PROJECT_PATH}/$PROJECT_ID/${PathsUtil.USER_PATH}/$USER_ID/${PathsUtil.MESSAGING_DATA_PATH}",
                ),
            ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath(DATA_MESSAGES_JSON_PATH, hasSize<Any>(1)))
            .andExpect(jsonPath(DATA_MESSAGE_FCMID_JSON_PATH, `is`(FCM_MESSAGE_ID)))
    }

    @Test
    fun getDataMessagesUsingProjectId() {
        mockMvc.perform(
            MockMvcRequestBuilders.get(
                URI.create(
                    "/${PathsUtil.PROJECT_PATH}/$PROJECT_ID/${PathsUtil.MESSAGING_DATA_PATH}",
                ),
            ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath(DATA_MESSAGES_JSON_PATH, hasSize<Any>(1)))
            .andExpect(jsonPath(DATA_MESSAGE_FCMID_JSON_PATH, `is`(FCM_MESSAGE_ID)))
    }

    @Test
    fun addSingleDataMessage() {
        val dataMessageDto2 = FcmDataMessageDto().apply {
            scheduledTime = this@FcmDataMessageControllerTest.scheduledTime
            sourceId = SOURCE_ID
            fcmMessageId = FCM_MESSAGE_ID + "7"
            sourceType = SOURCE_TYPE
            appPackage = SOURCE_TYPE
            ttlSeconds = 86400
            delivered = false
        }

        mockMvc.perform(
            MockMvcRequestBuilders.post(
                URI.create(
                    "/${PathsUtil.PROJECT_PATH}/$PROJECT_ID/${PathsUtil.USER_PATH}/$USER_ID/${PathsUtil.MESSAGING_DATA_PATH}",
                ),
            )
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(dataMessageDto2)),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.fcmMessageId", `is`(FCM_MESSAGE_ID + "7")))
            .andExpect(jsonPath("$.id", `is`(2)))
    }

    @Test
    fun addBatchDataMessages() {
        val dataMessageDto2 = FcmDataMessageDto().apply {
            scheduledTime = this@FcmDataMessageControllerTest.scheduledTime
            sourceId = SOURCE_ID
            fcmMessageId = FCM_MESSAGE_ID + "7"
            sourceType = SOURCE_TYPE
            appPackage = SOURCE_TYPE
            ttlSeconds = 86400
            delivered = false
        }

        val dataMessageDto3 = FcmDataMessageDto().apply {
            scheduledTime = this@FcmDataMessageControllerTest.scheduledTime
            sourceId = SOURCE_ID
            fcmMessageId = FCM_MESSAGE_ID + "7"
            sourceType = SOURCE_TYPE
            appPackage = SOURCE_TYPE
            ttlSeconds = 86400
            delivered = false
        }

        val fcmDataMessages = FcmDataMessages().apply {
            withDataMessages(listOf(dataMessageDto2, dataMessageDto3))
        }

        mockMvc.perform(
            MockMvcRequestBuilders.post(
                URI.create(
                    "/${PathsUtil.PROJECT_PATH}/$PROJECT_ID/${PathsUtil.USER_PATH}/$USER_ID/${PathsUtil.MESSAGING_DATA_PATH}/batch",
                ),
            )
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(fcmDataMessages)),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath(DATA_MESSAGES_JSON_PATH, hasSize<Any>(2)))
            .andExpect(jsonPath(DATA_MESSAGE_FCMID_JSON_PATH, `is`(FCM_MESSAGE_ID + "7")))
    }

    @Test
    fun deleteDataMessagesForUser() {
        mockMvc.perform(
            MockMvcRequestBuilders.delete(
                URI.create(
                    "/${PathsUtil.PROJECT_PATH}/$PROJECT_ID/${PathsUtil.USER_PATH}/$USER_ID/${PathsUtil.MESSAGING_DATA_PATH}",
                ),
            ),
        )
    }
}
