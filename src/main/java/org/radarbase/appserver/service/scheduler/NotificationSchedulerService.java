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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.spi.MutableTrigger;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.service.scheduler.quartz.NotificationJob;
import org.radarbase.appserver.service.scheduler.quartz.SchedulerServiceImpl;
import org.radarbase.fcm.common.ObjectMapperFactory;
import org.radarbase.fcm.downstream.FcmSender;
import org.radarbase.fcm.model.FcmNotificationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * {@link Service} for scheduling Notifications to be sent through FCM at the {@link org.radarbase.appserver.entity.Scheduled} time.
 * It also provided functions for updating/ deleting already scheduled Notification Jobs.
 *
 * @author yatharthranjan
 */
@Slf4j
@Service
public class NotificationSchedulerService {

    // TODO add a schedule cache to cache incoming requests and do batch scheduling

    @Autowired
    private ObjectMapperFactory mapperFactory;

    @Autowired
    @Qualifier("fcmSenderProps")
    private FcmSender fcmSender;

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private SchedulerServiceImpl schedulerService;


    private static final String TRIGGER_PREFIX = "notification-trigger-";
    private static final String JOB_PREFIX = "notification-jobdetail-";

    public void sendNotification(Notification notification) throws Exception {
        fcmSender.send(createMessageFromNotification(notification));
    }

    public void scheduleNotification(Notification notification) {
        log.info("Notification = {}", notification);
        JobDetail jobDetail = getJobDetailForNotification(notification).getObject();

        log.info("Job Detail = {}", jobDetail);
        Trigger trigger = getTriggerForNotification(notification, jobDetail).getObject();

        schedulerService.scheduleJob(jobDetail, trigger);
    }

    public void scheduleNotifications(List<Notification> notifications) {

        Map<JobDetail, Set<? extends Trigger>> jobDetailSetMap = new HashMap<>();

        notifications.forEach(notification -> {
            log.info("Notification = {}", notification);
            JobDetail jobDetail = getJobDetailForNotification(notification).getObject();

            log.info("Job Detail = {}", jobDetail);
            Set<Trigger> triggerSet = new HashSet<>();
            triggerSet.add(getTriggerForNotification(notification, jobDetail).getObject());

            jobDetailSetMap.put(jobDetail, triggerSet);
        });

        schedulerService.scheduleJobs(jobDetailSetMap);
    }

    public void updateScheduledNotification(Notification notification) {

        String jobKeyString = new StringBuilder().append(JOB_PREFIX)
                .append(notification.getUser().getSubjectId())
                .append("-")
                .append(notification.getId())
                .toString();

       JobKey jobKey = new JobKey(jobKeyString);

       String triggerKeyString = new StringBuilder().append(TRIGGER_PREFIX)
               .append(notification.getUser().getSubjectId())
               .append("-")
               .append(notification.getId())
               .toString();

       TriggerKey triggerKey = new TriggerKey(triggerKeyString);

       JobDataMap jobDataMap = new JobDataMap();
       jobDataMap.put("notification", notification);

       schedulerService.updateScheduledJob(jobKey, triggerKey, jobDataMap, notification);
    }

    public void deleteScheduledNotifications(List<Notification> notifications) {
        List<JobKey> keys = notifications.stream().map(n -> new JobKey("notification-jobdetail-"
                + n.getUser().getSubjectId() + "-" + n.getId())).collect(Collectors.toList());
        schedulerService.deleteScheduledJobs(keys);
    }

    public void deleteScheduledNotification(Notification notification) {
        JobKey key = new JobKey("notification-jobdetail-"
                + notification.getUser().getSubjectId() + "-" + notification.getId());
        schedulerService.deleteScheduledJob(key);
    }

    private static SimpleTriggerFactoryBean getTriggerForNotification(Notification notification, JobDetail jobDetail) {
        SimpleTriggerFactoryBean triggerFactoryBean = new SimpleTriggerFactoryBean();
        triggerFactoryBean.setJobDetail(jobDetail);
        triggerFactoryBean.setName("notification-trigger-" + notification.getUser().getSubjectId() + "-" + notification.getId());
        triggerFactoryBean.setRepeatCount(0);
        triggerFactoryBean.setRepeatInterval(0L);
        triggerFactoryBean.setStartTime(new Date(notification.getScheduledTime().toEpochMilli()));
        triggerFactoryBean.afterPropertiesSet();
        return triggerFactoryBean;
    }

    private static JobDetailFactoryBean getJobDetailForNotification(Notification notification) {
        JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
        jobDetailFactory.setJobClass(NotificationJob.class);
        jobDetailFactory.setDescription("Send Notification at scheduled time...");
        jobDetailFactory.setDurability(true);
        jobDetailFactory.setName("notification-jobdetail-" + notification.getUser().getSubjectId() + "-" + notification.getId());
        Map<String, Object> map = new HashMap<>();
        map.put("notification", notification);
        jobDetailFactory.setJobDataAsMap(map);
        jobDetailFactory.afterPropertiesSet();
        return jobDetailFactory;
    }

    private FcmNotificationMessage createMessageFromNotification(Notification notification) throws Exception {
        ObjectMapper mapper = mapperFactory.getObject();

        Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("body", notification.getBody());
        notificationMap.put("title", notification.getTitle());
        notificationMap.put("sound", "default");

        return FcmNotificationMessage.builder()
                .to(notification.getUser().getFcmToken())
                .deliveryReceiptRequested(true)
                .messageId(String.valueOf(notification.getFcmMessageId()))
                .timeToLive(notification.getTtlSeconds())
                .notification(notificationMap)
                .build();
    }
}
