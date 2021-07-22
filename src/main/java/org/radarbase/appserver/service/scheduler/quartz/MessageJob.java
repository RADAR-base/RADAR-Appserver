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

import com.google.firebase.ErrorCode;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MessagingErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.radarbase.appserver.dto.fcm.FcmUserDto;
import org.radarbase.appserver.entity.DataMessage;
import org.radarbase.appserver.entity.Message;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.service.FcmDataMessageService;
import org.radarbase.appserver.service.FcmNotificationService;
import org.radarbase.appserver.service.MessageType;
import org.radarbase.appserver.service.UserService;
import org.radarbase.appserver.service.scheduler.DataMessageSchedulerService;
import org.radarbase.appserver.service.scheduler.NotificationSchedulerService;

/**
 * A {@link Job} that sends an FCM message to the device when executed.
 *
 * @author yatharthranjan
 * @see NotificationSchedulerService
 * @see SchedulerServiceImpl
 */
@Slf4j
public class MessageJob implements Job {

  private final transient NotificationSchedulerService notificationSchedulerService;

  private final transient DataMessageSchedulerService dataMessageSchedulerService;

  private final transient FcmNotificationService notificationService;

  private final transient FcmDataMessageService dataMessageService;

  private final transient UserService userService;

  public MessageJob(
      NotificationSchedulerService notificationSchedulerService,
      DataMessageSchedulerService dataMessageSchedulerService,
      FcmNotificationService notificationService,
      FcmDataMessageService dataMessageService,
      UserService userService) {
    this.notificationSchedulerService = notificationSchedulerService;
    this.dataMessageSchedulerService = dataMessageSchedulerService;
    this.notificationService = notificationService;
    this.dataMessageService = dataMessageService;
    this.userService = userService;
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
    Message message = null;
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
          message = notification;
          notificationSchedulerService.send(notification);
          break;
        case DATA:
          DataMessage dataMessage =
              dataMessageService.getDataMessageByProjectIdAndSubjectIdAndDataMessageId(
                  projectId, subjectId, messageId);
          message = dataMessage;
          dataMessageSchedulerService.send(dataMessage);
          break;
        default:
          break;
      }
    } catch (FirebaseMessagingException exc) {
      log.error("Error occurred when sending downstream message.", exc);
      // TODO: update the data message status using event
      if (message != null) {
        handleErrorCode(exc.getErrorCode(), message);
        handleFCMErrorCode(exc.getMessagingErrorCode(), message);
      }
    } catch (Exception e) {
      throw new JobExecutionException(e);
    }
  }

  protected void handleErrorCode(ErrorCode errorCode, Message message) {
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

  protected void handleFCMErrorCode(MessagingErrorCode errorCode, Message message) {
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
        FcmUserDto userDto = new FcmUserDto(message.getUser()).setFcmToken("undefined");
        log.warn("The Device for user {} was unregistered.", userDto.getSubjectId());
        notificationService.removeNotificationsForUser(
            userDto.getProjectId(), userDto.getSubjectId());
        dataMessageService.removeDataMessagesForUser(
            userDto.getProjectId(), userDto.getSubjectId());
        userService.updateUser(userDto);
        break;
    }
  }
}
