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

package org.radarbase.appserver.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.radarbase.appserver.controller.FcmNotificationControllerTest.FCM_MESSAGE_ID;
import static org.radarbase.appserver.controller.FcmNotificationControllerTest.PROJECT_ID;
import static org.radarbase.appserver.controller.FcmNotificationControllerTest.USER_ID;
import static org.radarbase.appserver.controller.RadarUserControllerTest.FCM_TOKEN_1;
import static org.radarbase.appserver.repository.NotificationRepositoryTest.NOTIFICATION_BODY;
import static org.radarbase.appserver.repository.NotificationRepositoryTest.NOTIFICATION_FCM_MESSAGE_ID;
import static org.radarbase.appserver.repository.NotificationRepositoryTest.NOTIFICATION_SOURCE_ID;
import static org.radarbase.appserver.repository.NotificationRepositoryTest.NOTIFICATION_TITLE;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.radarbase.appserver.converter.NotificationConverter;
import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.radarbase.appserver.dto.fcm.FcmNotifications;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.entity.Project;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.entity.UserMetrics;
import org.radarbase.appserver.exception.NotFoundException;
import org.radarbase.appserver.repository.NotificationRepository;
import org.radarbase.appserver.repository.ProjectRepository;
import org.radarbase.appserver.repository.UserRepository;
import org.radarbase.appserver.service.scheduler.NotificationSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
@RunWith(SpringRunner.class)
@DataJpaTest
class FcmNotificationServiceTest {

  private static final String NEW_SUFFIX = "-new";
  private static final String NOTIFICATION_TITLE_1 = "Testing1";
  private static final String NOTIFICATION_TITLE_2 = "Testing2";
  private static final String NOTIFICATION_TITLE_3 = "Testing3";
  private static final String NOTIFICATION_TITLE_4 = "Testing4";
  private static User user;
  private final transient Instant scheduledTime = Instant.now().plus(Duration.ofSeconds(100));
  @MockBean private transient NotificationSchedulerService schedulerService;
  // TODO Make this generic when NotificationService interface is complete
  @Autowired private transient FcmNotificationService notificationService;
  @MockBean private transient NotificationRepository notificationRepository;
  @MockBean private transient UserRepository userRepository;
  @MockBean private transient ProjectRepository projectRepository;

  @BeforeEach
  void setUp() {
    setUpProjectAndUser();
    setUpNotification1And2();

    Project projectNew = new Project().setProjectId(PROJECT_ID).setId(2L);
    Mockito.when(projectRepository.save(new Project().setProjectId(PROJECT_ID + NEW_SUFFIX)))
        .thenReturn(projectNew);

    User userNew =
        new User()
            .setProject(projectNew)
            .setEnrolmentDate(Instant.now())
            .setFcmToken(FCM_TOKEN_1)
            .setSubjectId(USER_ID + NEW_SUFFIX)
            .setUserMetrics(
                new UserMetrics().setLastOpened(Instant.now()).setLastDelivered(Instant.now()))
            .setId(2L);

    Mockito.when(userRepository.save(Mockito.any())).thenReturn(userNew);

    Notification notification3 =
        new Notification()
            .setBody(NOTIFICATION_BODY)
            .setTitle(NOTIFICATION_TITLE_3)
            .setScheduledTime(scheduledTime)
            .setSourceId(NOTIFICATION_SOURCE_ID)
            .setFcmMessageId("1234567")
            .setTtlSeconds(86400)
            .setDelivered(false)
            .setUser(user)
            .setId(3L);
    notification3.setCreatedAt(new Date());

    Mockito.when(notificationRepository.save(notification3)).thenReturn(notification3);

    Mockito.when(notificationRepository.findById(3L)).thenReturn(Optional.of(notification3));

    Mockito.when(
            notificationRepository
                .existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                    1L,
                    NOTIFICATION_SOURCE_ID,
                    scheduledTime,
                    NOTIFICATION_TITLE_3,
                    NOTIFICATION_BODY,
                    null,
                    86400))
        .thenReturn(false);

    Notification notification4 =
        new Notification()
            .setBody(NOTIFICATION_BODY)
            .setTitle(NOTIFICATION_TITLE_4)
            .setScheduledTime(scheduledTime)
            .setSourceId(NOTIFICATION_SOURCE_ID)
            .setFcmMessageId("12345678")
            .setTtlSeconds(86400)
            .setDelivered(false)
            .setUser(userNew)
            .setId(4L);

    notification4.setCreatedAt(new Date());
    notification4.setUpdatedAt(new Date());
    Mockito.when(notificationRepository.save(notification4)).thenReturn(notification4);

    Mockito.when(notificationRepository.findById(4L)).thenReturn(Optional.of(notification4));

    Mockito.when(
            notificationRepository
                .existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                    2L,
                    NOTIFICATION_SOURCE_ID,
                    scheduledTime,
                    NOTIFICATION_TITLE_4,
                    NOTIFICATION_BODY,
                    null,
                    86400))
        .thenReturn(false);

    Notification notification5 =
        new Notification()
            .setBody(NOTIFICATION_BODY + " Updated")
            .setTitle("Testing 2 Updated")
            .setUser(user)
            .setScheduledTime(scheduledTime)
            .setSourceId(NOTIFICATION_SOURCE_ID)
            .setFcmMessageId(FCM_MESSAGE_ID)
            .setTtlSeconds(86400)
            .setDelivered(false)
            .setId(2L);

    notification5.setCreatedAt(new Date());
    notification5.setUpdatedAt(new Date());
    Mockito.when(notificationRepository.save(notification5)).thenReturn(notification5);

    Mockito.when(userRepository.findByFcmToken(FCM_TOKEN_1)).thenReturn(Optional.of(user));
  }

  private void setUpProjectAndUser() {
    // given
    Project project = new Project().setProjectId(PROJECT_ID).setId(1L);

    Mockito.when(projectRepository.findByProjectId(project.getProjectId()))
        .thenReturn(Optional.of(project));

    user =
        new User()
            .setFcmToken(FCM_TOKEN_1)
            .setEnrolmentDate(Instant.now())
            .setProject(project)
            .setTimezone(0d)
            .setLanguage("en")
            .setSubjectId(USER_ID)
            .setUserMetrics(
                new UserMetrics().setLastOpened(Instant.now()).setLastDelivered(Instant.now()))
            .setId(1L);

    Mockito.when(userRepository.findBySubjectId(user.getSubjectId())).thenReturn(Optional.of(user));

    Mockito.when(userRepository.findBySubjectIdAndProjectId(USER_ID, 1L))
        .thenReturn(Optional.of(user));

    Mockito.when(userRepository.findByProjectId(1L)).thenReturn(List.of(user));
  }

  private void setUpNotification1And2() {
    Notification notification1 =
        new Notification()
            .setUser(user)
            .setBody(NOTIFICATION_BODY)
            .setTitle(NOTIFICATION_TITLE)
            .setScheduledTime(scheduledTime)
            .setSourceId(NOTIFICATION_SOURCE_ID)
            .setFcmMessageId(NOTIFICATION_FCM_MESSAGE_ID)
            .setTtlSeconds(86400)
            .setDelivered(false)
            .setId(1L);

    Notification notification2 =
        new Notification()
            .setUser(user)
            .setBody(NOTIFICATION_BODY)
            .setTitle(NOTIFICATION_TITLE_2)
            .setScheduledTime(scheduledTime)
            .setSourceId(NOTIFICATION_SOURCE_ID)
            .setFcmMessageId(FCM_MESSAGE_ID)
            .setTtlSeconds(86400)
            .setDelivered(false)
            .setId(2L);

    Mockito.when(notificationRepository.findAll())
        .thenReturn(List.of(notification1, notification2));

    Mockito.when(notificationRepository.findByUserId(1L))
        .thenReturn(List.of(notification1, notification2));

    Mockito.when(notificationRepository.findByFcmMessageId(NOTIFICATION_FCM_MESSAGE_ID))
        .thenReturn(Optional.of(notification1));

    Mockito.when(notificationRepository.findByFcmMessageId(FCM_MESSAGE_ID))
        .thenReturn(Optional.of(notification2));

    Mockito.when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification1));

    Mockito.when(notificationRepository.findById(2L)).thenReturn(Optional.of(notification2));

    Mockito.when(
            notificationRepository
                .existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                    1L,
                    NOTIFICATION_SOURCE_ID,
                    scheduledTime,
                    NOTIFICATION_TITLE_1,
                    NOTIFICATION_BODY,
                    null,
                    86400))
        .thenReturn(true);

    Mockito.when(
            notificationRepository
                .existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                    1L,
                    NOTIFICATION_SOURCE_ID,
                    scheduledTime,
                    NOTIFICATION_TITLE_2,
                    NOTIFICATION_BODY,
                    null,
                    86400))
        .thenReturn(true);
  }

  @Test
  void getAllNotifications() {
    FcmNotifications fcmNotifications = notificationService.getAllNotifications();

    assertEquals(2, fcmNotifications.getNotifications().size());
    assertEquals(NOTIFICATION_TITLE, fcmNotifications.getNotifications().get(0).getTitle());
    assertEquals(NOTIFICATION_TITLE_2, fcmNotifications.getNotifications().get(1).getTitle());
  }

  @Test
  void getNotificationById() {
    FcmNotificationDto notificationDto = notificationService.getNotificationById(1L);

    assertEquals(NOTIFICATION_TITLE, notificationDto.getTitle());
    assertEquals(86400, notificationDto.getTtlSeconds());
    assertEquals(NOTIFICATION_SOURCE_ID, notificationDto.getSourceId());
  }

  @Test
  void getNotificationsBySubjectId() {
    FcmNotifications fcmNotifications = notificationService.getNotificationsBySubjectId(USER_ID);

    assertEquals(2, fcmNotifications.getNotifications().size());
    assertEquals(NOTIFICATION_TITLE, fcmNotifications.getNotifications().get(0).getTitle());
    assertEquals(NOTIFICATION_TITLE_2, fcmNotifications.getNotifications().get(1).getTitle());
  }

  @Test
  void getNotificationsByProjectIdAndSubjectId() {
    FcmNotifications notifications =
        notificationService.getNotificationsByProjectIdAndSubjectId(PROJECT_ID, USER_ID);

    assertEquals(2, notifications.getNotifications().size());
    assertEquals(NOTIFICATION_TITLE, notifications.getNotifications().get(0).getTitle());
    assertEquals(NOTIFICATION_TITLE_2, notifications.getNotifications().get(1).getTitle());
  }

  @Test
  void getNotificationsByProjectId() {
    FcmNotifications notifications = notificationService.getNotificationsByProjectId(PROJECT_ID);

    assertEquals(2, notifications.getNotifications().size());
    assertTrue(
        notifications.getNotifications().stream()
            .anyMatch(n -> n.getTitle().equals(NOTIFICATION_TITLE)));
    assertTrue(
        notifications.getNotifications().stream()
            .anyMatch(n -> n.getTitle().equals(NOTIFICATION_TITLE_2)));
  }

  @Test
  void checkIfNotificationExists() {
    FcmNotificationDto notificationDto =
        new FcmNotificationDto()
            .setBody(NOTIFICATION_BODY)
            .setTitle(NOTIFICATION_TITLE_2)
            .setScheduledTime(scheduledTime)
            .setSourceId(NOTIFICATION_SOURCE_ID)
            .setFcmMessageId(FCM_MESSAGE_ID)
            .setTtlSeconds(86400)
            .setDelivered(false);

    assertTrue(notificationService.checkIfNotificationExists(notificationDto, USER_ID));

    // A random notification should not exist
    assertFalse(
        notificationService.checkIfNotificationExists(
            new FcmNotificationDto().setScheduledTime(Instant.now()), USER_ID));
  }

  @Test
  @Disabled("Not implemented yet")
  void getFilteredNotifications() {
    assert (true);
  }

  @Test
  void addNotification() {
    final FcmNotificationDto notificationDto =
        new FcmNotificationDto()
            .setBody(NOTIFICATION_BODY)
            .setTitle(NOTIFICATION_TITLE_3)
            .setScheduledTime(scheduledTime)
            .setSourceId(NOTIFICATION_SOURCE_ID)
            .setFcmMessageId("1234567")
            .setTtlSeconds(86400)
            .setDelivered(false);

    notificationService.addNotification(notificationDto, USER_ID, PROJECT_ID);
    FcmNotificationDto savedNotification = notificationService.getNotificationById(3L);

    assertEquals(NOTIFICATION_TITLE_3, savedNotification.getTitle());
    assertEquals("1234567", savedNotification.getFcmMessageId());
  }

  @Test
  void addNotification_whenUserNotFound() {

    final FcmNotificationDto notificationDto =
        new FcmNotificationDto()
            .setBody(NOTIFICATION_BODY)
            .setTitle(NOTIFICATION_TITLE_4)
            .setScheduledTime(Instant.now())
            .setSourceId(NOTIFICATION_SOURCE_ID)
            .setFcmMessageId(FCM_MESSAGE_ID)
            .setTtlSeconds(86400)
            .setDelivered(false);

    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> notificationService.addNotification(notificationDto, USER_ID + "-2", PROJECT_ID));

    assertTrue(
        ex.getMessage()
            .contains(
                "The supplied Subject ID is invalid. No user found. Please Create a User First."));
  }

  @Test
  void addNotification_whenProjectNotFound() {

    final FcmNotificationDto notificationDto =
        new FcmNotificationDto()
            .setBody(NOTIFICATION_BODY)
            .setTitle(NOTIFICATION_TITLE_4)
            .setScheduledTime(Instant.now())
            .setSourceId(NOTIFICATION_SOURCE_ID)
            .setFcmMessageId(FCM_MESSAGE_ID)
            .setTtlSeconds(86400)
            .setDelivered(false);

    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () -> notificationService.addNotification(notificationDto, USER_ID, PROJECT_ID + "-2"));

    assertTrue(ex.getMessage().contains("Project Id does not exist"));
  }

  @Test
  void addNotifications() {

    FcmNotificationDto notificationDto =
        new FcmNotificationDto()
            .setBody(NOTIFICATION_BODY)
            .setTitle(NOTIFICATION_TITLE_4)
            .setScheduledTime(scheduledTime)
            .setSourceId(NOTIFICATION_SOURCE_ID)
            .setFcmMessageId("12345678")
            .setTtlSeconds(86400)
            .setDelivered(false);

    FcmNotificationDto notificationDto2 =
        new FcmNotificationDto()
            .setBody(NOTIFICATION_BODY + "2")
            .setTitle(NOTIFICATION_TITLE_4 + "3")
            .setScheduledTime(scheduledTime)
            .setSourceId(NOTIFICATION_SOURCE_ID)
            .setFcmMessageId("12345678")
            .setTtlSeconds(86400)
            .setDelivered(false);

    notificationService.addNotifications(
        new FcmNotifications().setNotifications(List.of(notificationDto, notificationDto2)),
        USER_ID,
        PROJECT_ID);

    FcmNotifications savedNotifications = notificationService.getNotificationsBySubjectId(USER_ID);

    assertEquals(2, savedNotifications.getNotifications().size());
    assertTrue(
        savedNotifications.getNotifications().stream()
            .anyMatch(notificationDto1 -> notificationDto1.getBody().equals(NOTIFICATION_BODY)));
  }

  @Test
  void updateNotification() {

    FcmNotificationDto notificationDto =
        new FcmNotificationDto()
            .setBody(NOTIFICATION_BODY + " Updated")
            .setTitle("Testing 2 Updated")
            .setScheduledTime(scheduledTime)
            .setSourceId(NOTIFICATION_SOURCE_ID)
            .setFcmMessageId(FCM_MESSAGE_ID)
            .setTtlSeconds(86400)
            .setDelivered(false)
            .setId(2L);

    notificationService.updateNotification(notificationDto, USER_ID, PROJECT_ID);

    assertEquals("Test notif Updated", notificationService.getNotificationById(2L).getBody());
    assertEquals("Testing 2 Updated", notificationService.getNotificationById(2L).getTitle());
  }

  // If does not throw CustomExceptionHandler then test is valid
  @Test
  void removeNotificationsForUser() {
    assertDoesNotThrow(() -> notificationService.removeNotificationsForUser(PROJECT_ID, USER_ID));
  }

  @Test
  void updateDeliveryStatus() {
    notificationService.updateDeliveryStatus("12345", true);

    assertTrue(notificationService.getNotificationById(1L).isDelivered());
  }

  // Directly calls the repository so no need to assert. Just check that no excpetion is thrown
  @Test
  void deleteNotificationByFcmMessageId() {
    assertDoesNotThrow(() -> notificationService.deleteNotificationByFcmMessageId(FCM_MESSAGE_ID));
  }

  // If does not throw CustomExceptionHandler then test is valid
  @Test
  void removeNotificationsForUserUsingFcmToken() {
    assertDoesNotThrow(
        () -> notificationService.removeNotificationsForUserUsingFcmToken(FCM_TOKEN_1));
  }

  @TestConfiguration
  static class FcmNotificationServiceTestContextConfiguration {
    private final transient NotificationConverter notificationConverter =
        new NotificationConverter();
    @Autowired private transient NotificationRepository notificationRepository;
    @Autowired private transient UserRepository userRepository;
    @Autowired private transient ProjectRepository projectRepository;
    @Autowired private transient NotificationSchedulerService schedulerService;
    @Autowired private transient ApplicationEventPublisher eventPublisher;

    @Bean
    public NotificationService notificationService() {
      return new FcmNotificationService(
          notificationRepository,
          userRepository,
          projectRepository,
          schedulerService,
          notificationConverter,
          eventPublisher);
    }
  }
}
