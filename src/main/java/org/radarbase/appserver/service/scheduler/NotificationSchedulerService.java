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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.service.scheduler.quartz.NotificationJob;
import org.radarbase.appserver.service.scheduler.quartz.QuartzNamingStrategy;
import org.radarbase.appserver.service.scheduler.quartz.SchedulerService;
import org.radarbase.appserver.service.scheduler.quartz.SimpleQuartzNamingStrategy;
import org.radarbase.fcm.downstream.FcmSender;
import org.radarbase.fcm.model.FcmNotificationMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.stereotype.Service;

/**
 * {@link Service} for scheduling Notifications to be sent through FCM at the {@link
 * org.radarbase.appserver.entity.Scheduled} time. It also provided functions for updating/ deleting
 * already scheduled Notification Jobs.
 *
 * @author yatharthranjan
 */
@Service
@Slf4j
public class NotificationSchedulerService {

  // TODO add a schedule cache to cache incoming requests and do batch scheduling

  private static final QuartzNamingStrategy NAMING_STRATEGY = new SimpleQuartzNamingStrategy();

  private final FcmSender fcmSender;
  private final SchedulerService schedulerService;

  public NotificationSchedulerService(
      @Autowired @Qualifier("fcmSenderProps") FcmSender fcmSender,
      @Autowired SchedulerService schedulerService) {
    this.fcmSender = fcmSender;
    this.schedulerService = schedulerService;
  }

  public static SimpleTriggerFactoryBean getTriggerForNotification(
      Notification notification, JobDetail jobDetail) {
    SimpleTriggerFactoryBean triggerFactoryBean = new SimpleTriggerFactoryBean();
    triggerFactoryBean.setJobDetail(jobDetail);
    triggerFactoryBean.setName(
        NAMING_STRATEGY.getTriggerName(
            notification.getUser().getSubjectId(), notification.getId().toString()));
    triggerFactoryBean.setRepeatCount(0);
    triggerFactoryBean.setRepeatInterval(0L);
    triggerFactoryBean.setStartTime(new Date(notification.getScheduledTime().toEpochMilli()));
    triggerFactoryBean.afterPropertiesSet();
    return triggerFactoryBean;
  }

  public static JobDetailFactoryBean getJobDetailForNotification(Notification notification) {
    JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
    jobDetailFactory.setJobClass(NotificationJob.class);
    jobDetailFactory.setDescription("Send Notification at scheduled time...");
    jobDetailFactory.setDurability(true);
    jobDetailFactory.setName(
        NAMING_STRATEGY.getJobKeyName(
            notification.getUser().getSubjectId(), notification.getId().toString()));
    Map<String, Object> map = new HashMap<>();
    map.put("notification", notification);
    jobDetailFactory.setJobDataAsMap(map);
    jobDetailFactory.afterPropertiesSet();
    return jobDetailFactory;
  }

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

    notifications.forEach(
        notification -> {
          log.info("Notification = {}", notification);
          JobDetail jobDetail = getJobDetailForNotification(notification).getObject();

          log.info("Job Detail = {}", jobDetail);
          Set<Trigger> triggerSet = new HashSet<>();
          triggerSet.add(getTriggerForNotification(notification, jobDetail).getObject());

          jobDetailSetMap.putIfAbsent(jobDetail, triggerSet);
        });

    schedulerService.scheduleJobs(jobDetailSetMap);
  }

  public void updateScheduledNotification(Notification notification) {

    String jobKeyString =
        NAMING_STRATEGY.getJobKeyName(
            notification.getUser().getSubjectId(), notification.getId().toString());

    JobKey jobKey = new JobKey(jobKeyString);

    String triggerKeyString =
        NAMING_STRATEGY.getTriggerName(
            notification.getUser().getSubjectId(), notification.getId().toString());

    TriggerKey triggerKey = new TriggerKey(triggerKeyString);

    JobDataMap jobDataMap = new JobDataMap();
    jobDataMap.put("notification", notification);

    schedulerService.updateScheduledJob(jobKey, triggerKey, jobDataMap, notification);
  }

  public void deleteScheduledNotifications(List<Notification> notifications) {
    List<JobKey> keys =
        notifications.stream()
            .map(
                n ->
                    new JobKey(
                        NAMING_STRATEGY.getJobKeyName(
                            n.getUser().getSubjectId(), n.getId().toString())))
            .collect(Collectors.toList());
    schedulerService.deleteScheduledJobs(keys);
  }

  public void deleteScheduledNotification(Notification notification) {
    JobKey key =
        new JobKey(
            NAMING_STRATEGY.getJobKeyName(
                notification.getUser().getSubjectId(), notification.getId().toString()));
    schedulerService.deleteScheduledJob(key);
  }

  private FcmNotificationMessage createMessageFromNotification(Notification notification)
      throws Exception {

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
        .data(notification.getAdditionalData())
        .build();
  }
}
