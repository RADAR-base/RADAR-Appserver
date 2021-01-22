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

import com.google.firebase.ErrorCode;
import com.google.firebase.messaging.MessagingErrorCode;
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
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.radarbase.appserver.entity.DataMessage;
import org.radarbase.appserver.entity.Message;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.service.MessageType;
import org.radarbase.appserver.service.scheduler.quartz.MessageJob;
import org.radarbase.appserver.service.scheduler.quartz.QuartzNamingStrategy;
import org.radarbase.appserver.service.scheduler.quartz.SchedulerService;
import org.radarbase.appserver.service.scheduler.quartz.SimpleQuartzNamingStrategy;
import org.radarbase.fcm.downstream.FcmSender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.quartz.JobDetailFactoryBean;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public abstract class MessageSchedulerService<T extends Message> {

    // TODO add a schedule cache to cache incoming requests

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

    public abstract void send(T message) throws Exception;

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

    public void schedule(T message) {
        log.info("Message = {}", message);

        JobDetail jobDetail = getJobDetailForMessage(message, getMessageType(message)).getObject();

        if (jobDetail != null) {
            boolean jobExistAlready = schedulerService.checkJobExists(jobDetail.getKey());
            if (jobExistAlready) {
                log.info("Job has been scheduled already.");
                }
            else {
                log.debug("Job Detail = {}", jobDetail);
                Trigger trigger = getTriggerForMessage(message, jobDetail).getObject();
                schedulerService.scheduleJob(jobDetail, trigger);
            }
        }
    }

    public void scheduleMultiple(List<T> messages) {
        Map<JobDetail, Set<? extends Trigger>> jobDetailSetMap = new HashMap<>();
        messages.forEach(
                (T m) -> {
                    log.debug("Message = {}", m);
                    JobDetail jobDetail = getJobDetailForMessage(m, getMessageType(m)).getObject();
                    boolean jobExistAlready = schedulerService.checkJobExists(jobDetail.getKey());
                    if (jobExistAlready) {
                        // To avoid rescheduling the jobs which are scheduled already
                        return;
                    }
                    log.debug("Job Detail = {}", jobDetail);
                    Set<Trigger> triggerSet = new HashSet<>();
                    triggerSet.add(getTriggerForMessage(m, jobDetail).getObject());

                    jobDetailSetMap.putIfAbsent(jobDetail, triggerSet);
                });
        log.info("Scheduling {} messages", jobDetailSetMap.size());
        schedulerService.scheduleJobs(jobDetailSetMap);
    }

    public void updateScheduled(T message) {
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

    public void deleteScheduledMultiple(List<T> messages) {
        List<JobKey> keys =
                messages.stream()
                        .map(
                                n -> new JobKey(NAMING_STRATEGY.getJobKeyName(
                                                n.getUser().getSubjectId(), n.getId().toString())))
                        .collect(Collectors.toList());
        schedulerService.deleteScheduledJobs(keys);
    }

    public void deleteScheduled(T message) {
        JobKey key =
                new JobKey(
                        NAMING_STRATEGY.getJobKeyName(
                                message.getUser().getSubjectId(), message.getId().toString()));
        schedulerService.deleteScheduledJob(key);
    }

    public MessageType getMessageType(T message) {
        if (message instanceof Notification) {
            return MessageType.NOTIFICATION;
        } else if (message instanceof DataMessage) {
            return MessageType.DATA;
        } else {
            return MessageType.UNKNOWN;
        }
    }

    protected void handleErrorCode(ErrorCode errorCode) {
        // More info on ErrorCode: https://firebase.google.com/docs/reference/fcm/rest/v1/ErrorCode
        switch (errorCode) {
            case INVALID_ARGUMENT:
            case INTERNAL:
            case ABORTED:
            case CONFLICT:
            case CANCELLED:
            case DATA_LOSS:
            case NOT_FOUND:
            case OUT_OF_RANGE:
            case ALREADY_EXISTS:
            case DEADLINE_EXCEEDED:
            case PERMISSION_DENIED:
            case RESOURCE_EXHAUSTED:
            case FAILED_PRECONDITION:
            case UNAUTHENTICATED:
            case UNKNOWN:
                break;
            case UNAVAILABLE:
                // TODO: Could schedule for retry.
                log.warn("The FCM service is unavailable.");
                break;

        }
    }

    protected void handleFCMErrorCode(MessagingErrorCode errorCode) {
        switch (errorCode) {
            case INTERNAL:
            case QUOTA_EXCEEDED:
            case INVALID_ARGUMENT:
            case SENDER_ID_MISMATCH:
            case THIRD_PARTY_AUTH_ERROR:
                break;
            case UNAVAILABLE:
                // TODO: Could schedule for retry.
                log.warn("The FCM service is unavailable.");
                break;
            case UNREGISTERED:
                //TODO: remove all scheduled notifications/messages for this user.
                log.warn("The Device was unregistered.");
                break;
        }
    }
}
