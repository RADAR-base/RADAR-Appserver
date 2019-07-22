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

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.radarbase.appserver.converter.NotificationConverter;
import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.radarbase.appserver.dto.fcm.FcmNotifications;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.entity.Project;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.exception.AlreadyExistsException;
import org.radarbase.appserver.exception.InvalidNotificationDetailsException;
import org.radarbase.appserver.exception.InvalidUserDetailsException;
import org.radarbase.appserver.exception.NotFoundException;
import org.radarbase.appserver.repository.NotificationRepository;
import org.radarbase.appserver.repository.ProjectRepository;
import org.radarbase.appserver.repository.UserRepository;
import org.radarbase.appserver.service.scheduler.NotificationSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link Service} for interacting with the {@link Notification} {@link javax.persistence.Entity}
 * using the {@link NotificationRepository}.
 *
 * @author yatharthranjan
 */
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
@Service
public class FcmNotificationService implements NotificationService {

  // TODO Add option to specify a scheduling provider (default will be fcm)
  // TODO: Use strategy pattern for handling notifications for scheduling and adding to database

  private static final String INVALID_SUBJECT_ID_MESSAGE =
      "The supplied Subject ID is invalid. No user found. Please Create a User First.";
  private final transient NotificationRepository notificationRepository;
  private final transient UserRepository userRepository;
  private final transient ProjectRepository projectRepository;
  private final transient NotificationSchedulerService schedulerService;
  private final transient NotificationConverter notificationConverter;

  @Autowired
  public FcmNotificationService(
      NotificationRepository notificationRepository,
      UserRepository userRepository,
      ProjectRepository projectRepository,
      NotificationSchedulerService schedulerService,
      NotificationConverter notificationConverter) {
    this.notificationRepository = notificationRepository;
    this.userRepository = userRepository;
    this.projectRepository = projectRepository;
    this.schedulerService = schedulerService;
    this.notificationConverter = notificationConverter;
  }

  @Transactional(readOnly = true)
  public FcmNotifications getAllNotifications() {
    List<Notification> notifications = notificationRepository.findAll();
    return new FcmNotifications()
        .setNotifications(notificationConverter.entitiesToDtos(notifications));
  }

  @Transactional(readOnly = true)
  public FcmNotificationDto getNotificationById(long id) {
    Optional<Notification> notification = notificationRepository.findById(id);
    return notificationConverter.entityToDto(notification.orElseGet(Notification::new));
  }

  @Transactional(readOnly = true)
  public FcmNotifications getNotificationsBySubjectId(String subjectId) {
    Optional<User> user = this.userRepository.findBySubjectId(subjectId);
    if (user.isEmpty()) {
      throw new NotFoundException(INVALID_SUBJECT_ID_MESSAGE);
    }
    List<Notification> notifications = notificationRepository.findByUserId(user.get().getId());
    return new FcmNotifications()
        .setNotifications(notificationConverter.entitiesToDtos(notifications));
  }

  @Transactional(readOnly = true)
  public FcmNotifications getNotificationsByProjectIdAndSubjectId(
      String projectId, String subjectId) {
    User user = subjectAndProjectExistElseThrow(subjectId, projectId);

    List<Notification> notifications = notificationRepository.findByUserId(user.getId());
    return new FcmNotifications()
        .setNotifications(notificationConverter.entitiesToDtos(notifications));
  }

  @Transactional(readOnly = true)
  public FcmNotifications getNotificationsByProjectId(String projectId) {
    Optional<Project> project = projectRepository.findByProjectId(projectId);

    if (project.isEmpty()) {
      throw new NotFoundException("Project not found with projectId " + projectId);
    }
    List<User> users = this.userRepository.findByProjectId(project.get().getId());
    Set<Notification> notifications = new HashSet<>();
    users.stream()
        .map((User user) -> this.notificationRepository.findByUserId(user.getId()))
        .forEach(notifications::addAll);
    return new FcmNotifications()
        .setNotifications(notificationConverter.entitiesToDtos(notifications));
  }

  @Transactional(readOnly = true)
  public boolean checkIfNotificationExists(FcmNotificationDto notificationDto, String subjectId) {
    Optional<User> user = this.userRepository.findBySubjectId(subjectId);
    if (user.isEmpty()) {
      throw new NotFoundException(INVALID_SUBJECT_ID_MESSAGE);
    }
    Notification notification =
        notificationConverter.dtoToEntity(notificationDto).setUser(user.get());

    List<Notification> notifications = this.notificationRepository.findByUserId(user.get().getId());
    return notifications.contains(notification);
  }

  // TODO : WIP
  @Transactional(readOnly = true)
  public FcmNotifications getFilteredNotifications(
      String type,
      boolean delivered,
      int ttlSeconds,
      LocalDateTime startTime,
      LocalDateTime endTime,
      int limit) {
    return null;
  }

  @Transactional
  public FcmNotificationDto addNotification(
      FcmNotificationDto notificationDto, String subjectId, String projectId) {

    User user = subjectAndProjectExistElseThrow(subjectId, projectId);
    if (!notificationRepository
        .existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
            user.getId(),
            notificationDto.getSourceId(),
            notificationDto.getScheduledTime(),
            notificationDto.getTitle(),
            notificationDto.getBody(),
            notificationDto.getType(),
            notificationDto.getTtlSeconds())) {

      Notification notificationSaved =
          this.notificationRepository.save(
              notificationConverter.dtoToEntity(notificationDto).setUser(user));
      this.schedulerService.scheduleNotification(notificationSaved);
      user.getUsermetrics().setLastOpened(Instant.now());
      this.userRepository.save(user);
      return notificationConverter.entityToDto(notificationSaved);
    } else {
      throw new AlreadyExistsException(
          "The Notification Already exists. Please Use update endpoint", notificationDto);
    }
  }

  @Transactional
  public FcmNotificationDto updateNotification(
      FcmNotificationDto notificationDto, String subjectId, String projectId) {

    if (notificationDto.getId() == null) {
      throw new InvalidNotificationDetailsException(
          "ID must be supplied for updating the notification");
    }

    User user = subjectAndProjectExistElseThrow(subjectId, projectId);

    Optional<Notification> notification =
        this.notificationRepository.findById(notificationDto.getId());

    if (notification.isEmpty()) {
      throw new NotFoundException("Notification does not exist. Please create first");
    }

    Notification newNotification =
        notification
            .get()
            .setBody(notificationDto.getBody())
            .setScheduledTime(notificationDto.getScheduledTime())
            .setSourceId(notificationDto.getSourceId())
            .setTitle(notificationDto.getTitle())
            .setTtlSeconds(notificationDto.getTtlSeconds())
            .setType(notificationDto.getType())
            .setUser(user)
            .setFcmMessageId(String.valueOf(notificationDto.hashCode()));
    Notification notificationSaved = this.notificationRepository.save(newNotification);

    if (!notification.get().isDelivered()) {
      this.schedulerService.updateScheduledNotification(notificationSaved);
    }
    return notificationConverter.entityToDto(notificationSaved);
  }

  @Transactional
  public void removeNotificationsForUser(String projectId, String subjectId) {
    User user = subjectAndProjectExistElseThrow(subjectId, projectId);

    List<Notification> notifications = this.notificationRepository.findByUserId(user.getId());
    this.schedulerService.deleteScheduledNotifications(notifications);

    this.notificationRepository.deleteByUserId(user.getId());
  }

  @Transactional
  public void updateDeliveryStatus(String fcmMessageId, boolean isDelivered) {
    Optional<Notification> notification =
        this.notificationRepository.findByFcmMessageId(fcmMessageId);

    notification.ifPresentOrElse(
        (Notification n) -> {
          n.setDelivered(isDelivered);
          this.notificationRepository.save(n);
        },
        () -> {
          throw new InvalidNotificationDetailsException(
              "Notification with the provided FCM message ID does not exist.");
        });
  }

  @Transactional
  public void deleteNotificationByFcmMessageId(String fcmMessageId) {
    this.notificationRepository.deleteByFcmMessageId(fcmMessageId);
  }

  public void removeNotificationsForUserUsingFcmToken(String fcmToken) {
    Optional<User> user = this.userRepository.findByFcmToken(fcmToken);

    user.ifPresentOrElse(
        (User user1) -> {
          this.notificationRepository.deleteByUserId(user1.getId());
          User newUser = user1.setFcmToken("");
          this.userRepository.save(newUser);
        },
        () -> {
          throw new InvalidUserDetailsException("The user with the given Fcm Token does not exist");
        });
  }

  @Transactional
  public FcmNotifications addNotifications(
      FcmNotifications notificationDtos, String subjectId, String projectId) {
    final User user = subjectAndProjectExistElseThrow(subjectId, projectId);
    List<Notification> notifications = notificationRepository.findByUserId(user.getId());

    List<Notification> newNotifications =
        notificationDtos.getNotifications().stream()
            .map(notificationConverter::dtoToEntity)
            .filter(notification -> !notifications.contains(notification))
            .map(n -> n.setUser(user))
            .collect(Collectors.toList());

    List<Notification> savedNotifications = this.notificationRepository.saveAll(newNotifications);
    this.schedulerService.scheduleNotifications(savedNotifications);

    return new FcmNotifications()
        .setNotifications(notificationConverter.entitiesToDtos(savedNotifications));
  }

  private User subjectAndProjectExistElseThrow(String subjectId, String projectId) {
    Optional<Project> project = this.projectRepository.findByProjectId(projectId);
    if (project.isEmpty()) {
      throw new NotFoundException(
          "Project Id does not exist. Please create a project with the ID first");
    }

    Optional<User> user =
        this.userRepository.findBySubjectIdAndProjectId(subjectId, project.get().getId());
    if (user.isEmpty()) {
      throw new NotFoundException(INVALID_SUBJECT_ID_MESSAGE);
    }

    return user.get();
  }
}
