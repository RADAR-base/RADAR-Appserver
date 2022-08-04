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

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.dto.protocol.Assessment;
import org.radarbase.appserver.dto.protocol.AssessmentType;
import org.radarbase.appserver.dto.protocol.Protocol;
import org.radarbase.appserver.dto.protocol.ScheduleCacheEntry;
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule;
import org.radarbase.appserver.dto.questionnaire.Schedule;
import org.radarbase.appserver.entity.Project;
import org.radarbase.appserver.entity.Task;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.exception.NotFoundException;
import org.radarbase.appserver.repository.ProjectRepository;
import org.radarbase.appserver.repository.TaskRepository;
import org.radarbase.appserver.repository.UserRepository;
import org.radarbase.appserver.search.TaskSpecificationsBuilder;
import org.radarbase.appserver.service.questionnaire.protocol.ProtocolGenerator;
import org.radarbase.appserver.service.questionnaire.protocol.TimeCalculatorService;
import org.radarbase.appserver.service.questionnaire.schedule.QuestionnaireScheduleGeneratorService;
import org.radarbase.appserver.util.CachedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
public class QuestionnaireScheduleService {

    private static final String TASK_SEARCH_PATTERN = "(\\w+?)(:|<|>)(\\w+?),";
    private final transient TimeCalculatorService timeCalculatorService = new TimeCalculatorService();

    private final transient ProtocolGenerator protocolGenerator;

    private transient CachedMap<String, Schedule> subjectScheduleMap;

    private final transient UserRepository userRepository;

    private final transient TaskRepository taskRepository;

    private final transient QuestionnaireScheduleGeneratorService scheduleGeneratorService;

    private final transient ProjectRepository projectRepository;

    @Autowired
    public QuestionnaireScheduleService(ProtocolGenerator protocolGenerator, UserRepository userRepository, ProjectRepository projectRepository, QuestionnaireScheduleGeneratorService scheduleGeneratorService, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.protocolGenerator = protocolGenerator;
        this.protocolGenerator.getAllProtocols();
        this.scheduleGeneratorService = scheduleGeneratorService;
        this.init();
        this.getAllSchedules();
    }

    public void init() {
        subjectScheduleMap =
                new CachedMap<>(this::generateAllSchedules, Duration.ofHours(2), Duration.ofHours(1));
    }

    @Transactional
    public List<Task> getTasksUsingProjectIdAndSubjectId(String projectId, String subjectId) {
        User user = subjectAndProjectExistElseThrow(subjectId, projectId);
        return this.getTasksForUser(user);
    }

    @Transactional
    public List<Task> getTasksByTypeUsingProjectIdAndSubjectId(String projectId,
                                                               String subjectId,
                                                               AssessmentType type,
                                                               String search) {

        Specification<Task> spec = getSearchBuilder(projectId, subjectId, type, search).build();
        return this.taskRepository.findAll(spec);

//        if (type != AssessmentType.ALL) {
//            return getTasksUsingProjectIdAndSubjectId(projectId, subjectId);
//        } else {
//            User user = subjectAndProjectExistElseThrow(subjectId, projectId);
//            return this.taskRepository.findByUserIdAndType(user.getId(), type);
//        }
    }

    @Transactional
    public List<Task> getTasksForDateUsingProjectIdAndSubjectId(String projectId, String subjectId, Instant startTime, Instant endTime) {
        User user = subjectAndProjectExistElseThrow(subjectId, projectId);
        List<Task> tasks = this.getTasksForUser(user);
        tasks.removeIf(t-> {
            Instant timestamp = t.getTimestamp().toInstant();
            return timestamp.plusMillis(t.getCompletionWindow()).isBefore(startTime) || timestamp.isAfter(endTime);
        });
        return tasks;
    }

    @Transactional
    public Task getTaskUsingProjectIdAndSubjectIdAndTaskId(String projectId, String subjectId, Long taskId) {
        User user = subjectAndProjectExistElseThrow(subjectId, projectId);
        return this.taskRepository.findByIdAndUserId(taskId, user.getId())
                .orElseThrow(() -> new NotFoundException("The task was not found"));
    }


    @Transactional
    public List<Task> getTasksForUser(User user) {
        return this.taskRepository.findByUserId(user.getId());
    }

    @Transactional
    public Schedule generateScheduleUsingProjectIdAndSubjectId(String projectId, String subjectId) {
        User user = subjectAndProjectExistElseThrow(subjectId, projectId);
        Protocol protocol = protocolGenerator.getProtocolForSubject(user.getSubjectId());
        return this.scheduleGeneratorService.generateScheduleForUser(user, protocol);
    }

    @Transactional
    public Schedule generateScheduleForUser(User user) {
        Protocol protocol = protocolGenerator.getProtocolForSubject(user.getSubjectId());
        Schedule prevSchedule = getScheduleForSubject(user.getSubjectId());
        if (!Objects.equals(prevSchedule.getVersion(), protocol.getVersion())) {
            this.removeScheduleForUser(user);
        }
        return this.scheduleGeneratorService.generateScheduleForUser(user, protocol);
    }

    @Transactional
    public Schedule generateScheduleUsingProjectIdAndSubjectIdAndAssessment(String projectId,
                                                                            String subjectId,
                                                                            Assessment assessment) {
        User user = subjectAndProjectExistElseThrow(subjectId, projectId);

        Schedule schedule = this.getScheduleForSubject(user.getSubjectId());
        AssessmentSchedule a = this.scheduleGeneratorService.generateSingleAssessmentSchedule(assessment, user);
        schedule.addAssessmentSchedule(a);
        return schedule;
    }

    public Map<String, Schedule> generateAllSchedules() {
        List<User> users = this.userRepository.findAll();

        return users.parallelStream()
                .map(u -> {
                    Schedule schedule = this.generateScheduleForUser(u);
                    return new ScheduleCacheEntry(u.getSubjectId(), schedule);
                }).collect(Collectors.toMap(ScheduleCacheEntry::getId, ScheduleCacheEntry::getSchedule));
    }

    public @NonNull Map<String, Schedule> getAllSchedules() {
        try {
            return subjectScheduleMap.get();
        } catch (IOException ex) {
            return subjectScheduleMap.getCache();
        }
    }

    public Schedule getScheduleForSubject(String subjectId) {
        try {
            return subjectScheduleMap.get(subjectId);
        } catch (IOException ex) {
            log.warn(
                    "Cannot retrieve Protocols for subject {} : {}, Using cached values.", subjectId, ex);
            return subjectScheduleMap.getCache().get(subjectId);
        } catch(NoSuchElementException ex) {
            log.warn("Subject does not exist in map. Fetching..");
            return forceGetScheduleForSubject(subjectId);
        }
    }

    private @NonNull Schedule forceGetScheduleForSubject(String subjectId) {
        try {
            return subjectScheduleMap.get(true).get(subjectId);
        } catch (IOException ex) {
            log.warn("Cannot retrieve Protocols, using cached values if available.", ex);
            return subjectScheduleMap.getCache().get(subjectId);
        }
    }

    @Transactional
    public void removeScheduleForUserUsingSubjectIdAndType(String projectId,
                                                           String subjectId,
                                                           AssessmentType type,
                                                           String search) {
        Specification<Task> spec = getSearchBuilder(projectId, subjectId, type, search).build();

        // TODO: DeleteAll with Specifications will soon be released in JPA (v 3.0.0), so update this to not fetch all entities.
        List<Task> tasks = taskRepository.findAll(spec);
        taskRepository.deleteAll(tasks);
    }

    @Transactional
    public void removeScheduleForUser(User user) {
        this.taskRepository.deleteByUserId(user.getId());
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
            throw new NotFoundException("Subject Id does not exist. Please create a user with the ID first");
        }

        return user.get();
    }

    private TaskSpecificationsBuilder getSearchBuilder(String projectId,
                                                       String subjectId,
                                                       AssessmentType type,
                                                       String search) {

        User user = subjectAndProjectExistElseThrow(subjectId, projectId);

        TaskSpecificationsBuilder builder = new TaskSpecificationsBuilder();
        builder.with("user", ":", user);

        if (type != AssessmentType.ALL && type != null) {
            builder.with("type", ":", type);
        }

        Pattern pattern = Pattern.compile(TASK_SEARCH_PATTERN);
        Matcher matcher = pattern.matcher(search + ",");
        while (matcher.find()) {
            builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
        }
        return builder;
    }

}
