/*
 * Copyright 2018 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.appserver.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito.given
import org.mockito.Mockito.doAnswer
import org.radarbase.appserver.dto.fcm.FcmNotificationDto
import org.radarbase.appserver.dto.fcm.FcmNotifications
import org.radarbase.appserver.service.FcmNotificationService
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
@WebMvcTest(FcmNotificationController::class)
@AutoConfigureMockMvc(addFilters = false)
class FcmNotificationControllerTest {

    private val scheduledTime: Instant = Instant.now().plus(Duration.ofSeconds(100))

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @MockBean
    private lateinit var notificationService: FcmNotificationService

    @BeforeEach
    fun setUp() {
        val notificationDto = FcmNotificationDto().apply {
            body = BODY
            title = TITLE_1
            scheduledTime = this@FcmNotificationControllerTest.scheduledTime
            sourceId = SOURCE_ID
            fcmMessageId = FCM_MESSAGE_ID
            ttlSeconds = 86400
            delivered = false
            id = 1L
        }

        given(notificationService.getAllNotifications())
            .willReturn(FcmNotifications().withNotifications(listOf(notificationDto)))

        given(notificationService.getNotificationById(1L))
            .willReturn(notificationDto)

        given(notificationService.getNotificationsByProjectIdAndSubjectId(PROJECT_ID, USER_ID))
            .willReturn(FcmNotifications().withNotifications(listOf(notificationDto)))

        given(notificationService.getNotificationsByProjectId(PROJECT_ID))
            .willReturn(FcmNotifications().withNotifications(listOf(notificationDto)))

        given(notificationService.getNotificationsBySubjectId(USER_ID))
            .willReturn(FcmNotifications().withNotifications(listOf(notificationDto)))

        val notificationDto2 = FcmNotificationDto().apply {
            body = BODY
            title = TITLE_2
            scheduledTime = this@FcmNotificationControllerTest.scheduledTime
            sourceId = SOURCE_ID
            fcmMessageId = FCM_MESSAGE_ID + 7
            sourceType = SOURCE_TYPE
            type = TYPE
            appPackage = SOURCE_TYPE
            ttlSeconds = 86400
            delivered = false
            id = 2L
        }

        given(notificationService.addNotification(eq(notificationDto2), eq(USER_ID), eq(PROJECT_ID), eq(SCHEDULE_TRUE)))
            .willReturn(notificationDto2)

        given(
            notificationService.addNotification(
                eq(notificationDto2),
                eq(USER_ID),
                eq(PROJECT_ID),
                eq(SCHEDULE_FALSE)
            )
        )
            .willReturn(notificationDto2)

        given(notificationService.scheduleNotification(eq(USER_ID), eq(PROJECT_ID), eq(2L)))
            .willReturn(notificationDto2)

        val notificationDto3 = FcmNotificationDto().apply {
            body = "Test notif 3"
            title = "Testing 3"
            scheduledTime = this@FcmNotificationControllerTest.scheduledTime
            sourceId = SOURCE_ID
            fcmMessageId = FCM_MESSAGE_ID + 7
            sourceType = SOURCE_TYPE
            type = TYPE
            appPackage = SOURCE_TYPE
            ttlSeconds = 86400
            delivered = false
        }

        val fcmNotifications = FcmNotifications().withNotifications(listOf(notificationDto2, notificationDto3))

        given(
            notificationService.addNotifications(
                eq(fcmNotifications),
                eq(USER_ID),
                eq(PROJECT_ID),
                eq(SCHEDULE_TRUE)
            )
        )
            .willReturn(fcmNotifications)

        given(
            notificationService.addNotifications(
                eq(fcmNotifications),
                eq(USER_ID),
                eq(PROJECT_ID),
                eq(SCHEDULE_FALSE)
            )
        )
            .willReturn(fcmNotifications)

        given(notificationService.scheduleAllUserNotifications(eq(USER_ID), eq(PROJECT_ID)))
            .willReturn(fcmNotifications)

        given(notificationService.getNotificationById(2L)).willReturn(notificationDto2)

        doAnswer {
            val projectId = it.arguments[0] as String
            val subjectId = it.arguments[1] as String
            assertEquals(PROJECT_ID, projectId)
            assertEquals(USER_ID, subjectId)
            null
        }.`when`(notificationService).removeNotificationsForUser(any(String::class.java), any(String::class.java))
    }


    @Test
    fun getAllNotifications() {
        mockMvc.perform(MockMvcRequestBuilders.get(URI.create("/${PathsUtil.MESSAGING_NOTIFICATION_PATH}")))
            .andExpect { status().isOk }
            .andExpect { jsonPath(NOTIFICATIONS_JSON_PATH).value { hasSize<Any>(2) } }
            .andExpect(jsonPath(NOTIFICATION_TITLE_JSON_PATH).value(TITLE_1))
            .andExpect(jsonPath(NOTIFICATION_FCMID_JSON_PATH).value(FCM_MESSAGE_ID))
    }

    @Test
    fun getNotificationsUsingId() {
        mockMvc.perform(MockMvcRequestBuilders.get(URI.create("/${PathsUtil.MESSAGING_NOTIFICATION_PATH}/1")))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value(TITLE_1))
            .andExpect(jsonPath("$.fcmMessageId").value(FCM_MESSAGE_ID))
    }

    @Test
    @Disabled("Not implemented yet")
    fun getFilteredNotifications() {
        // TODO
    }

    @Test
    fun getNotificationUsingProjectIdAndSubjectId() {
        mockMvc.perform(
            MockMvcRequestBuilders.get(
                URI.create(
                    "/${PathsUtil.PROJECT_PATH}/$PROJECT_ID/${PathsUtil.USER_PATH}/$USER_ID/${PathsUtil.MESSAGING_NOTIFICATION_PATH}"
                )
            )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath(NOTIFICATIONS_JSON_PATH).value(hasSize<Any>(1)))
            .andExpect(jsonPath(NOTIFICATION_TITLE_JSON_PATH).value(TITLE_1))
            .andExpect(jsonPath(NOTIFICATION_FCMID_JSON_PATH).value(FCM_MESSAGE_ID))
    }

    @Test
    fun getNotificationsUsingProjectId() {
        mockMvc.perform(
            MockMvcRequestBuilders.get(
                URI.create("/${PathsUtil.PROJECT_PATH}/$PROJECT_ID/${PathsUtil.MESSAGING_NOTIFICATION_PATH}")
            )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath(NOTIFICATIONS_JSON_PATH).value(hasSize<Any>(1)))
            .andExpect(jsonPath(NOTIFICATION_TITLE_JSON_PATH).value(TITLE_1))
            .andExpect(jsonPath(NOTIFICATION_FCMID_JSON_PATH).value(FCM_MESSAGE_ID))
    }

    @Test
    fun addSingleNotification() {
        val notificationDto2 = FcmNotificationDto().apply {
            body = BODY
            title = TITLE_2
            scheduledTime = this@FcmNotificationControllerTest.scheduledTime
            sourceId = SOURCE_ID
            fcmMessageId = FCM_MESSAGE_ID + 7
            sourceType = SOURCE_TYPE
            type = TYPE
            appPackage = SOURCE_TYPE
            ttlSeconds = 86400
            delivered = false
        }

        mockMvc.perform(
            MockMvcRequestBuilders.post(
                URI.create(
                    "/${PathsUtil.PROJECT_PATH}/$PROJECT_ID/${PathsUtil.USER_PATH}/$USER_ID/${PathsUtil.MESSAGING_NOTIFICATION_PATH}"
                )
            )
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(notificationDto2))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.title").value(TITLE_2))
            .andExpect(jsonPath("$.fcmMessageId").value(FCM_MESSAGE_ID + 7))
            .andExpect(jsonPath("$.id").value(2))
    }

    @Test
    fun addSingleNotificationWithoutScheduling() {
        val notificationDto2 = FcmNotificationDto().apply {
            body = BODY
            title = TITLE_2
            scheduledTime = this@FcmNotificationControllerTest.scheduledTime
            sourceId = SOURCE_ID
            fcmMessageId = FCM_MESSAGE_ID + 7
            sourceType = SOURCE_TYPE
            type = TYPE
            appPackage = SOURCE_TYPE
            ttlSeconds = 86400
            delivered = false
        }

        mockMvc.perform(
            MockMvcRequestBuilders.post(
                URI.create(
                    "/${PathsUtil.PROJECT_PATH}/$PROJECT_ID/${PathsUtil.USER_PATH}/$USER_ID/${PathsUtil.MESSAGING_NOTIFICATION_PATH}?schedule=false"
                )
            )
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(notificationDto2))
        )
            .andExpect { status().isCreated }
            .andExpect { jsonPath("$.title").value(TITLE_2) }
            .andExpect { jsonPath("$.fcmMessageId").value(FCM_MESSAGE_ID + 7) }
            .andExpect { jsonPath("$.id").value(2) }
    }

    @Test
    fun scheduleUnscheduledNotification() {
        mockMvc.perform(
            MockMvcRequestBuilders.post(
                URI.create(
                    "/${PathsUtil.PROJECT_PATH}/$PROJECT_ID/${PathsUtil.USER_PATH}/$USER_ID/${PathsUtil.MESSAGING_NOTIFICATION_PATH}/2/schedule"
                )
            )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.title").value(TITLE_2))
            .andExpect(jsonPath("$.fcmMessageId").value(FCM_MESSAGE_ID + 7))
            .andExpect(jsonPath("$.id").value(2))
    }

    @Test
    fun addBatchNotifications() {
        val notificationDto2 = FcmNotificationDto().apply {
            body = BODY
            title = TITLE_2
            scheduledTime = this@FcmNotificationControllerTest.scheduledTime
            sourceId = SOURCE_ID
            fcmMessageId = FCM_MESSAGE_ID + "7"
            sourceType = SOURCE_TYPE
            type = TYPE
            appPackage = SOURCE_TYPE
            ttlSeconds = 86400
            delivered = false
        }

        val notificationDto3 = FcmNotificationDto().apply {
            body = "Test notif 3"
            title = "Testing 3"
            scheduledTime = this@FcmNotificationControllerTest.scheduledTime
            sourceId = SOURCE_ID
            fcmMessageId = FCM_MESSAGE_ID + "7"
            sourceType = SOURCE_TYPE
            type = TYPE
            appPackage = SOURCE_TYPE
            ttlSeconds = 86400
            delivered = false
        }

        val fcmNotifications = FcmNotifications().apply {
            withNotifications(listOf(notificationDto2, notificationDto3))
        }

        mockMvc.perform(
            MockMvcRequestBuilders.post(
                URI.create(
                    "/${PathsUtil.PROJECT_PATH}/$PROJECT_ID/${PathsUtil.USER_PATH}/$USER_ID/${PathsUtil.MESSAGING_NOTIFICATION_PATH}/batch"
                )
            )
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(fcmNotifications))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath(NOTIFICATIONS_JSON_PATH).value(hasSize<Any>(2)))
            .andExpect(jsonPath(NOTIFICATION_TITLE_JSON_PATH).value(TITLE_2))
            .andExpect(jsonPath(NOTIFICATION_FCMID_JSON_PATH).value(FCM_MESSAGE_ID + "7"))
    }

    @Test
    fun addBatchNotificationsWithoutScheduling() {
        val notificationDto2 = FcmNotificationDto().apply {
            body = BODY
            title = TITLE_2
            scheduledTime = this@FcmNotificationControllerTest.scheduledTime
            sourceId = SOURCE_ID
            fcmMessageId = FCM_MESSAGE_ID + "7"
            sourceType = SOURCE_TYPE
            type = TYPE
            appPackage = SOURCE_TYPE
            ttlSeconds = 86400
            delivered = false
        }

        val notificationDto3 = FcmNotificationDto().apply {
            body = "Test notif 3"
            title = "Testing 3"
            scheduledTime = this@FcmNotificationControllerTest.scheduledTime
            sourceId = SOURCE_ID
            fcmMessageId = FCM_MESSAGE_ID + "7"
            sourceType = SOURCE_TYPE
            type = TYPE
            appPackage = SOURCE_TYPE
            ttlSeconds = 86400
            delivered = false
        }

        val fcmNotifications = FcmNotifications().apply {
            withNotifications(listOf(notificationDto2, notificationDto3))
        }

        mockMvc.perform(
            MockMvcRequestBuilders.post(
                URI.create(
                    "/${PathsUtil.PROJECT_PATH}/$PROJECT_ID/${PathsUtil.USER_PATH}/$USER_ID/${PathsUtil.MESSAGING_NOTIFICATION_PATH}/batch?schedule=false"
                )
            )
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(fcmNotifications))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath(NOTIFICATIONS_JSON_PATH).value(hasSize<Any>(2)))
            .andExpect(jsonPath(NOTIFICATION_TITLE_JSON_PATH).value(TITLE_2))
            .andExpect(jsonPath(NOTIFICATION_FCMID_JSON_PATH).value(FCM_MESSAGE_ID + "7"))
    }

    @Test
    fun scheduleUnscheduledNotifications() {
        mockMvc.perform(
            MockMvcRequestBuilders.post(
                URI.create(
                    "/${PathsUtil.PROJECT_PATH}/$PROJECT_ID/${PathsUtil.USER_PATH}/$USER_ID/${PathsUtil.MESSAGING_NOTIFICATION_PATH}/schedule"
                )
            )
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath(NOTIFICATIONS_JSON_PATH).value(hasSize<Any>(2)))
            .andExpect(jsonPath(NOTIFICATION_TITLE_JSON_PATH).value(TITLE_2))
            .andExpect(jsonPath(NOTIFICATION_FCMID_JSON_PATH).value(FCM_MESSAGE_ID + "7"))
    }

    @Test
    fun deleteNotificationsForUser() {
        mockMvc.perform(
            MockMvcRequestBuilders.delete(
                URI.create(
                    "/${PathsUtil.PROJECT_PATH}/$PROJECT_ID/${PathsUtil.USER_PATH}/$USER_ID/${PathsUtil.MESSAGING_NOTIFICATION_PATH}"
                )
            )
        )
    }


    companion object {
        const val FCM_MESSAGE_ID = "123456"
        const val PROJECT_ID = "test-project"
        const val USER_ID = "test-user"
        const val SCHEDULE_TRUE = true
        const val SCHEDULE_FALSE = false
        const val TITLE_1 = "Testing 1"
        const val TITLE_2 = "Testing 2"
        const val BODY = "Test notif"
        const val SOURCE_TYPE = "aRMT"
        const val SOURCE_ID = "test"
        const val TYPE = "ESM"
        const val NOTIFICATIONS_JSON_PATH = "$.notifications"
        const val NOTIFICATION_TITLE_JSON_PATH = "$.notifications[0].title"
        const val NOTIFICATION_FCMID_JSON_PATH = "$.notifications[0].fcmMessageId"
    }
}