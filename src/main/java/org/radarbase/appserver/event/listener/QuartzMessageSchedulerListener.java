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

package org.radarbase.appserver.event.listener;

import java.time.Instant;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.radarbase.appserver.entity.DataMessage;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.event.state.MessageState;
import org.radarbase.appserver.event.state.dto.DataMessageStateEventDto;
import org.radarbase.appserver.event.state.dto.NotificationStateEventDto;
import org.radarbase.appserver.repository.DataMessageRepository;
import org.radarbase.appserver.repository.NotificationRepository;
import org.radarbase.appserver.service.MessageType;
import org.radarbase.appserver.service.scheduler.quartz.QuartzNamingStrategy;
import org.radarbase.appserver.service.scheduler.quartz.SimpleQuartzNamingStrategy;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class QuartzMessageSchedulerListener implements SchedulerListener {

    protected static final QuartzNamingStrategy NAMING_STRATEGY = new SimpleQuartzNamingStrategy();
    private final transient ApplicationEventPublisher messageStateEventPublisher;
    private final transient NotificationRepository notificationRepository;
    private final transient DataMessageRepository dataMessageRepository;
    private final transient Scheduler scheduler;

    public QuartzMessageSchedulerListener(
            ApplicationEventPublisher messageStateEventPublisher,
            NotificationRepository notificationRepository,
            DataMessageRepository dataMessageRepository,
            Scheduler scheduler) {
        this.messageStateEventPublisher = messageStateEventPublisher;
        this.notificationRepository = notificationRepository;
        this.dataMessageRepository = dataMessageRepository;
        this.scheduler = scheduler;
    }

    /**
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link JobDetail}</code> is
     * scheduled.
     */
    @Override
    public void jobScheduled(Trigger trigger) {
        JobDetail jobDetail;
        try {
            jobDetail = scheduler.getJobDetail(trigger.getJobKey());
        } catch (SchedulerException exc) {
            log.warn("Encountered error while getting job information from Trigger: ", exc);
            return;
        }

        JobDataMap jobDataMap = jobDetail.getJobDataMap();
        MessageType type = MessageType.valueOf(jobDataMap.getString("messageType"));
        Long messageId = jobDataMap.getLongValue("messageId");

        switch (type) {
            case NOTIFICATION:
                Optional<Notification> notification =
                        notificationRepository.findById(messageId);
                if (notification.isEmpty()) {
                    log.warn("The notification does not exist in database and yet was scheduled.");
                    return;
                }
                NotificationStateEventDto notificationStateEvent =
                        new NotificationStateEventDto(
                                this, notification.get(), MessageState.SCHEDULED, null, Instant.now());
                messageStateEventPublisher.publishEvent(notificationStateEvent);
                break;
            case DATA:
                Optional<DataMessage> dataMessage =
                        dataMessageRepository.findById(messageId);
                if (dataMessage.isEmpty()) {
                    log.warn("The data message does not exist in database and yet was scheduled.");
                    return;
                }
                DataMessageStateEventDto dataMessageStateEvent =
                        new DataMessageStateEventDto(
                                this, dataMessage.get(), MessageState.SCHEDULED, null, Instant.now());
                messageStateEventPublisher.publishEvent(dataMessageStateEvent);
                break;
            default:
                break;
        }
    }

    /**
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link JobDetail}</code> is
     * unscheduled.
     *
     * @see SchedulerListener#schedulingDataCleared()
     */
    @Override
    public void jobUnscheduled(TriggerKey triggerKey) {
        long notificationId;
        try {
            notificationId = Long.parseLong(NAMING_STRATEGY.getMessageId(triggerKey.getName()));
        } catch (NumberFormatException ex) {
            log.warn("The message id could not be established from unscheduled trigger.");
            return;
        }
        Optional<Notification> notification =
                notificationRepository.findById(notificationId);

        if (notification.isEmpty()) {
            log.warn("The notification does not exist in database and yet was unscheduled.");
            return;
        }
        NotificationStateEventDto notificationStateEvent =
                new NotificationStateEventDto(
                        this, notification.get(), MessageState.CANCELLED, null, Instant.now());
        messageStateEventPublisher.publishEvent(notificationStateEvent);
    }

    /**
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code> has reached
     * the condition in which it will never fire again.
     */
    @Override
    public void triggerFinalized(Trigger trigger) {
        // Not implemented
    }

    /**
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code> has been
     * paused.
     */
    @Override
    public void triggerPaused(TriggerKey triggerKey) {
        // Not implemented
    }

    /**
     * Called by the <code>{@link Scheduler}</code> when a group of <code>{@link Trigger}s</code> has
     * been paused.
     *
     * <p>If all groups were paused then triggerGroup will be null
     *
     * @param triggerGroup the paused group, or null if all were paused
     */
    @Override
    public void triggersPaused(String triggerGroup) {
        // Not implemented
    }

    /**
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link Trigger}</code> has been
     * un-paused.
     */
    @Override
    public void triggerResumed(TriggerKey triggerKey) {
        // Not implemented
    }

    /**
     * Called by the <code>{@link Scheduler}</code> when a group of <code>{@link Trigger}s</code> has
     * been un-paused.
     */
    @Override
    public void triggersResumed(String triggerGroup) {
        // Not implemented
    }

    /**
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link JobDetail}</code> has been
     * added.
     */
    @Override
    public void jobAdded(JobDetail jobDetail) {
        // Not implemented
    }

    /**
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link JobDetail}</code> has been
     * deleted.
     */
    @Override
    public void jobDeleted(JobKey jobKey) {
        // Not implemented
    }

    /**
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link JobDetail}</code> has been
     * paused.
     */
    @Override
    public void jobPaused(JobKey jobKey) {
        // Not implemented
    }

    /**
     * Called by the <code>{@link Scheduler}</code> when a group of <code>{@link JobDetail}s</code>
     * has been paused.
     *
     * @param jobGroup the paused group, or null if all were paused
     */
    @Override
    public void jobsPaused(String jobGroup) {
        // Not implemented
    }

    /**
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link JobDetail}</code> has been
     * un-paused.
     */
    @Override
    public void jobResumed(JobKey jobKey) {
        // Not implemented
    }

    /**
     * Called by the <code>{@link Scheduler}</code> when a group of <code>{@link JobDetail}s</code>
     * has been un-paused.
     */
    @Override
    public void jobsResumed(String jobGroup) {
        // Not implemented
    }

    /**
     * Called by the <code>{@link Scheduler}</code> when a serious error has occurred within the
     * scheduler - such as repeated failures in the <code>JobStore</code>, or the inability to
     * instantiate a <code>Job</code> instance when its <code>Trigger</code> has fired.
     *
     * <p>The <code>getErrorCode()</code> method of the given SchedulerException can be used to
     * determine more specific information about the type of error that was encountered.
     */
    @Override
    public void schedulerError(String msg, SchedulerException cause) {
        // Not implemented
    }

    /**
     * Called by the <code>{@link Scheduler}</code> to inform the listener that it has move to standby
     * mode.
     */
    @Override
    public void schedulerInStandbyMode() {
        // Not implemented
    }

    /**
     * Called by the <code>{@link Scheduler}</code> to inform the listener that it has started.
     */
    @Override
    public void schedulerStarted() {
        // Not implemented
    }

    /**
     * Called by the <code>{@link Scheduler}</code> to inform the listener that it is starting.
     */
    @Override
    public void schedulerStarting() {
        // Not implemented
    }

    /**
     * Called by the <code>{@link Scheduler}</code> to inform the listener that it has shutdown.
     */
    @Override
    public void schedulerShutdown() {
        // Not implemented
    }

    /**
     * Called by the <code>{@link Scheduler}</code> to inform the listener that it has begun the
     * shutdown sequence.
     */
    @Override
    public void schedulerShuttingdown() {
        // Not implemented
    }

    /**
     * Called by the <code>{@link Scheduler}</code> to inform the listener that all jobs, triggers and
     * calendars were deleted.
     */
    @Override
    public void schedulingDataCleared() {
        // Not implemented
    }
}
