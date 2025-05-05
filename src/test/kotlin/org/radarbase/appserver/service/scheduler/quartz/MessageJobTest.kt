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

package org.radarbase.appserver.service.scheduler.quartz

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.junit.jupiter.api.Assertions.*
import org.quartz.JobDataMap
import org.quartz.JobExecutionException
import org.quartz.impl.JobDetailImpl
import org.quartz.impl.JobExecutionContextImpl
import org.radarbase.appserver.entity.DataMessage
import org.radarbase.appserver.entity.Notification
import org.radarbase.appserver.exception.EmailMessageTransmitException
import org.radarbase.appserver.exception.FcmMessageTransmitException
import org.radarbase.appserver.service.FcmDataMessageService
import org.radarbase.appserver.service.FcmNotificationService
import org.radarbase.appserver.service.MessageType
import org.radarbase.appserver.service.transmitter.DataMessageTransmitter
import org.radarbase.appserver.service.transmitter.NotificationTransmitter
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(classes = [MessageJob::class])
@ExtendWith(SpringExtension::class)
class MessageJobTest {

    private lateinit var messageJob: MessageJob

    @MockBean
    private lateinit var fcmDataMessageService: FcmDataMessageService

    @MockBean
    private lateinit var fcmNotificationService: FcmNotificationService

    @MockBean
    private lateinit var notificationTransmitter: NotificationTransmitter

    @MockBean
    private lateinit var dataMessageTransmitter: DataMessageTransmitter

    private lateinit var jobDataMap: JobDataMap
    private lateinit var jobExecutionContext: JobExecutionContextImpl
    private lateinit var notification: Notification
    private lateinit var dataMessage: DataMessage

    @BeforeEach
    fun setUp() {
        val notificationTransmitters = listOf(notificationTransmitter, notificationTransmitter)
        val dataMessageTransmitters = listOf(dataMessageTransmitter, dataMessageTransmitter)

        messageJob = MessageJob(notificationTransmitters, dataMessageTransmitters, fcmNotificationService, fcmDataMessageService)

        jobExecutionContext = mock(JobExecutionContextImpl::class.java)
        val job = mock(JobDetailImpl::class.java)
        jobDataMap = mock(JobDataMap::class.java)

        `when`(job.jobDataMap).thenReturn(jobDataMap)
        `when`(jobDataMap.getString("projectId")).thenReturn("projectId")
        `when`(jobDataMap.getString("subjectId")).thenReturn("subjectId")
        `when`(jobDataMap.getLong("messageId")).thenReturn(1L)

        setNotificationType(MessageType.NOTIFICATION)

        `when`(jobExecutionContext.jobDetail).thenReturn(job)

        notification = mock(Notification::class.java)
        dataMessage = mock(DataMessage::class.java)

        `when`(fcmNotificationService.getNotificationByProjectIdAndSubjectIdAndNotificationId("projectId", "subjectId", 1L)).thenReturn(notification)
        `when`(fcmDataMessageService.getDataMessageByProjectIdAndSubjectIdAndDataMessageId("projectId", "subjectId", 1L)).thenReturn(dataMessage)
    }

    @Test
    fun `execute notification should send notification twice`() {
        setNotificationType(MessageType.NOTIFICATION)
        messageJob.execute(jobExecutionContext)
        verify(notificationTransmitter, times(2)).send(notification)
        verify(dataMessageTransmitter, never()).send(any<DataMessage>())
    }

    @Test
    fun `execute data message should send data message twice`() {
        setNotificationType(MessageType.DATA)
        messageJob.execute(jobExecutionContext)
        verify(notificationTransmitter, never()).send(any<Notification>())
        verify(dataMessageTransmitter, times(2)).send(dataMessage)
    }

    @Test
    fun `should be silent on EmailMessageTransmitException`() {
        setNotificationType(MessageType.NOTIFICATION)
        doThrow(EmailMessageTransmitException("Email exception"), EmailMessageTransmitException("Email exception"))
            .`when`(notificationTransmitter).send(notification)

        assertDoesNotThrow { messageJob.execute(jobExecutionContext) }
    }

    @Test
    fun `should throw JobExecutionException on FcmMessageTransmitException`() {
        setNotificationType(MessageType.NOTIFICATION)
        doThrow(EmailMessageTransmitException("Email exception"), FcmMessageTransmitException("Fcm exception"))
            .`when`(notificationTransmitter).send(notification)

        assertThrows(JobExecutionException::class.java) { messageJob.execute(jobExecutionContext) }
    }

    private fun setNotificationType(messageType: MessageType) {
        `when`(jobDataMap.getString("messageType")).thenReturn(messageType.name)
    }
}
