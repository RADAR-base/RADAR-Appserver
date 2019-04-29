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

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.radarbase.appserver.converter.NotificationConverter;
import org.radarbase.appserver.converter.UserConverter;
import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.radarbase.appserver.dto.fcm.FcmNotifications;
import org.radarbase.appserver.dto.fcm.FcmUserDto;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.entity.Project;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.exception.NotFoundException;
import org.radarbase.appserver.repository.NotificationRepository;
import org.radarbase.appserver.repository.ProjectRepository;
import org.radarbase.appserver.repository.UserRepository;
import org.radarbase.appserver.service.scheduler.NotificationSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
class FcmNotificationServiceTest {

  private final Instant scheduledTime = Instant.now().plus(Duration.ofSeconds(100));
  @MockBean NotificationSchedulerService schedulerService;

  // TODO Make this generic when NotificationService interface is complete
  @Autowired private FcmNotificationService notificationService;
  @MockBean private NotificationRepository notificationRepository;
  @MockBean private UserRepository userRepository;
  @MockBean private ProjectRepository projectRepository;

  @BeforeEach
  void setUp() {
    // given
    Project project = new Project().setProjectId("test-project").setId(1L);

    Mockito.when(projectRepository.findByProjectId(project.getProjectId()))
        .thenReturn(Optional.of(project));

    Project projectNew = new Project().setProjectId("test-project").setId(2L);

    Mockito.when(projectRepository.save(new Project().setProjectId("test-project-new")))
        .thenReturn(projectNew);

    User user =
        new User()
            .setFcmToken("xxxx")
            .setEnrolmentDate(Instant.now())
            .setProject(project)
            .setTimezone(0d)
            .setLanguage("en")
            .setSubjectId("test-user")
            .setId(1L);

    Mockito.when(userRepository.findBySubjectId(user.getSubjectId())).thenReturn(Optional.of(user));

    Mockito.when(userRepository.findBySubjectIdAndProjectId("test-user", 1L))
        .thenReturn(Optional.of(user));

    Mockito.when(userRepository.findByProjectId(1L)).thenReturn(List.of(user));

    User userNew =
        new User()
            .setProject(projectNew)
            .setEnrolmentDate(Instant.now())
            .setFcmToken("xxxx")
            .setSubjectId("test-user-new")
            .setId(2L);

    Mockito.when(userRepository.save(Mockito.any())).thenReturn(userNew);

    Notification notification1 =
        new Notification()
            .setUser(user)
            .setBody("Test notif")
            .setTitle("Testing1")
            .setScheduledTime(scheduledTime)
            .setSourceId("test")
            .setFcmMessageId("12345")
            .setTtlSeconds(86400)
            .setDelivered(false)
            .setId(1L);

    Notification notification2 =
        new Notification()
            .setUser(user)
            .setBody("Test notif")
            .setTitle("Testing2")
            .setScheduledTime(scheduledTime)
            .setSourceId("test")
            .setFcmMessageId("123456")
            .setTtlSeconds(86400)
            .setDelivered(false)
            .setId(2L);

    Mockito.when(notificationRepository.findAll())
        .thenReturn(List.of(notification1, notification2));

    Mockito.when(notificationRepository.findByUserId(1L))
        .thenReturn(List.of(notification1, notification2));

    Mockito.when(notificationRepository.findByFcmMessageId("12345"))
        .thenReturn(Optional.of(notification1));

    Mockito.when(notificationRepository.findByFcmMessageId("123456"))
        .thenReturn(Optional.of(notification2));

    Mockito.when(notificationRepository.findById(1L)).thenReturn(Optional.of(notification1));

    Mockito.when(notificationRepository.findById(2L)).thenReturn(Optional.of(notification2));

    Mockito.when(
            notificationRepository
                .existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                    1L, "test", scheduledTime, "Testing1", "Test notif", null, 86400))
        .thenReturn(true);

    Mockito.when(
            notificationRepository
                .existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                    1L, "test", scheduledTime, "Testing2", "Test notif", null, 86400))
        .thenReturn(true);

    Notification notification3 =
        new Notification()
            .setBody("Test notif")
            .setTitle("Testing3")
            .setScheduledTime(scheduledTime)
            .setSourceId("test")
            .setFcmMessageId("1234567")
            .setTtlSeconds(86400)
            .setDelivered(false)
            .setUser(user)
            .setId(3L);

    Mockito.when(notificationRepository.save(notification3)).thenReturn(notification3);

    Mockito.when(notificationRepository.findById(3L)).thenReturn(Optional.of(notification3));

    Mockito.when(
            notificationRepository
                .existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                    1L, "test", scheduledTime, "Testing3", "Test notif", null, 86400))
        .thenReturn(false);

    Notification notification4 =
        new Notification()
            .setBody("Test notif")
            .setTitle("Testing4")
            .setScheduledTime(scheduledTime)
            .setSourceId("test")
            .setFcmMessageId("12345678")
            .setTtlSeconds(86400)
            .setDelivered(false)
            .setUser(userNew)
            .setId(4L);

    Mockito.when(notificationRepository.save(notification4)).thenReturn(notification4);

    Mockito.when(notificationRepository.findById(4L)).thenReturn(Optional.of(notification4));

    Mockito.when(
            notificationRepository
                .existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                    2L, "test", scheduledTime, "Testing4", "Test notif", null, 86400))
        .thenReturn(false);

    Notification notification5 =
        new Notification()
            .setBody("Test notif Updated")
            .setTitle("Testing 2 Updated")
            .setUser(user)
            .setScheduledTime(scheduledTime)
            .setSourceId("test")
            .setFcmMessageId("123456")
            .setTtlSeconds(86400)
            .setDelivered(false)
            .setId(2L);

    Mockito.when(notificationRepository.save(notification5)).thenReturn(notification5);

    Mockito.when(userRepository.findByFcmToken("xxxx")).thenReturn(Optional.of(user));
  }

  @Test
  void getAllNotifications() {
    FcmNotifications fcmNotifications = notificationService.getAllNotifications();

    assertEquals(2, fcmNotifications.getNotifications().size());
    assertEquals("Testing1", fcmNotifications.getNotifications().get(0).getTitle());
    assertEquals("Testing2", fcmNotifications.getNotifications().get(1).getTitle());
  }

  @Test
  void getNotificationById() {
    FcmNotificationDto notificationDto = notificationService.getNotificationById(1L);

    assertEquals("Testing1", notificationDto.getTitle());
    assertEquals(86400, notificationDto.getTtlSeconds());
    assertEquals("test", notificationDto.getSourceId());
  }

  @Test
  void getNotificationsBySubjectId() {
    FcmNotifications fcmNotifications =
        notificationService.getNotificationsBySubjectId("test-user");

    assertEquals(2, fcmNotifications.getNotifications().size());
    assertEquals("Testing1", fcmNotifications.getNotifications().get(0).getTitle());
    assertEquals("Testing2", fcmNotifications.getNotifications().get(1).getTitle());
  }

  @Test
  void getNotificationsByProjectIdAndSubjectId() {
    FcmNotifications notifications =
        notificationService.getNotificationsByProjectIdAndSubjectId("test-project", "test-user");

    assertEquals(2, notifications.getNotifications().size());
    assertEquals("Testing1", notifications.getNotifications().get(0).getTitle());
    assertEquals("Testing2", notifications.getNotifications().get(1).getTitle());
  }

  @Test
  void getNotificationsByProjectId() {
    FcmNotifications notifications =
        notificationService.getNotificationsByProjectId("test-project");

    assertEquals(2, notifications.getNotifications().size());
    assertTrue(
        notifications.getNotifications().stream().anyMatch(n -> n.getTitle().equals("Testing1")));
    assertTrue(
        notifications.getNotifications().stream().anyMatch(n -> n.getTitle().equals("Testing2")));
  }

  @Test
  void checkIfNotificationExists() {
    FcmNotificationDto notificationDto =
        new FcmNotificationDto()
            .setBody("Test notif")
            .setTitle("Testing2")
            .setScheduledTime(scheduledTime)
            .setSourceId("test")
            .setFcmMessageId("123456")
            .setTtlSeconds(86400)
            .setDelivered(false);

    assertTrue(notificationService.checkIfNotificationExists(notificationDto, "test-user"));

    // A random notification should not exist
    assertFalse(
        notificationService.checkIfNotificationExists(
            new FcmNotificationDto().setScheduledTime(Instant.now()), "test-user"));
  }

  @Test
  @Disabled("Not implemented yet")
  void getFilteredNotifications() {
    assert (true);
  }

  @Test
  void addNotification() {
    FcmNotificationDto notificationDto =
        new FcmNotificationDto()
            .setBody("Test notif")
            .setTitle("Testing3")
            .setScheduledTime(scheduledTime)
            .setSourceId("test")
            .setFcmMessageId("1234567")
            .setTtlSeconds(86400)
            .setDelivered(false);

    notificationService.addNotification(notificationDto, "test-user", "test-project");
    FcmNotificationDto savedNotification = notificationService.getNotificationById(3L);

    assertEquals("Testing3", savedNotification.getTitle());
    assertEquals("1234567", savedNotification.getFcmMessageId());
  }

  @Test
  void addNotification_whenUserNotFound() {

    FcmNotificationDto notificationDto =
        new FcmNotificationDto()
            .setBody("Test notif")
            .setTitle("Testing4")
            .setScheduledTime(Instant.now())
            .setSourceId("test")
            .setFcmMessageId("123456")
            .setTtlSeconds(86400)
            .setDelivered(false);

    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () ->
                notificationService.addNotification(
                    notificationDto, "test-user-2", "test-project"));

    assertTrue(ex.getMessage().contains("The supplied subject ID is invalid. No user found."));
  }

  @Test
  void addNotification_whenProjectNotFound() {

    FcmNotificationDto notificationDto =
        new FcmNotificationDto()
            .setBody("Test notif")
            .setTitle("Testing4")
            .setScheduledTime(Instant.now())
            .setSourceId("test")
            .setFcmMessageId("123456")
            .setTtlSeconds(86400)
            .setDelivered(false);

    NotFoundException ex =
        assertThrows(
            NotFoundException.class,
            () ->
                notificationService.addNotification(
                    notificationDto, "test-project-2", "test-user"));

    assertTrue(ex.getMessage().contains("Project Id does not exist"));
  }

  @Test
  void addNotificationForced() {

    FcmNotificationDto notificationDto =
        new FcmNotificationDto()
            .setBody("Test notif")
            .setTitle("Testing4")
            .setScheduledTime(scheduledTime)
            .setSourceId("test")
            .setFcmMessageId("12345678")
            .setTtlSeconds(86400)
            .setDelivered(false);

    FcmUserDto userDto =
        new FcmUserDto()
            .setProjectId("test-project-new")
            .setEnrolmentDate(Instant.now())
            .setFcmToken("xxxx")
            .setSubjectId("test-user-new");

    // Note we are using a project name and user name that does not exist yet.
    notificationService.addNotificationForced(notificationDto, userDto, "test-project-new");

    FcmNotificationDto savedNotification = notificationService.getNotificationById(4L);

    assertEquals("Testing4", savedNotification.getTitle());
    assertEquals("12345678", savedNotification.getFcmMessageId());
  }

  @Test
  void updateNotification() {

    FcmNotificationDto notificationDto =
        new FcmNotificationDto()
            .setBody("Test notif Updated")
            .setTitle("Testing 2 Updated")
            .setScheduledTime(scheduledTime)
            .setSourceId("test")
            .setFcmMessageId("123456")
            .setTtlSeconds(86400)
            .setDelivered(false)
            .setId(2L);

    notificationService.updateNotification(notificationDto, "test-user", "test-project");

    assertEquals("Test notif Updated", notificationService.getNotificationById(2L).getBody());
    assertEquals("Testing 2 Updated", notificationService.getNotificationById(2L).getTitle());
  }

  // If does not throw CustomExceptionHandler then test is valid
  @Test
  void removeNotificationsForUser() {
    assertDoesNotThrow(
        () -> notificationService.removeNotificationsForUser("test-project", "test-user"));
  }

  @Test
  void updateDeliveryStatus() {
    notificationService.updateDeliveryStatus("12345", true);

    assertTrue(notificationService.getNotificationById(1L).isDelivered());
  }

  // Directly calls the repository so no need to assert. Just check that no excpetion is thrown
  @Test
  void deleteNotificationByFcmMessageId() {
    assertDoesNotThrow(() -> notificationService.deleteNotificationByFcmMessageId("123456"));
  }

  // If does not throw CustomExceptionHandler then test is valid
  @Test
  void removeNotificationsForUserUsingFcmToken() {
    assertDoesNotThrow(() -> notificationService.removeNotificationsForUserUsingFcmToken("xxxx"));
  }

  @TestConfiguration
  static class FcmNotificationServiceTestContextConfiguration {
    private final NotificationConverter notificationConverter = new NotificationConverter();
    private final UserConverter userConverter = new UserConverter();
    @Autowired private NotificationRepository notificationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private NotificationSchedulerService schedulerService;

    @Bean
    public NotificationService notificationService() {
      return new FcmNotificationService(
          notificationRepository,
          userRepository,
          projectRepository,
          schedulerService,
          notificationConverter,
          userConverter);
    }
  }
}
