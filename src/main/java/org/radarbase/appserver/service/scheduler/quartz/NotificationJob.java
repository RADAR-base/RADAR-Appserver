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

import lombok.SneakyThrows;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.service.FcmNotificationService;
import org.radarbase.appserver.service.scheduler.NotificationSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A {@link Job} that sends an FCM notification to the device when executed.
 *
 * @see NotificationSchedulerService
 * @see SchedulerServiceImpl
 * @author yatharthranjan
 */
public class NotificationJob implements Job {

  @Autowired private transient NotificationSchedulerService schedulerService;

  @Autowired private transient FcmNotificationService notificationService;
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
  @SneakyThrows
  @Override
  public void execute(JobExecutionContext context) {
    Notification notification =
        notificationService.getNotificationByProjectIdAndSubjectIdAndNotificationId(
            context.getJobDetail().getJobDataMap().getString("projectId"),
            context.getJobDetail().getJobDataMap().getString("subjectId"),
            context.getJobDetail().getJobDataMap().getLong("notificationId"));
//    Notification notification =
//        (Notification) context.getJobDetail().getJobDataMap().get("notification");
    schedulerService.sendNotification(notification);
  }
}
