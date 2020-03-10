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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.radarbase.appserver.entity.DataMessage;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.event.state.DataMessageStateEvent;
import org.radarbase.appserver.event.state.MessageState;
import org.radarbase.appserver.event.state.NotificationStateEvent;
import org.radarbase.appserver.repository.DataMessageRepository;
import org.radarbase.appserver.repository.NotificationRepository;
import org.radarbase.appserver.service.MessageType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class QuartzMessageJobListener implements JobListener {

    private final transient ApplicationEventPublisher messageStateEventPublisher;
    private final transient NotificationRepository notificationRepository;
    private final transient DataMessageRepository dataMessageRepository;

    public QuartzMessageJobListener(
            ApplicationEventPublisher messageStateEventPublisher,
            NotificationRepository notificationRepository, DataMessageRepository dataMessageRepository) {
        this.messageStateEventPublisher = messageStateEventPublisher;
        this.notificationRepository = notificationRepository;
        this.dataMessageRepository = dataMessageRepository;
    }

    /**
     * Get the name of the <code>JobListener</code>.
     */
    @Override
    public String getName() {
        return getClass().getName();
    }

    /**
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link JobDetail}</code> is about to
     * be executed (an associated <code>{@link Trigger}</code> has occurred).
     *
     * <p>This method will not be invoked if the execution of the Job was vetoed by a <code>{
     * TriggerListener}</code>.
     *
     * @see #jobExecutionVetoed(JobExecutionContext)
     */
    @Override
    public void jobToBeExecuted(JobExecutionContext context) {
        // Not implemented
    }

    /**
     * Called by the <code>{@link Scheduler}</code> when a <code>{@link JobDetail}</code> was about to
     * be executed (an associated <code>{@link Trigger}</code> has occurred), but a <code>{
     * TriggerListener}</code> vetoed it's execution.
     *
     * @see #jobToBeExecuted(JobExecutionContext)
     */
    @Override
    public void jobExecutionVetoed(JobExecutionContext context) {
        // Not implemented
    }

    /**
     * Called by the <code>{@link Scheduler}</code> after a <code>{@link JobDetail}</code> has been
     * executed, and be for the associated <code>Trigger</code>'s <code>triggered(xx)</code> method
     * has been called.
     */
    @Override
    public void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
        JobDataMap jobDataMap = context.getMergedJobDataMap();
        Long messageId = jobDataMap.getLongValue("messageId");
        String messageType = jobDataMap.getString("messageType");
        if (messageType == null) {
            log.warn("The message type does not exist.");
            return;
        }

        MessageType type = MessageType.valueOf(messageType);
        switch (type) {
            case NOTIFICATION:
                Optional<Notification> notification =
                        notificationRepository.findById(messageId);
                if (notification.isEmpty()) {
                    log.warn("The notification does not exist in database and yet was scheduled.");
                    return;
                }
                if (jobException != null) {
                    Map<String, String> additionalInfo = new HashMap<>();
                    additionalInfo.put("error", jobException.getMessage());
                    additionalInfo.put("error_description", jobException.toString());
                    NotificationStateEvent notificationStateEventError =
                            new NotificationStateEvent(
                                    this, notification.get(), MessageState.ERRORED, additionalInfo, Instant.now());
                    messageStateEventPublisher.publishEvent(notificationStateEventError);

                    log.warn("The job could not be executed.", jobException);
                    return;
                }

                NotificationStateEvent notificationStateEvent =
                        new NotificationStateEvent(
                                this, notification.get(), MessageState.EXECUTED, null, Instant.now());
                messageStateEventPublisher.publishEvent(notificationStateEvent);
                break;

            case DATA:
                Optional<DataMessage> dataMessage =
                        dataMessageRepository.findById(messageId);
                if (dataMessage.isEmpty()) {
                    log.warn("The data message does not exist in database and yet was scheduled.");
                    return;
                }

                if (jobException != null) {
                    Map<String, String> additionalInfo = new HashMap<>();
                    additionalInfo.put("error", jobException.getMessage());
                    additionalInfo.put("error_description", jobException.toString());
                    DataMessageStateEvent dataMessageStateEventError =
                            new DataMessageStateEvent(
                                    this, dataMessage.get(), MessageState.ERRORED, additionalInfo, Instant.now());
                    messageStateEventPublisher.publishEvent(dataMessageStateEventError);

                    log.warn("The job could not be executed.", jobException);
                    return;
                }

                DataMessageStateEvent dataMessageStateEvent =
                        new DataMessageStateEvent(
                                this, dataMessage.get(), MessageState.EXECUTED, null, Instant.now());
                messageStateEventPublisher.publishEvent(dataMessageStateEvent);
                break;

            default:
                log.warn("The message type does not exist.");
                break;
        }
    }
}
