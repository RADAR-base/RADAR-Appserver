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

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.radarbase.appserver.entity.DataMessage;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.exception.FcmMessageTransmitException;
import org.radarbase.appserver.exception.MessageTransmitException;
import org.radarbase.appserver.service.FcmDataMessageService;
import org.radarbase.appserver.service.FcmNotificationService;
import org.radarbase.appserver.service.MessageType;
import org.radarbase.appserver.service.transmitter.DataMessageTransmitter;
import org.radarbase.appserver.service.transmitter.EmailNotificationTransmitter;
import org.radarbase.appserver.service.transmitter.FcmTransmitter;
import org.radarbase.appserver.service.transmitter.NotificationTransmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A {@link Job} that sends messages to the device or email when executed.
 *
 * @author yatharthranjan
 * @see FcmTransmitter
 * @see EmailNotificationTransmitter
 */
@Slf4j
public class MessageJob implements Job {

  private final transient List<NotificationTransmitter> notificationTransmitters;

  private final transient List<DataMessageTransmitter> dataMessageTransmitters;

  private final transient FcmNotificationService notificationService;

  private final transient FcmDataMessageService dataMessageService;

  public MessageJob(
      List<NotificationTransmitter> notificationTransmitters,
      List<DataMessageTransmitter> dataMessageTransmitters,
      FcmNotificationService notificationService,
      FcmDataMessageService dataMessageService) {
    this.notificationTransmitters = notificationTransmitters;
    this.dataMessageTransmitters = dataMessageTransmitters;
    this.notificationService = notificationService;
    this.dataMessageService = dataMessageService;
  }

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
    JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
    MessageType type = MessageType.valueOf(jobDataMap.getString("messageType"));
    String projectId = jobDataMap.getString("projectId");
    String subjectId = jobDataMap.getString("subjectId");
    Long messageId = jobDataMap.getLong("messageId");
    List<Exception> exceptions = new ArrayList<>();
    try {
      switch (type) {
        case NOTIFICATION:
          Notification notification =
              notificationService.getNotificationByProjectIdAndSubjectIdAndNotificationId(
                  projectId, subjectId, messageId);
          notificationTransmitters.forEach(t -> {
            try {
              t.send(notification);
            } catch (MessageTransmitException e) {
              exceptions.add(e);
            }
          });
          break;
        case DATA:
          DataMessage dataMessage =
              dataMessageService.getDataMessageByProjectIdAndSubjectIdAndDataMessageId(
                  projectId, subjectId, messageId);
          dataMessageTransmitters.forEach(t -> {
            try {
              t.send(dataMessage);
            } catch (MessageTransmitException e) {
              exceptions.add(e);
            }
          });
          break;
        default:
          break;
      }
    } catch (Exception e) {
      log.error("Could not transmit a message", e);
      throw new JobExecutionException("Could not transmit a message", e);
    }

    // Here handle the exceptions that occurred while transmitting the message via the
    // transmitters. At present, only the FcmTransmitter affects the job execution state.
    Optional<Exception> fcmException = exceptions.stream()
        .filter(e -> e instanceof FcmMessageTransmitException)
        .findFirst();
    if (fcmException.isPresent()) {
      throw new JobExecutionException("Could not transmit a message", fcmException.get());
    }
  }
}
