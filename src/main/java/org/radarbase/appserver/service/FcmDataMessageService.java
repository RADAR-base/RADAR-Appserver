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

import org.radarbase.appserver.converter.DataMessageConverter;
import org.radarbase.appserver.dto.fcm.FcmDataMessageDto;
import org.radarbase.appserver.dto.fcm.FcmDataMessages;
import org.radarbase.appserver.entity.DataMessage;
import org.radarbase.appserver.entity.Project;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.event.state.MessageState;
import org.radarbase.appserver.event.state.dto.DataMessageStateEventDto;
import org.radarbase.appserver.exception.AlreadyExistsException;
import org.radarbase.appserver.exception.InvalidNotificationDetailsException;
import org.radarbase.appserver.exception.InvalidUserDetailsException;
import org.radarbase.appserver.exception.NotFoundException;
import org.radarbase.appserver.repository.DataMessageRepository;
import org.radarbase.appserver.repository.ProjectRepository;
import org.radarbase.appserver.repository.UserRepository;
import org.radarbase.appserver.service.scheduler.MessageSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * {@link Service} for interacting with the {@link DataMessage} {@link jakarta.persistence.Entity}
 * using the {@link DataMessageRepository}.
 *
 * @author yatharthranjan
 */
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
@Service
public class FcmDataMessageService implements DataMessageService {

    // TODO Add option to specify a scheduling provider (default will be fcm)
    // TODO: Use strategy pattern for handling data messages for scheduling and adding to database

    private static final String INVALID_SUBJECT_ID_MESSAGE =
            "The supplied Subject ID is invalid. No user found. Please Create a User First.";
    private final transient DataMessageRepository dataMessageRepository;
    private final transient UserRepository userRepository;
    private final transient ProjectRepository projectRepository;
    private final transient MessageSchedulerService schedulerService;
    private final transient DataMessageConverter dataMessageConverter;
    private final transient ApplicationEventPublisher dataMessageStateEventPublisher;

    @Autowired
    public FcmDataMessageService(
            DataMessageRepository dataMessageRepository,
            UserRepository userRepository,
            ProjectRepository projectRepository,
            MessageSchedulerService schedulerService,
            DataMessageConverter dataMessageConverter,
            ApplicationEventPublisher eventPublisher) {
        this.dataMessageRepository = dataMessageRepository;
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.schedulerService = schedulerService;
        this.dataMessageConverter = dataMessageConverter;
        this.dataMessageStateEventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public FcmDataMessages getAllDataMessages() {
        List<DataMessage> dataMessages = dataMessageRepository.findAll();
        return new FcmDataMessages()
                .setDataMessages(dataMessageConverter.entitiesToDtos(dataMessages));
    }

    @Transactional(readOnly = true)
    public FcmDataMessageDto getDataMessageById(long id) {
        Optional<DataMessage> dataMessage = dataMessageRepository.findById(id);
        return dataMessageConverter.entityToDto(dataMessage.orElseGet(DataMessage::new));
    }

    @Transactional(readOnly = true)
    public FcmDataMessages getDataMessagesBySubjectId(String subjectId) {
        Optional<User> user = this.userRepository.findBySubjectId(subjectId);
        if (user.isEmpty()) {
            throw new NotFoundException(INVALID_SUBJECT_ID_MESSAGE);
        }
        List<DataMessage> dataMessages = dataMessageRepository.findByUserId(user.get().getId());
        return new FcmDataMessages()
                .setDataMessages(dataMessageConverter.entitiesToDtos(dataMessages));
    }

    @Transactional(readOnly = true)
    public FcmDataMessages getDataMessagesByProjectIdAndSubjectId(
            String projectId, String subjectId) {
        User user = subjectAndProjectExistElseThrow(subjectId, projectId);

        List<DataMessage> dataMessages = dataMessageRepository.findByUserId(user.getId());
        return new FcmDataMessages()
                .setDataMessages(dataMessageConverter.entitiesToDtos(dataMessages));
    }

    @Transactional(readOnly = true)
    public FcmDataMessages getDataMessagesByProjectId(String projectId) {
        Project project = projectRepository.findByProjectId(projectId);

        if (project == null) {
            throw new NotFoundException("Project not found with projectId " + projectId);
        }
        List<User> users = this.userRepository.findByProjectId(project.getId());
        Set<DataMessage> dataMessages = new HashSet<>();
        users.stream()
                .map((User user) -> this.dataMessageRepository.findByUserId(user.getId()))
                .forEach(dataMessages::addAll);
        return new FcmDataMessages()
                .setDataMessages(dataMessageConverter.entitiesToDtos(dataMessages));
    }

    @Transactional(readOnly = true)
    public boolean checkIfDataMessageExists(FcmDataMessageDto dataMessageDto, String subjectId) {
        Optional<User> user = this.userRepository.findBySubjectId(subjectId);
        if (user.isEmpty()) {
            throw new NotFoundException(INVALID_SUBJECT_ID_MESSAGE);
        }
        DataMessage dataMessage = new DataMessage.DataMessageBuilder(dataMessageConverter.dtoToEntity(dataMessageDto)).user(user.get()).build();

        List<DataMessage> dataMessages = this.dataMessageRepository.findByUserId(user.get().getId());
        return dataMessages.contains(dataMessage);
    }

    // TODO : WIP
    @Transactional(readOnly = true)
    public FcmDataMessages getFilteredDataMessages(
            String type,
            boolean delivered,
            int ttlSeconds,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int limit) {
        return null;
    }

    @Transactional
    public FcmDataMessageDto addDataMessage(
            FcmDataMessageDto dataMessageDto, String subjectId, String projectId) {

        User user = subjectAndProjectExistElseThrow(subjectId, projectId);
        if (!dataMessageRepository
                .existsByUserIdAndSourceIdAndScheduledTimeAndTtlSeconds(
                        user.getId(),
                        dataMessageDto.getSourceId(),
                        dataMessageDto.getScheduledTime(),
                        dataMessageDto.getTtlSeconds())) {

            DataMessage dataMessageSaved =
                    this.dataMessageRepository.saveAndFlush(
                            new DataMessage.DataMessageBuilder(dataMessageConverter.dtoToEntity(dataMessageDto)).user(user).build());
            user.getUsermetrics().setLastOpened(Instant.now());
            this.userRepository.save(user);
            addDataMessageStateEvent(
                    dataMessageSaved, MessageState.ADDED, dataMessageSaved.getCreatedAt().toInstant());
            this.schedulerService.schedule(dataMessageSaved);
            return dataMessageConverter.entityToDto(dataMessageSaved);
        } else {
            throw new AlreadyExistsException(
                    "The Data Message Already exists. Please Use update endpoint", dataMessageDto);
        }
    }

    private void addDataMessageStateEvent(
            DataMessage dataMessage, MessageState state, Instant time) {
        if (dataMessageStateEventPublisher != null) {
            DataMessageStateEventDto dataMessageStateEvent =
                    new DataMessageStateEventDto(this, dataMessage, state, null, time);
            dataMessageStateEventPublisher.publishEvent(dataMessageStateEvent);
        }
    }

    @Transactional
    public FcmDataMessageDto updateDataMessage(
            FcmDataMessageDto dataMessageDto, String subjectId, String projectId) {

        if (dataMessageDto.getId() == null) {
            throw new InvalidNotificationDetailsException(
                    "ID must be supplied for updating the data message");
        }

        User user = subjectAndProjectExistElseThrow(subjectId, projectId);

        Optional<DataMessage> dataMessage =
                this.dataMessageRepository.findById(dataMessageDto.getId());

        if (dataMessage.isEmpty()) {
            throw new NotFoundException("Data message does not exist. Please create first");
        }

        DataMessage newDataMessage = new DataMessage.DataMessageBuilder(dataMessage.get())
                .scheduledTime(dataMessageDto.getScheduledTime())
                .sourceId(dataMessageDto.getSourceId())
                .ttlSeconds(dataMessageDto.getTtlSeconds())
                .user(user)
                .fcmMessageId(String.valueOf(dataMessageDto.hashCode()))
                .build();
        DataMessage dataMessageSaved = this.dataMessageRepository.saveAndFlush(newDataMessage);
        addDataMessageStateEvent(
                dataMessageSaved, MessageState.UPDATED, dataMessageSaved.getUpdatedAt().toInstant());
        if (!dataMessage.get().isDelivered()) {
            this.schedulerService.updateScheduled(dataMessageSaved);
        }
        return dataMessageConverter.entityToDto(dataMessageSaved);
    }

    @Transactional
    public void removeDataMessagesForUser(String projectId, String subjectId) {
        User user = subjectAndProjectExistElseThrow(subjectId, projectId);

        List<DataMessage> dataMessages = this.dataMessageRepository.findByUserId(user.getId());
        this.schedulerService.deleteScheduledMultiple(dataMessages);

        this.dataMessageRepository.deleteByUserId(user.getId());
    }

    @Transactional
    public void updateDeliveryStatus(String fcmMessageId, boolean isDelivered) {
        Optional<DataMessage> dataMessage =
                this.dataMessageRepository.findByFcmMessageId(fcmMessageId);

        dataMessage.ifPresentOrElse(
                (DataMessage d) -> {

                    DataMessage newDataMessage = new DataMessage.DataMessageBuilder(d).delivered(isDelivered).build();
                    this.dataMessageRepository.save(newDataMessage);
                },
                () -> {
                    throw new InvalidNotificationDetailsException(
                            "Data message with the provided FCM message ID does not exist.");
                });
    }

    // TODO: Investigate if data messages/notifications can be marked in the state CANCELLED when deleted.
    @Transactional
    public void deleteDataMessageByProjectIdandSubjectIdAndDataMessageId(String projectId, String subjectId, long id) {
        User user = subjectAndProjectExistElseThrow(subjectId, projectId);

        if (this.dataMessageRepository.existsByIdAndUserId(id, user.getId()))
            this.dataMessageRepository.deleteByIdAndUserId(id, user.getId());
        else
            throw new InvalidNotificationDetailsException(
                    "Data message with the provided ID does not exist.");
    }

    @Transactional
    public void removeDataMessagesForUserUsingFcmToken(String fcmToken) {
        Optional<User> user = this.userRepository.findByFcmToken(fcmToken);

        user.ifPresentOrElse(
                (User user1) -> {
                    this.dataMessageRepository.deleteByUserId(user1.getId());
          /*User newUser = user1.setFcmToken("");
          this.userRepository.save(newUser);*/
                },
                () -> {
                    throw new InvalidUserDetailsException("The user with the given Fcm Token does not exist");
                });
    }

    @Transactional
    public FcmDataMessages addDataMessages(
            FcmDataMessages dataMessageDtos, String subjectId, String projectId) {
        final User user = subjectAndProjectExistElseThrow(subjectId, projectId);
        List<DataMessage> dataMessages = dataMessageRepository.findByUserId(user.getId());

        List<DataMessage> newDataMessages =
                dataMessageDtos.getDataMessages().stream()
                        .map(dataMessageConverter::dtoToEntity)
                        .map(d -> new DataMessage.DataMessageBuilder(d).user(user).build())
                        .filter(dataMessage -> !dataMessages.contains(dataMessage))
                        .collect(Collectors.toList());

        List<DataMessage> savedDataMessages = this.dataMessageRepository.saveAll(newDataMessages);
        this.dataMessageRepository.flush();

        savedDataMessages.forEach(
                n -> addDataMessageStateEvent(n, MessageState.ADDED, n.getCreatedAt().toInstant())
        );
        this.schedulerService.scheduleMultiple(savedDataMessages);
        return new FcmDataMessages()
                .setDataMessages(dataMessageConverter.entitiesToDtos(savedDataMessages));
    }

    public User subjectAndProjectExistElseThrow(String subjectId, String projectId) {
        Project project = this.projectRepository.findByProjectId(projectId);
        if (project == null || project.getId() == null) {
            throw new NotFoundException(
                    "Project Id does not exist. Please create a project with the ID first");
        }

        Optional<User> user =
                this.userRepository.findBySubjectIdAndProjectId(subjectId, project.getId());
        if (user.isEmpty()) {
            throw new NotFoundException(INVALID_SUBJECT_ID_MESSAGE);
        }

        return user.get();
    }

    @Transactional(readOnly = true)
    public DataMessage getDataMessageByProjectIdAndSubjectIdAndDataMessageId(
            String projectId, String subjectId, long dataMessageId) {
        User user = subjectAndProjectExistElseThrow(subjectId, projectId);

        Optional<DataMessage> dataMessage =
                dataMessageRepository.findByIdAndUserId(dataMessageId, user.getId());

        if (dataMessage.isEmpty()) {
            throw new InvalidNotificationDetailsException(
                    "The Data message with Id "
                            + dataMessageId
                            + " does not exist in project "
                            + projectId
                            + " for user "
                            + subjectId);
        }
        return dataMessage.get();
    }

    @Transactional(readOnly = true)
    public DataMessage getDataMessageByMessageId(String messageId) {
        Optional<DataMessage> dataMessage = this.dataMessageRepository.findByFcmMessageId(messageId);
        if (dataMessage.isEmpty()) {
            throw new InvalidNotificationDetailsException(
                    "The Data message with FCM Message Id " + messageId + "does not exist.");
        }
        return dataMessage.get();
    }
}
