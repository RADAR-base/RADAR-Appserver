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
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule;
import org.radarbase.appserver.dto.questionnaire.Schedule;
import org.radarbase.appserver.entity.Project;
import org.radarbase.appserver.entity.Task;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.exception.NotFoundException;
import org.radarbase.appserver.repository.ProjectRepository;
import org.radarbase.appserver.repository.UserRepository;
import org.radarbase.appserver.search.TaskSpecificationsBuilder;
import org.radarbase.appserver.service.questionnaire.protocol.ProtocolGenerator;
import org.radarbase.appserver.service.questionnaire.schedule.QuestionnaireScheduleGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit; 
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
@SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
public class QuestionnaireScheduleService {

    private static final String TASK_SEARCH_PATTERN = "(\\w+?)(:|<|>)(\\w+?),";

    private final transient ProtocolGenerator protocolGenerator;

    private transient HashMap<String, Schedule> subjectScheduleMap = new HashMap<String, Schedule>();

    private final transient UserRepository userRepository;

    private final transient QuestionnaireScheduleGeneratorService scheduleGeneratorService;

    private final transient ProjectRepository projectRepository;

    @Autowired
    private final transient TaskService taskService;

    @Autowired
    public QuestionnaireScheduleService(ProtocolGenerator protocolGenerator, UserRepository userRepository, ProjectRepository projectRepository, QuestionnaireScheduleGeneratorService scheduleGeneratorService, TaskService taskService) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskService = taskService;
        this.protocolGenerator = protocolGenerator;
        this.scheduleGeneratorService = scheduleGeneratorService;
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
        return this.taskService.getTasksBySpecification(spec);

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
    public List<Task> getTasksForUser(User user) {
        return this.taskService.getTasksByUser(user);
    }

    @Transactional
    public Schedule generateScheduleUsingProjectIdAndSubjectId(String projectId, String subjectId) {
        User user = subjectAndProjectExistElseThrow(subjectId, projectId);
        return this.generateScheduleForUser(user);
    }

    @Transactional
    public Schedule generateScheduleForUser(User user) {
        Protocol protocol = protocolGenerator.getProtocolForSubject(user.getSubjectId());
        if (protocol == null) {
            Schedule emptySchedule = new Schedule();
            subjectScheduleMap.put(user.getSubjectId(), emptySchedule);
            return emptySchedule;
        }
        Schedule prevSchedule = getScheduleForSubject(user.getSubjectId());
        String prevTimezone = prevSchedule.getTimezone() != null ? prevSchedule.getTimezone() : user.getTimezone();
        if (!Objects.equals(prevSchedule.getVersion(), protocol.getVersion()) || !prevTimezone.equals(user.getTimezone())) {
            this.removeScheduleForUser(user);
        }
        Schedule newSchedule = this.scheduleGeneratorService.generateScheduleForUser(user, protocol, prevSchedule);

        List<Task> tasks = getTasksListFromAssessmentSchedules(newSchedule.getAssessmentSchedules());
        this.taskService.addTasks(tasks, user);

        subjectScheduleMap.put(user.getSubjectId(), newSchedule);
        return newSchedule;
    }

    private List<Task> getTasksListFromAssessmentSchedules(List<AssessmentSchedule> assessmentSchedules) {
        return assessmentSchedules.stream()
                .filter(s -> s.hasTasks())
                .flatMap(a -> a.getTasks().stream().filter(Objects::nonNull))
                .collect(Collectors.toList());
    }

    @Transactional
    public Schedule generateScheduleUsingProjectIdAndSubjectIdAndAssessment(String projectId,
                                                                            String subjectId,
                                                                            Assessment assessment) {
        User user = subjectAndProjectExistElseThrow(subjectId, projectId);
        Protocol protocol = protocolGenerator.getProtocolForSubject(user.getSubjectId());
        if (!protocol.hasAssessment(assessment.getName())) {
            throw new NotFoundException("Assessment not found in protocol. Add assessment to protocol first.");
        }

        Schedule schedule = this.getScheduleForSubject(user.getSubjectId());
        AssessmentSchedule a = this.scheduleGeneratorService.generateSingleAssessmentSchedule(assessment, user, Collections.emptyList(), user.getTimezone());
        schedule.addAssessmentSchedule(a);
        return schedule;
    }

    @Scheduled(fixedRate = 3_600_000)
    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    public void generateAllSchedules() {
        List<User> users = this.userRepository.findAll();
        log.info("Generating all schedules..");

        List<Schedule> schedules = users.stream()
                .map(u -> {
                    Schedule schedule = this.generateScheduleForUser(u);
                    return schedule;
                }).collect(Collectors.toList());
    }

    public Schedule getScheduleForSubject(String subjectId) {
        try {
            Schedule schedule = subjectScheduleMap.get(subjectId);
            return schedule != null ? schedule : new Schedule();
        } catch (NoSuchElementException ex) {
            log.warn("Subject does not exist in map.");
        }
        return new Schedule();
    }

    @Transactional
    public void removeScheduleForUserUsingSubjectIdAndType(String projectId,
                                                           String subjectId,
                                                           AssessmentType type,
                                                           String search) {
        Specification<Task> spec = getSearchBuilder(projectId, subjectId, type, search).build();

        // TODO: DeleteAll with Specifications will soon be released in JPA (v 3.0.0), so update this to not fetch all entities.
        this.taskService.deleteTasksBySpecification(spec);
    }

    @Transactional
    public void removeScheduleForUser(User user) {
        this.taskService.deleteTasksByUserId(user.getId());
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
