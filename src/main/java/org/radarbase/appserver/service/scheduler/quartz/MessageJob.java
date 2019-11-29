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

package org.radarbase.appserver.service.scheduler.quartz;

import org.apache.juli.logging.Log;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.radarbase.appserver.entity.DataMessage;
import org.radarbase.appserver.entity.Message;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.service.FcmDataMessageService;
import org.radarbase.appserver.service.FcmNotificationService;
import org.radarbase.appserver.service.MessageType;
import org.radarbase.appserver.service.scheduler.DataMessageSchedulerService;
import org.radarbase.appserver.service.scheduler.NotificationSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A {@link Job} that sends an FCM message to the device when executed.
 *
 * @author yatharthranjan
 * @see NotificationSchedulerService
 * @see SchedulerServiceImpl
 */
public class MessageJob implements Job {

    @Autowired
    private transient NotificationSchedulerService notificationSchedulerService;

    @Autowired
    private transient DataMessageSchedulerService dataMessageSchedulerService;

    @Autowired
    private transient FcmNotificationService notificationService;

    @Autowired
    private transient FcmDataMessageService dataMessageService;

    /**
     * Called by the <code>{@link org.quartz.Scheduler}</code> when a <code>{@link org.quartz.Trigger}
     * </code> fires that is associated with the <code>Job</code>.
     *
     * <p>The implementation may wish to set a {@link JobExecutionContext#setResult(Object) result}
     * object on the {@link JobExecutionContext} before this method exits. The result itself is
     * meaningless to Quartz, but may be informative to <code>{@link org.quartz.JobListener}s</code>
     * or <code>{@link org.quartz.TriggerListener}s</code> that are watching the job's execution.
     *
     * @param context context containing jobs details and data added when creating the job.
     * @throws RuntimeException if there is an CustomExceptionHandler while executing the job.
     */
    @Override
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
            MessageType type = MessageType.valueOf(jobDataMap.getString("messageType"));
            String projectId = jobDataMap.getString("projectId");
            String subjectId = jobDataMap.getString("subjectId");
            Long messageId = jobDataMap.getLong("messageId");

            switch (type) {
                case NOTIFICATION:
                    Notification notification =
                            notificationService.getNotificationByProjectIdAndSubjectIdAndNotificationId(
                                    projectId, subjectId, messageId);
                    notificationSchedulerService.send(notification);
                    break;
                case DATA:
                    DataMessage dataMessage =
                            dataMessageService.getDataMessageByProjectIdAndSubjectIdAndDataMessageId(
                                    projectId, subjectId, messageId);
                    dataMessageSchedulerService.send(dataMessage);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            throw new JobExecutionException(e);
        }
    }
}
