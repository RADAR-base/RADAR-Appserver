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

import lombok.SneakyThrows;
import org.radarbase.appserver.converter.ConverterFactory;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.entity.Project;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.entity.UserMetrics;
import org.radarbase.appserver.exception.AlreadyExistsException;
import org.radarbase.appserver.exception.InvalidNotificationDetailsException;
import org.radarbase.appserver.exception.NotFoundException;
import org.radarbase.appserver.repository.NotificationRepository;
import org.radarbase.appserver.repository.ProjectRepository;
import org.radarbase.appserver.repository.UserRepository;
import org.radarbase.appserver.service.scheduler.NotificationSchedulerService;
import org.radarbase.fcm.dto.FcmNotificationDto;
import org.radarbase.fcm.dto.FcmNotifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * {@link Service} for interacting with the {@link Notification} {@link javax.persistence.Entity} using the
 * {@link NotificationRepository}.
 *
 * @author yatharthranjan
 */
@Service
public class FcmNotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private NotificationSchedulerService schedulerService;

    @Transactional(readOnly = true)
    public FcmNotifications getAllNotifications() {
        List<Notification> notifications = notificationRepository.findAll();
        return new FcmNotifications().setNotifications(ConverterFactory.getNotificationConverter().entitiesToDtos(notifications));
    }

    @Transactional(readOnly = true)
    public FcmNotificationDto getNotificationById(long id) {
        Optional<Notification> notification = notificationRepository.findById(id);
        return ConverterFactory.getNotificationConverter().entityToDto(notification.orElseGet(Notification::new));
    }

    @Transactional(readOnly = true)
    public FcmNotifications getNotificationsBySubjectId(String subjectId){
        Optional<User> user = this.userRepository.findBySubjectId(subjectId);
        if(user.isEmpty()) {
            throw new NotFoundException("The supplied subject ID is invalid. No user found. Please Create a User First.");
        }
        List<Notification> notifications = notificationRepository.findByUserId(user.get().getId());
        return new FcmNotifications().setNotifications(ConverterFactory.getNotificationConverter().entitiesToDtos(notifications));
    }

    @Transactional(readOnly = true)
    public FcmNotifications getNotificationsByProjectIdAndSubjectId(String projectId, String subjectId) {
        Optional<Project> project = projectRepository.findByProjectId(projectId);

        if(project.isEmpty()) {
            throw new NotFoundException("Project not found with projectId " + projectId);
        }

        Optional<User> user = this.userRepository.findBySubjectIdAndProjectId(subjectId, project.get().getId());
        if(user.isEmpty()) {
            throw new NotFoundException("The supplied subject ID is invalid. No user found in the project ID provided. Please Create a User First.");
        }

        List<Notification> notifications = notificationRepository.findByUserId(user.get().getId());
        return new FcmNotifications().setNotifications(ConverterFactory.getNotificationConverter().entitiesToDtos(notifications));
    }

    @Transactional(readOnly = true)
    public FcmNotifications getNotificationsByProjectId(String projectId) {
        Optional<Project> project = projectRepository.findByProjectId(projectId);

        if(project.isEmpty()) {
            throw new NotFoundException("Project not found with projectId " + projectId);
        }
        List<User> users = this.userRepository.findByProjectId(project.get().getId());
        Set<Notification> notifications = new HashSet<>();
        for(User user: users) {
            notifications.addAll(this.notificationRepository.findByUserId(user.getId()));
        }
        return new FcmNotifications().setNotifications(ConverterFactory.getNotificationConverter().entitiesToDtos(notifications));

    }

    @Transactional(readOnly = true)
    public boolean checkIfNotificationExists(FcmNotificationDto notificationDto, String subjectId) {
        Optional<User> user = this.userRepository.findBySubjectId(subjectId);
        if(user.isEmpty()) {
            throw new NotFoundException("The supplied subject ID is invalid. No user found. Please Create a User First.");
        }
        Notification notification = ConverterFactory.getNotificationConverter().dtoToEntity(notificationDto).setUser(user.get());

        List<Notification> notifications = this.notificationRepository.findByUserId(user.get().getId());
        return notifications.contains(notification);
    }

    //TODO : WIP
    @Transactional(readOnly = true)
    public FcmNotifications getFilteredNotifications(String type, boolean delivered,
                                                     int ttlSeconds, LocalDateTime startTime, LocalDateTime endTime) {
        return null;
    }

    // TODO Also update users lastOpened metric
    @Transactional
    public FcmNotificationDto addNotification(FcmNotificationDto notificationDto, String subjectId, String projectId) {
        Optional<Project> project = this.projectRepository.findByProjectId(projectId);
        if(project.isEmpty()) {
            throw new NotFoundException("Project Id does not exist. Please create a project with the ID first");
        }

        Optional<User> user = this.userRepository.findBySubjectIdAndProjectId(subjectId, project.get().getId());
        if(user.isEmpty()) {
            throw new NotFoundException("The supplied subject ID is invalid. No user found. Please Create a User First.");
        }
        if(!notificationRepository.existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                user.get().getId(), notificationDto.getSourceId(), notificationDto.getScheduledTime().toInstant(ZoneOffset.UTC),
                notificationDto.getTitle(), notificationDto.getBody(), notificationDto.getType(), notificationDto.getTtlSeconds()
        )) {
            Notification notification = ConverterFactory.getNotificationConverter().dtoToEntity(notificationDto).setUser(user.get());

            notification = this.notificationRepository.save(notification);
            this.schedulerService.scheduleNotification(notification);

            return ConverterFactory.getNotificationConverter().entityToDto(notification);

        } else {
            throw new AlreadyExistsException("The Notification Already exists. Please Use update endpoint", notificationDto);
        }
    }

    @SneakyThrows
    @Transactional
    public FcmNotificationDto addNotificationForced(FcmNotificationDto notificationDto, String fcmToken, String subjectId, String projectId) {
        Optional<Project> project = this.projectRepository.findByProjectId(projectId);
        Project newProject;
        if(project.isEmpty()) {
            Project project1 = new Project();
            newProject = this.projectRepository.save(project1.setProjectId(projectId));
        } else {
            newProject = project.get();
        }

        Optional<User> user = this.userRepository.findBySubjectIdAndProjectId(subjectId, newProject.getId());
        AtomicReference<User> newUser = new AtomicReference<>();

        user.ifPresentOrElse(newUser::set, () -> newUser.set(this.userRepository.save(
                new User().setEnrolmentDate(Instant.now())
                        .setFcmToken(fcmToken)
                        .setTimezone(0)
                        .setProject(newProject)
                        .setSubjectId(subjectId))
                .setUserMetrics(new UserMetrics(LocalDateTime.now().toInstant(ZoneOffset.UTC), null)
                )));

        if(!notificationRepository.existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                newUser.get().getId(), notificationDto.getSourceId(), notificationDto.getScheduledTime().toInstant(ZoneOffset.UTC),
                notificationDto.getTitle(), notificationDto.getBody(), notificationDto.getType(), notificationDto.getTtlSeconds()
        )) {
            Notification notification = ConverterFactory.getNotificationConverter().dtoToEntity(notificationDto).setUser(newUser.get());
            notification = this.notificationRepository.save(notification);

            this.schedulerService.scheduleNotification(notification);

            return ConverterFactory.getNotificationConverter().entityToDto(notification);
        } else {
            throw new AlreadyExistsException("The Notification Already exists. Please Use update endpoint", notificationDto);
        }
    }

    @Transactional
    public FcmNotificationDto updateNotification(FcmNotificationDto notificationDto, String subjectId, String projectId) {
        Optional<Project> project = this.projectRepository.findByProjectId(projectId);
        if(project.isEmpty()) {
            throw new NotFoundException("Project Id does not exist. Please create a project with the ID first");
        }

        Optional<User> user = this.userRepository.findBySubjectIdAndProjectId(subjectId, project.get().getId());
        if(user.isEmpty()) {
            throw new NotFoundException("The supplied subject ID is invalid. No user found. Please Create a User First.");
        }
        if(notificationDto.getId() == null) {
            throw new InvalidNotificationDetailsException("ID must be supplied for updating the notification");
        }
        Optional<Notification> notification = this.notificationRepository.findById(notificationDto.getId());

        if(notification.isEmpty()) {
            throw new NotFoundException("Notification does not exist. Please create first");
        }

        Notification newNotification = notification.get().setBody(notificationDto.getBody())
                .setScheduledTime(notificationDto.getScheduledTime().toInstant(ZoneOffset.UTC))
                .setSourceId(notificationDto.getSourceId()).setTitle(notificationDto.getTitle()).setTtlSeconds(notificationDto.getTtlSeconds())
                .setType(notificationDto.getType()).setUser(user.get()).setFcmMessageId(String.valueOf(notificationDto.hashCode()));
        newNotification = this.notificationRepository.save(newNotification);

        if(!notification.get().isDelivered()) {
            this.schedulerService.updateScheduledNotification(newNotification);
        }
        return ConverterFactory.getNotificationConverter().entityToDto(newNotification);
    }

    @Transactional
    public void removeNotificationsForUser(String projectId, String subjectId) {
        Optional<Project> project = projectRepository.findByProjectId(projectId);

        if(project.isEmpty()) {
            throw new NotFoundException("Project not found with projectId " + projectId);
        }

        Optional<User> user = this.userRepository.findBySubjectIdAndProjectId(subjectId, project.get().getId());
        if(user.isEmpty()) {
            throw new NotFoundException("The supplied subject ID is invalid. No user found in the project ID provided. Please Create a User First.");
        }

        List<Notification> notifications = this.notificationRepository.findByUserId(user.get().getId());
        this.schedulerService.deleteScheduledNotifications(notifications);

        this.notificationRepository.deleteByUserId(user.get().getId());
    }

    @Transactional
    public void updateDeliveryStatus(String fcmMessageId, boolean isDelivered) {
        Optional<Notification> notification = this.notificationRepository.findByFcmMessageId(fcmMessageId);

        notification.ifPresentOrElse(n -> {
            n.setDelivered(isDelivered);
            Notification newNotification = this.notificationRepository.save(n);
        }, () -> {
            throw new InvalidNotificationDetailsException("Notification with the provided FCM message ID does not exist.");
        });
    }

    @Transactional
    public void deleteNotificationByFcmMessageId(String fcmMessageId) {
        this.notificationRepository.deleteByFcmMessageId(fcmMessageId);
    }

    // TODO add batch adding of notifications. The scheduler function is already there.
}