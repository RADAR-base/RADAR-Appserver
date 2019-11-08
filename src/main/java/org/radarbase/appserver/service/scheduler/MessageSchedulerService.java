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

import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.radarbase.appserver.entity.Message;
import org.radarbase.appserver.service.MessageType;
import org.radarbase.appserver.service.scheduler.quartz.*;
import org.radarbase.fcm.downstream.FcmSender;
import org.radarbase.fcm.model.FcmDownstreamMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * {@link Service} for scheduling Notifications to be sent through FCM at the {@link
 * org.radarbase.appserver.entity.Scheduled} time. It also provided functions for updating/ deleting
 * already scheduled Notification Jobs.
 *
 * @author yatharthranjan
 */
@Service
@Slf4j
public class MessageSchedulerService {

  // TODO add a schedule cache to cache incoming requests and do batch scheduling

  protected static final QuartzNamingStrategy NAMING_STRATEGY = new SimpleQuartzNamingStrategy();
  protected static final boolean IS_DELIVERY_RECEIPT_REQUESTED = true;
  protected final transient FcmSender fcmSender;
  protected final transient SchedulerService schedulerService;

  public MessageSchedulerService(
      @Autowired @Qualifier("fcmSenderProps") FcmSender fcmSender,
      @Autowired SchedulerService schedulerService) {
    this.fcmSender = fcmSender;
    this.schedulerService = schedulerService;
  }

  public static SimpleTriggerFactoryBean getTriggerForMessage(
          Message message, JobDetail jobDetail) {
    SimpleTriggerFactoryBean triggerFactoryBean = new SimpleTriggerFactoryBean();
    triggerFactoryBean.setJobDetail(jobDetail);
    triggerFactoryBean.setName(
        NAMING_STRATEGY.getTriggerName(
                message.getUser().getSubjectId(), message.getId().toString()));
    triggerFactoryBean.setRepeatCount(0);
    triggerFactoryBean.setRepeatInterval(0L);
    triggerFactoryBean.setStartTime(new Date(message.getScheduledTime().toEpochMilli()));
    triggerFactoryBean.afterPropertiesSet();
    triggerFactoryBean.setMisfireInstruction(SimpleTrigger.MISFIRE_INSTRUCTION_FIRE_NOW);
    return triggerFactoryBean;
  }

  public static JobDetailFactoryBean getJobDetailForMessage(Message message, MessageType messageType) {
    JobDetailFactoryBean jobDetailFactory = new JobDetailFactoryBean();
    jobDetailFactory.setJobClass(MessageJob.class);
    jobDetailFactory.setDescription("Send message at scheduled time...");
    jobDetailFactory.setDurability(true);
    jobDetailFactory.setName(
            NAMING_STRATEGY.getJobKeyName(
                    message.getUser().getSubjectId(), message.getId().toString()));
    Map<String, Object> map = new HashMap<>();
    map.put("subjectId", message.getUser().getSubjectId());
    map.put("projectId", message.getUser().getProject().getProjectId());
    map.put("messageId", message.getId());
    map.put("messageType", messageType.toString());
    jobDetailFactory.setJobDataAsMap(map);
    jobDetailFactory.afterPropertiesSet();
    return jobDetailFactory;
  }

  protected static void putIfNotNull(Map<String, Object> map, String key, Object value) {
    if (value != null) {
      map.put(key, value);
    }
  }

  public void scheduleMessage(Message message) {
    log.info("Message = {}", message);

    JobDetail jobDetail = getJobDetailForMessage(message, getMessageType(message)).getObject();

    if (jobDetail != null) {
      log.debug("Job Detail = {}", jobDetail);
      Trigger trigger = getTriggerForMessage(message, jobDetail).getObject();

      schedulerService.scheduleJob(jobDetail, trigger);
    }
  }

  public void scheduleMessages(List<Message> messages) {
    Map<JobDetail, Set<? extends Trigger>> jobDetailSetMap = new HashMap<>();

    messages.forEach(
            (Message message) -> {
              log.debug("Message = {}", message);
              JobDetail jobDetail = getJobDetailForMessage(message, getMessageType(message)).getObject();

              log.debug("Job Detail = {}", jobDetail);
              Set<Trigger> triggerSet = new HashSet<>();
              triggerSet.add(getTriggerForMessage(message, jobDetail).getObject());

              jobDetailSetMap.putIfAbsent(jobDetail, triggerSet);
            });
    log.info("Scheduling {} messages", messages.size());
    schedulerService.scheduleJobs(jobDetailSetMap);
  }

  public void updateScheduledMessage(Message message) {
    String jobKeyString =
        NAMING_STRATEGY.getJobKeyName(
                message.getUser().getSubjectId(), message.getId().toString());
    JobKey jobKey = new JobKey(jobKeyString);
    String triggerKeyString =
        NAMING_STRATEGY.getTriggerName(
                message.getUser().getSubjectId(), message.getId().toString());
    TriggerKey triggerKey = new TriggerKey(triggerKeyString);
    JobDataMap jobDataMap = new JobDataMap();

    schedulerService.updateScheduledJob(jobKey, triggerKey, jobDataMap, message);
  }

  public void deleteScheduledMessages(List<Message> messages) {
    List<JobKey> keys =
            messages.stream()
                    .map(
                            n ->
                                    new JobKey(
                                            NAMING_STRATEGY.getJobKeyName(
                                                    n.getUser().getSubjectId(), n.getId().toString())))
                    .collect(Collectors.toList());
    schedulerService.deleteScheduledJobs(keys);
  }

  public void deleteScheduledMessage(Message message) {
    JobKey key =
            new JobKey(
                    NAMING_STRATEGY.getJobKeyName(
                            message.getUser().getSubjectId(), message.getId().toString()));
    schedulerService.deleteScheduledJob(key);
  }

  public void sendMessage(FcmDownstreamMessage message) throws Exception {
    fcmSender.send(message);
  }

  public MessageType getMessageType(Message message){
    MessageType type = message.toString().contains(MessageType.Notification.toString())? MessageType.Notification : MessageType.Data;
    return type;
  }
}
