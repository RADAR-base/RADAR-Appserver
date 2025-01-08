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

package org.radarbase.appserver.service.scheduler

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.quartz.*
import org.radarbase.appserver.config.SchedulerConfig
import org.radarbase.appserver.entity.Notification
import org.radarbase.appserver.entity.Project
import org.radarbase.appserver.entity.User
import org.radarbase.appserver.service.MessageType
import org.radarbase.appserver.service.scheduler.quartz.SchedulerServiceImpl
import org.radarbase.fcm.downstream.FcmSender
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.scheduling.quartz.JobDetailFactoryBean
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.NONE,
    classes = [
        DataSourceAutoConfiguration::class,
        QuartzAutoConfiguration::class,
        SchedulerConfig::class,
        NotificationSchedulerServiceTest.SchedulerServiceTestConfig::class
    ]
)
class NotificationSchedulerServiceTest {

    companion object {
        private const val TIMEZONE = "Europe/London"
        private const val JOB_DETAIL_ID = "message-jobdetail-test-subject-1"
        private lateinit var notification: Notification
    }

    @Autowired
    private lateinit var schedulerService: MessageSchedulerService<Notification>

    @Autowired
    private lateinit var scheduler: Scheduler

    @BeforeEach
    fun setUp() {
        scheduler.clear()
        val user = User(1L, "test-subject", null, "xxxx", Project(), Instant.now(), null, TIMEZONE, "en", null)
        notification = Notification.NotificationBuilder()
            .delivered(false)
            .ttlSeconds(900)
            .sourceId("aRMT")
            .scheduledTime(Instant.now().plus(Duration.ofSeconds(3)))
            .fcmMessageId("xxxx")
            .title("Testing")
            .body("Testing")
            .user(user)
            .type("ESM")
            .appPackage("aRMT")
            .id(1L)
            .build()
    }

    @Test
    fun `schedule notification`() {
        scheduler.listenerManager.addJobListener(TestJobListener())
        schedulerService.schedule(notification)
        Thread.sleep(5000)
    }

    @Test
    fun `schedule multiple notifications`() {
        scheduler.listenerManager.addJobListener(TestJobListener())
        schedulerService.scheduleMultiple(listOf(notification))
        Thread.sleep(5000)
    }

    @Test
    fun `update scheduled notification`() {
        val jobDetail = MessageSchedulerService.getJobDetailForMessage(notification, MessageType.NOTIFICATION)
        val triggerFactoryBean = MessageSchedulerService.getTriggerForMessage(notification, jobDetail.`object`)
        scheduler.scheduleJob(jobDetail.`object`, triggerFactoryBean.`object`)

        val updatedNotification = Notification.NotificationBuilder(notification)
            .fcmMessageId("yyyy")
            .body("New body")
            .title("New Title")
            .scheduledTime(Instant.now().plus(Duration.ofSeconds(100)))
            .build()

        schedulerService.updateScheduled(updatedNotification)
        assertTrue(scheduler.checkExists(JobKey(JOB_DETAIL_ID)))
        assertEquals(
            updatedNotification.scheduledTime?.truncatedTo(ChronoUnit.MILLIS),
            scheduler.getTrigger(TriggerKey("message-trigger-test-subject-1")).startTime.toInstant()
        )
    }

    @Test
    fun `delete scheduled notification`() {
        val jobDetail: JobDetailFactoryBean? = MessageSchedulerService.getJobDetailForMessage(notification, MessageType.NOTIFICATION)
        val triggerFactoryBean = MessageSchedulerService.getTriggerForMessage(notification, jobDetail!!.`object`!!)
        scheduler.scheduleJob(jobDetail.`object`, triggerFactoryBean.`object`)

        schedulerService.deleteScheduled(notification)
        assertFalse(scheduler.checkExists(JobKey(JOB_DETAIL_ID)))
    }

    @TestConfiguration
    class SchedulerServiceTestConfig {
        @Autowired
        private lateinit var scheduler: Scheduler

        @Bean
        @Primary
        fun schedulerServiceBeanConfig(): MessageSchedulerService<Notification> {
            return MessageSchedulerService(mock(FcmSender::class.java), SchedulerServiceImpl(scheduler))
        }
    }

    private class TestJobListener : JobListener {

        override fun getName() = "test-job-listener"

        override fun jobToBeExecuted(context: JobExecutionContext) {
            assertEquals(notification, context.jobDetail.jobDataMap["notification"])
        }

        override fun jobExecutionVetoed(context: JobExecutionContext) {
            fail<Any?>("The Job Execution was vetoed.")
        }

        override fun jobWasExecuted(context: JobExecutionContext, jobException: JobExecutionException?) {
            assertEquals(notification, context.jobDetail.jobDataMap["notification"])
            assertNull(jobException)
        }
    }
}



