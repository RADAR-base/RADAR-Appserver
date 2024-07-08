package org.radarbase.appserver.service.scheduler.quartz;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionException;
import org.quartz.SchedulerException;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.JobExecutionContextImpl;
import org.radarbase.appserver.entity.DataMessage;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.exception.EmailMessageTransmitException;
import org.radarbase.appserver.exception.FcmMessageTransmitException;
import org.radarbase.appserver.exception.MessageTransmitException;
import org.radarbase.appserver.service.FcmDataMessageService;
import org.radarbase.appserver.service.FcmNotificationService;
import org.radarbase.appserver.service.MessageType;
import org.radarbase.appserver.service.transmitter.DataMessageTransmitter;
import org.radarbase.appserver.service.transmitter.NotificationTransmitter;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(classes = {MessageJob.class})
@ExtendWith(SpringExtension.class)
class MessageJobTest {

  private MessageJob messageJob;

  @MockBean
  private FcmDataMessageService fcmDataMessageService;

  @MockBean
  private FcmNotificationService fcmNotificationService;

  @MockBean
  private NotificationTransmitter notificationTransmitter;

  @MockBean
  private DataMessageTransmitter dataMessageTransmitter;

  private JobDataMap jobDataMap;
  private JobExecutionContextImpl jobExecutionContext;
  private Notification notification;
  private DataMessage dataMessage;

  @BeforeEach
  void setUp() {
    List<NotificationTransmitter> notificationTransmitters = new ArrayList<>();
    notificationTransmitters.add(notificationTransmitter);
    notificationTransmitters.add(notificationTransmitter);

    List<DataMessageTransmitter> dataMessageTransmitters = new ArrayList<>();
    dataMessageTransmitters.add(dataMessageTransmitter);
    dataMessageTransmitters.add(dataMessageTransmitter);

    messageJob = new MessageJob(notificationTransmitters, dataMessageTransmitters, fcmNotificationService, fcmDataMessageService);

    jobExecutionContext = mock(JobExecutionContextImpl.class);
    JobDetailImpl job = mock(JobDetailImpl.class);
    jobDataMap = mock(JobDataMap.class);
    when(job.getJobDataMap()).thenReturn(jobDataMap);
    when(jobDataMap.getString("projectId")).thenReturn("projectId");
    when(jobDataMap.getString("subjectId")).thenReturn("subjectId");
    when(jobDataMap.getLong("messageId")).thenReturn(1L);
    setNotificationType(MessageType.NOTIFICATION);

    when(jobExecutionContext.getJobDetail()).thenReturn(job);

    notification = mock(Notification.class);
    dataMessage = mock(DataMessage.class);
    when(fcmNotificationService.getNotificationByProjectIdAndSubjectIdAndNotificationId("projectId", "subjectId", 1L)).thenReturn(notification);
    when(fcmDataMessageService.getDataMessageByProjectIdAndSubjectIdAndDataMessageId("projectId", "subjectId", 1L)).thenReturn(dataMessage);
  }

  @Test
  void testExecuteNotification() throws SchedulerException, MessageTransmitException {
    setNotificationType(MessageType.NOTIFICATION);
    messageJob.execute(jobExecutionContext);
    verify(notificationTransmitter, times(2)).send(notification);
    verify(dataMessageTransmitter, never()).send(any());
  }

  @Test
  void testExecuteDataMessage() throws SchedulerException, MessageTransmitException {
    setNotificationType(MessageType.DATA);
    messageJob.execute(jobExecutionContext);
    verify(notificationTransmitter, never()).send(any());
    verify(dataMessageTransmitter, times(2)).send(dataMessage);
  }

  @Test
  void testIsSilentOnEmailTransmissionException() throws MessageTransmitException {
    setNotificationType(MessageType.NOTIFICATION);
    doThrow(
        new EmailMessageTransmitException("Email exception"),
        new EmailMessageTransmitException("Email exception")
    ).when(notificationTransmitter).send(notification);
    assertDoesNotThrow(() -> messageJob.execute(jobExecutionContext));
  }

  @Test
  void testExplodesOnFcmTransmissionException() throws MessageTransmitException {
    setNotificationType(MessageType.NOTIFICATION);
    doThrow(
        new EmailMessageTransmitException("Email exception"),
        new FcmMessageTransmitException("Fcm exception")
    ).when(notificationTransmitter).send(notification);
    assertThrows(JobExecutionException.class, () -> messageJob.execute(jobExecutionContext));
  }

  private void setNotificationType(MessageType messageType) {
    when(jobDataMap.getString("messageType")).thenReturn(messageType.name());
  }

}
