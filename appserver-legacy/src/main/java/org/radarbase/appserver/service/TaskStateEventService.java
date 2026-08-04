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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.radarbase.appserver.dto.TaskStateEventDto;
import org.radarbase.appserver.entity.Task;
import org.radarbase.appserver.entity.TaskStateEvent;
import org.radarbase.appserver.event.state.TaskState;
import org.radarbase.appserver.repository.TaskStateEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.SizeLimitExceededException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class TaskStateEventService {

    private static final Set<TaskState> EXTERNAL_EVENTS;
    private static final int MAX_NUMBER_OF_STATES = 20;

    static {
        EXTERNAL_EVENTS =
                Set.of(
                        TaskState.COMPLETED,
                        TaskState.UNKNOWN,
                        TaskState.ERRORED);
    }

    private final transient TaskStateEventRepository taskStateEventRepository;
    private final transient TaskService taskService;
    private final transient FcmNotificationService notificationService;

    private final transient ApplicationEventPublisher taskApplicationEventPublisher;
    private final transient ObjectMapper objectMapper;

    @Autowired
    public TaskStateEventService(
            TaskStateEventRepository taskStateEventRepository,
            TaskService taskService,
            FcmNotificationService notificationService,
            ApplicationEventPublisher taskApplicationEventPublisher,
            ObjectMapper objectMapper) {
        this.taskStateEventRepository = taskStateEventRepository;
        this.taskService = taskService;
        this.notificationService = notificationService;
        this.taskApplicationEventPublisher = taskApplicationEventPublisher;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void addTaskStateEvent(TaskStateEvent taskStateEvent) {
        taskStateEventRepository.save(taskStateEvent);
        taskService.updateTaskStatus(taskStateEvent.getTask(), taskStateEvent.getState());
        if (taskStateEvent.getState().equals(TaskState.COMPLETED)) {
            notificationService.deleteNotificationsByTaskId(taskStateEvent.getTask());
        }
    }

    @Transactional(readOnly = true)
    public List<TaskStateEventDto> getTaskStateEvents(
            String projectId, String subjectId, long taskId) {
        Task task = taskService.getTaskById(taskId);
        List<TaskStateEvent> stateEvents =
                taskStateEventRepository.findByTaskId(taskId);
        return stateEvents.stream()
                .map(
                        ns ->
                                new TaskStateEventDto(
                                        ns.getId(),
                                        task.getId(),
                                        ns.getState(),
                                        ns.getTime(),
                                        ns.getAssociatedInfo()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskStateEventDto> getTaskStateEventsByTaskId(
            long taskId) {
        List<TaskStateEvent> stateEvents =
                taskStateEventRepository.findByTaskId(taskId);
        return stateEvents.stream()
                .map(
                        ns ->
                                new TaskStateEventDto(
                                        ns.getId(),
                                        ns.getTask().getId(),
                                        ns.getState(),
                                        ns.getTime(),
                                        ns.getAssociatedInfo()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void publishNotificationStateEventExternal(
            String projectId,
            String subjectId,
            long taskId,
            TaskStateEventDto taskStateEventDto)
            throws SizeLimitExceededException {

        checkState(taskId, taskStateEventDto.getState());
        Task task = this.taskService.getTaskById(taskId);

        Map<String, String> additionalInfo = null;
        if (!taskStateEventDto.getAssociatedInfo().isEmpty()) {
            try {
                additionalInfo =
                        objectMapper.readValue(
                                taskStateEventDto.getAssociatedInfo(),
                                new TypeReference<Map<String, String>>() {
                                });
            } catch (IOException exc) {
                throw new IllegalStateException(
                        "Cannot convert additionalInfo to Map<String, String>. Please check its format.");
            }
        }

        org.radarbase.appserver.event.state.dto.TaskStateEventDto stateEvent =
                new org.radarbase.appserver.event.state.dto.TaskStateEventDto(
                        this,
                        task,
                        taskStateEventDto.getState(),
                        additionalInfo,
                        taskStateEventDto.getTime());
        taskApplicationEventPublisher.publishEvent(stateEvent);
    }

    private void checkState(long taskId, TaskState state)
            throws SizeLimitExceededException, IllegalStateException {
        if (EXTERNAL_EVENTS.contains(state)) {
            if (taskStateEventRepository.countByTaskId(taskId)
                    >= MAX_NUMBER_OF_STATES) {
                throw new SizeLimitExceededException(
                        "The max limit of state changes("
                                + MAX_NUMBER_OF_STATES
                                + ") has been reached. Cannot add new states.");
            }
        } else {
            throw new IllegalStateException(
                    "The state "
                            + state
                            + " is not an external state and cannot be updated by this endpoint.");
        }
    }
}
