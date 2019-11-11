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

package org.radarbase.appserver.service.scheduler;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.radarbase.appserver.config.SchedulerConfig;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.entity.Project;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.service.MessageType;
import org.radarbase.appserver.service.scheduler.NotificationSchedulerServiceTest.SchedulerServiceTestConfig;
import org.radarbase.appserver.service.scheduler.quartz.SchedulerServiceImpl;
import org.radarbase.fcm.downstream.FcmSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.quartz.QuartzAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = NONE,
        classes = {
                DataSourceAutoConfiguration.class,
                QuartzAutoConfiguration.class,
                SchedulerConfig.class,
                SchedulerServiceTestConfig.class
        })
class NotificationSchedulerServiceTest {

    private static final String JOB_DETAIL_ID = "message-jobdetail-test-subject-1";
    private static Notification notification;
    @Autowired
    private transient NotificationSchedulerService notificationSchedulerService;
    @Autowired
    private transient Scheduler scheduler;

    @BeforeEach
    @SuppressFBWarnings("ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
    void setUp() throws SchedulerException {
        scheduler.clear();
        User user =
                new User()
                        .setId(1L)
                        .setSubjectId("test-subject")
                        .setLanguage("en")
                        .setTimezone(0d)
                        .setProject(new Project())
                        .setEnrolmentDate(Instant.now())
                        .setFcmToken("xxxx");

        notification =
                new Notification.NotificationBuilder()
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
                        .build();
    }

    @Test
    void sendNotification() {
        assertDoesNotThrow(() -> notificationSchedulerService.sendNotification(notification));
    }

    @Test
    void scheduleNotification() throws InterruptedException, SchedulerException {
        scheduler.getListenerManager().addJobListener(new TestJobListener());
        notificationSchedulerService.scheduleNotification(notification);

        // sleep for 5 seconds for the job to be executed.
        // Assert statements are in the listener.
        Thread.sleep(5000);
    }

    @Test
    void scheduleNotifications() throws InterruptedException, SchedulerException {
        scheduler.getListenerManager().addJobListener(new TestJobListener());
        notificationSchedulerService.scheduleNotifications(List.of(notification));

        Thread.sleep(5000);
    }

    @Test
    void updateScheduledNotification() throws SchedulerException {
        // given
        JobDetailFactoryBean jobDetail =
                NotificationSchedulerService.getJobDetailForMessage(notification, MessageType.Notification);
        SimpleTriggerFactoryBean triggerFactoryBean =
                NotificationSchedulerService.getTriggerForMessage(notification, jobDetail.getObject());
        scheduler.scheduleJob(jobDetail.getObject(), triggerFactoryBean.getObject());

        Notification notification2 =
                new Notification.NotificationBuilder(notification)
                        .fcmMessageId("yyyy")
                        .body("New body")
                        .title("New Title")
                        .scheduledTime(Instant.now().plus(Duration.ofSeconds(100)))
                        .build();

        // when
        notificationSchedulerService.updateScheduledNotification(notification2);

        assertTrue(scheduler.checkExists(new JobKey(JOB_DETAIL_ID)));

        // NOTE: Below is removed since notification is not in job data map anymore
//        Notification notificationNew =
//                (Notification)
//                        scheduler.getJobDetail(new JobKey(JOB_DETAIL_ID)).getJobDataMap().get("notification");
//
//        assertEquals("New body", notificationNew.getBody());
//        assertEquals("New Title", notificationNew.getTitle());

        assertEquals(
                notification2.getScheduledTime().truncatedTo(ChronoUnit.MILLIS),
                scheduler
                        .getTrigger(new TriggerKey("message-trigger-test-subject-1"))
                        .getStartTime()
                        .toInstant());
    }

    @Test
    void deleteScheduledNotifications() throws SchedulerException {
        // given
        JobDetailFactoryBean jobDetail =
                NotificationSchedulerService.getJobDetailForMessage(notification, MessageType.Notification);
        SimpleTriggerFactoryBean triggerFactoryBean =
                NotificationSchedulerService.getTriggerForMessage(notification, jobDetail.getObject());
        scheduler.scheduleJob(jobDetail.getObject(), triggerFactoryBean.getObject());

        assertTrue(scheduler.checkExists(new JobKey(JOB_DETAIL_ID)));

        // when
        notificationSchedulerService.deleteScheduledNotifications(List.of(notification));

        assertFalse(scheduler.checkExists(new JobKey(JOB_DETAIL_ID)));
    }

    @Test
    void deleteScheduledNotification() throws SchedulerException {

        // given
        JobDetailFactoryBean jobDetail =
                NotificationSchedulerService.getJobDetailForMessage(notification, MessageType.Notification);
        SimpleTriggerFactoryBean triggerFactoryBean =
                NotificationSchedulerService.getTriggerForMessage(notification, jobDetail.getObject());
        scheduler.scheduleJob(jobDetail.getObject(), triggerFactoryBean.getObject());

        assertTrue(scheduler.checkExists(new JobKey(JOB_DETAIL_ID)));

        // when
        notificationSchedulerService.deleteScheduledNotification(notification);

        assertFalse(scheduler.checkExists(new JobKey(JOB_DETAIL_ID)));
    }

    @TestConfiguration
    static class SchedulerServiceTestConfig {
        @Autowired
        private transient Scheduler scheduler;

        @Bean
        public NotificationSchedulerService schedulerServiceBeanConfig() {

            // mock FCM as we do not want to connect to the server
            return new NotificationSchedulerService(
                    mock(FcmSender.class), new SchedulerServiceImpl(scheduler));
        }
    }

    private static class TestJobListener implements JobListener {

        /**
         * Get the name of the <code>JobListener</code>.
         */
        @Override
        public String getName() {
            return "test-job-listener";
        }

        /**
         * Called by the <code>{@link Scheduler}</code> when a <code>{@link JobDetail}</code> is about
         * to be executed (an associated <code>{@link Trigger}</code> has occurred).
         *
         * <p>This method will not be invoked if the execution of the Job was vetoed by a <code>{@link
         * org.quartz.TriggerListener}</code>.
         *
         * @see #jobExecutionVetoed(JobExecutionContext)
         */
        @Override
        public void jobToBeExecuted(JobExecutionContext context) {
            assertEquals(notification, context.getJobDetail().getJobDataMap().get("notification"));
        }

        /**
         * Called by the <code>{@link Scheduler}</code> when a <code>{@link JobDetail}</code> was about
         * to be executed (an associated <code>{@link Trigger}</code> has occurred), but a <code>{@link
         * org.quartz.TriggerListener}</code> vetoed it's execution.
         *
         * @see #jobToBeExecuted(JobExecutionContext)
         */
        @Override
        public void jobExecutionVetoed(JobExecutionContext context) {
            fail(new InterruptedException("The Job Execution was vetoed."));
        }

        /**
         * Called by the <code>{@link Scheduler}</code> after a <code>{@link JobDetail}</code> has been
         * executed, and be for the associated <code>Trigger</code>'s <code>triggered(xx)</code> method
         * has been called.
         */
        @Override
        public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
            assertEquals(notification, context.getJobDetail().getJobDataMap().get("notification"));
            assertNull(jobException);
        }
    }
}
