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
import java.util.*;
import java.util.stream.Collectors;

import org.radarbase.appserver.converter.NotificationConverter;
import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.radarbase.appserver.dto.fcm.FcmNotifications;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.entity.Project;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.event.state.MessageState;
import org.radarbase.appserver.event.state.dto.NotificationStateEventDto;
import org.radarbase.appserver.exception.InvalidNotificationDetailsException;
import org.radarbase.appserver.exception.InvalidUserDetailsException;
import org.radarbase.appserver.exception.NotFoundException;
import org.radarbase.appserver.exception.NotificationAlreadyExistsException;
import org.radarbase.appserver.repository.NotificationRepository;
import org.radarbase.appserver.repository.ProjectRepository;
import org.radarbase.appserver.repository.UserRepository;
import org.radarbase.appserver.service.scheduler.NotificationSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
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
    private final transient ApplicationEventPublisher notificationStateEventPublisher;

    @Autowired
    public FcmNotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            ProjectRepository projectRepository,
            NotificationSchedulerService schedulerService,
            NotificationConverter notificationConverter,
            ApplicationEventPublisher eventPublisher) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.schedulerService = schedulerService;
        this.notificationConverter = notificationConverter;
        this.notificationStateEventPublisher = eventPublisher;
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
        Notification notification = new Notification.NotificationBuilder(notificationConverter.dtoToEntity(notificationDto)).user(user.get()).build();

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
            FcmNotificationDto notificationDto, String subjectId, String projectId, boolean schedule) {

        User user = subjectAndProjectExistElseThrow(subjectId, projectId);
        Optional<Notification> notification = notificationRepository
                .findByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                        user.getId(),
                        notificationDto.getSourceId(),
                        notificationDto.getScheduledTime(),
                        notificationDto.getTitle(),
                        notificationDto.getBody(),
                        notificationDto.getType(),
                        notificationDto.getTtlSeconds());

        if (notification.isEmpty()) {
            Notification notificationSaved =
                    this.notificationRepository.saveAndFlush(
                            new Notification.NotificationBuilder(notificationConverter.dtoToEntity(notificationDto)).user(user).build());
            user.getUsermetrics().setLastOpened(Instant.now());
            this.userRepository.save(user);
            addNotificationStateEvent(
                    notificationSaved, MessageState.ADDED, notificationSaved.getCreatedAt().toInstant());
            if (schedule) {
                this.schedulerService.schedule(notificationSaved);
            }
            return notificationConverter.entityToDto(notificationSaved);
        } else {
            throw new NotificationAlreadyExistsException(
                    "The Notification Already exists. Please Use update endpoint",
                    notificationConverter.entityToDto(notification.get()));
        }
    }

    @Transactional
    public FcmNotificationDto addNotification(
            FcmNotificationDto notificationDto, String subjectId, String projectId) {

        User user = subjectAndProjectExistElseThrow(subjectId, projectId);
        Optional<Notification> notification = notificationRepository
                .findByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                        user.getId(),
                        notificationDto.getSourceId(),
                        notificationDto.getScheduledTime(),
                        notificationDto.getTitle(),
                        notificationDto.getBody(),
                        notificationDto.getType(),
                        notificationDto.getTtlSeconds());

        if (notification.isEmpty()) {
            Notification notificationSaved =
                    this.notificationRepository.saveAndFlush(
                            new Notification.NotificationBuilder(notificationConverter.dtoToEntity(notificationDto)).user(user).build());
            user.getUsermetrics().setLastOpened(Instant.now());
            this.userRepository.save(user);
            addNotificationStateEvent(
                    notificationSaved, MessageState.ADDED, notificationSaved.getCreatedAt().toInstant());
            this.schedulerService.schedule(notificationSaved);
            return notificationConverter.entityToDto(notificationSaved);
        } else {
            throw new NotificationAlreadyExistsException(
                    "The Notification Already exists. Please Use update endpoint",
                    notificationConverter.entityToDto(notification.get()));
        }
    }

    private void addNotificationStateEvent(
            Notification notification, MessageState state, Instant time) {
        if (notificationStateEventPublisher != null) {
            NotificationStateEventDto notificationStateEvent =
                    new NotificationStateEventDto(this, notification, state, null, time);
            notificationStateEventPublisher.publishEvent(notificationStateEvent);
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

        Notification newNotification = new Notification.NotificationBuilder(notification.get())
                .body(notificationDto.getBody())
                .scheduledTime(notificationDto.getScheduledTime())
                .sourceId(notificationDto.getSourceId())
                .title(notificationDto.getTitle())
                .ttlSeconds(notificationDto.getTtlSeconds())
                .type(notificationDto.getType())
                .user(user)
                .fcmMessageId(String.valueOf(notificationDto.hashCode()))
                .build();
        Notification notificationSaved = this.notificationRepository.saveAndFlush(newNotification);
        addNotificationStateEvent(
                notificationSaved, MessageState.UPDATED, notificationSaved.getUpdatedAt().toInstant());
        if (!notification.get().isDelivered()) {
            this.schedulerService.updateScheduled(notificationSaved);
        }
        return notificationConverter.entityToDto(notificationSaved);
    }

    @Transactional
    public FcmNotifications scheduleAllUserNotifications(String subjectId, String projectId) {

        User user = subjectAndProjectExistElseThrow(subjectId, projectId);
        List<Notification> notifications = notificationRepository.findByUserId(user.getId());
        this.schedulerService.scheduleMultiple(notifications);
        return new FcmNotifications()
                .setNotifications(notificationConverter.entitiesToDtos(notifications));
    }

    @Transactional
    public FcmNotificationDto scheduleNotification(String subjectId, String projectId, long notificationId) {

        User user = subjectAndProjectExistElseThrow(subjectId, projectId);
        Optional<Notification> notification = notificationRepository.findByIdAndUserId(notificationId, user.getId());
        if (notification.isEmpty()) {
            throw new NotFoundException(
                    "The Notification with Id "
                            + notificationId
                            + " does not exist in project "
                            + projectId
                            + " for user "
                            + subjectId);
        }
        this.schedulerService.schedule(notification.get());
        return notificationConverter.entityToDto(notification.get());
    }

    @Transactional
    public void removeNotificationsForUser(String projectId, String subjectId) {
        User user = subjectAndProjectExistElseThrow(subjectId, projectId);

        List<Notification> notifications = this.notificationRepository.findByUserId(user.getId());
        this.schedulerService.deleteScheduledMultiple(notifications);

        this.notificationRepository.deleteByUserId(user.getId());
    }

    @Transactional
    public void updateDeliveryStatus(String fcmMessageId, boolean isDelivered) {
        Optional<Notification> notification =
                this.notificationRepository.findByFcmMessageId(fcmMessageId);

        notification.ifPresentOrElse(
                (Notification n) -> {

                    Notification newNotif = new Notification.NotificationBuilder(n).delivered(isDelivered).build();
                    this.notificationRepository.save(newNotif);
                },
                () -> {
                    throw new InvalidNotificationDetailsException(
                            "Notification with the provided FCM message ID does not exist.");
                });
    }

    // TODO: Investigate if notifications can be marked in the state CANCELLED when deleted.
    @Transactional
    public void deleteNotificationByProjectIdAndSubjectIdAndNotificationId(String projectId, String subjectId, Long id) {
        User user = subjectAndProjectExistElseThrow(subjectId, projectId);
        Long userId = user.getId();

        if (this.notificationRepository.existsByIdAndUserId(id, userId)) {
            this.schedulerService.deleteScheduled(
                    this.notificationRepository.findByIdAndUserId(id, userId).get());
            this.notificationRepository.deleteByIdAndUserId(id, userId);
        } else
            throw new InvalidNotificationDetailsException(
                    "Notification with the provided ID does not exist.");
    }

    @Transactional
    public void removeNotificationsForUserUsingFcmToken(String fcmToken) {
        Optional<User> user = this.userRepository.findByFcmToken(fcmToken);

        user.ifPresentOrElse(
                (User user1) -> {
                    this.schedulerService.deleteScheduledMultiple(
                            this.notificationRepository.findByUserId(user1.getId()));

                    this.notificationRepository.deleteByUserId(user1.getId());
                },
                () -> {
                    throw new InvalidUserDetailsException("The user with the given Fcm Token does not exist");
                });
    }

    @Transactional
    public FcmNotifications addNotifications(
            FcmNotifications notificationDtos, String subjectId, String projectId, boolean schedule) {
        final User user = subjectAndProjectExistElseThrow(subjectId, projectId);
        List<Notification> notifications = notificationRepository.findByUserId(user.getId());

        List<Notification> newNotifications =
                notificationDtos.getNotifications().stream()
                        .map(notificationConverter::dtoToEntity)
                        .map(n -> new Notification.NotificationBuilder(n).user(user).build())
                        .filter(notification -> !notifications.contains(notification))
                        .collect(Collectors.toList());

        List<Notification> savedNotifications = this.notificationRepository.saveAll(newNotifications);
        this.notificationRepository.flush();
        savedNotifications.forEach(
                n -> addNotificationStateEvent(n, MessageState.ADDED, n.getCreatedAt().toInstant()));
        if (schedule) {
            this.schedulerService.scheduleMultiple(savedNotifications);
        }
        return new FcmNotifications()
                .setNotifications(notificationConverter.entitiesToDtos(savedNotifications));
    }

    @Transactional
    public List<Notification> addNotifications(List<Notification> notifications,User user) {
        List<Notification> newNotifications =
                notifications.stream()
                        .filter(notification ->
                            notificationRepository
                                    .findByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                                            user.getId(),
                                            notification.getSourceId(),
                                            notification.getScheduledTime(),
                                            notification.getTitle(),
                                            notification.getBody(),
                                            notification.getType(),
                                            notification.getTtlSeconds()).isPresent()
                        )
                        .collect(Collectors.toList());

        List<Notification> savedNotifications = this.notificationRepository.saveAll(newNotifications);
        this.notificationRepository.flush();
        savedNotifications.forEach(
                n -> addNotificationStateEvent(n, MessageState.ADDED, n.getCreatedAt().toInstant()));
        this.schedulerService.scheduleMultiple(savedNotifications);
        return savedNotifications;
    }

    @Transactional
    public FcmNotifications addNotifications(
            FcmNotifications notificationDtos, String subjectId, String projectId) {
        final User user = subjectAndProjectExistElseThrow(subjectId, projectId);
        List<Notification> notifications = notificationRepository.findByUserId(user.getId());

        List<Notification> newNotifications =
                notificationDtos.getNotifications().stream()
                        .map(notificationConverter::dtoToEntity)
                        .map(n -> new Notification.NotificationBuilder(n).user(user).build())
                        .filter(notification ->
                                notificationRepository
                                        .findByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                                                user.getId(),
                                                notification.getSourceId(),
                                                notification.getScheduledTime(),
                                                notification.getTitle(),
                                                notification.getBody(),
                                                notification.getType(),
                                                notification.getTtlSeconds()).isPresent()
                        )
                        .collect(Collectors.toList());

        List<Notification> savedNotifications = this.notificationRepository.saveAll(newNotifications);
        this.notificationRepository.flush();
        savedNotifications.forEach(
                n -> addNotificationStateEvent(n, MessageState.ADDED, n.getCreatedAt().toInstant()));
        this.schedulerService.scheduleMultiple(savedNotifications);
        return new FcmNotifications()
                .setNotifications(notificationConverter.entitiesToDtos(savedNotifications));
    }

    public User subjectAndProjectExistElseThrow(String subjectId, String projectId) {
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

    @Transactional(readOnly = true)
    public Notification getNotificationByProjectIdAndSubjectIdAndNotificationId(
            String projectId, String subjectId, long notificationId) {
        User user = subjectAndProjectExistElseThrow(subjectId, projectId);

        Optional<Notification> notification =
                notificationRepository.findByIdAndUserId(notificationId, user.getId());

        if (notification.isEmpty()) {
            throw new InvalidNotificationDetailsException(
                    "The Notification with Id "
                            + notificationId
                            + " does not exist in project "
                            + projectId
                            + " for user "
                            + subjectId);
        }
        return notification.get();
    }

    @Transactional(readOnly = true)
    public Notification getNotificationByMessageId(String messageId) {
        Optional<Notification> notification = this.notificationRepository.findByFcmMessageId(messageId);
        if (notification.isEmpty()) {
            throw new InvalidNotificationDetailsException(
                    "The Notification with FCM Message Id " + messageId + "does not exist.");
        }
        return notification.get();
    }

}
