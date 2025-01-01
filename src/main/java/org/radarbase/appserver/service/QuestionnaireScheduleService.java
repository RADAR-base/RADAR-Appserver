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

import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
@SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
public class QuestionnaireScheduleService {

    private static final Pattern TASK_SEARCH_PATTERN = Pattern.compile("(\\w+)([:<>])(\\w+)");
    private static final Pattern COMMA_PATTERN = Pattern.compile(",");

    private final transient ProtocolGenerator protocolGenerator;

    private final transient HashMap<String, Schedule> subjectScheduleMap = new HashMap<>();

    private final transient UserRepository userRepository;

    private final transient QuestionnaireScheduleGeneratorService scheduleGeneratorService;

    private final transient ProjectRepository projectRepository;

    @Autowired
    private final transient TaskService taskService;

    @Autowired
    private final transient FcmNotificationService notificationService;

    @Autowired
    public QuestionnaireScheduleService(ProtocolGenerator protocolGenerator, UserRepository userRepository, ProjectRepository projectRepository, QuestionnaireScheduleGeneratorService scheduleGeneratorService, TaskService taskService, FcmNotificationService notificationService) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskService = taskService;
        this.protocolGenerator = protocolGenerator;
        this.scheduleGeneratorService = scheduleGeneratorService;
        this.notificationService = notificationService;
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
        Schedule newSchedule;

        if (protocol == null) {
            newSchedule = new Schedule();
        } else {
            Schedule prevSchedule = getScheduleForSubject(user.getSubjectId());
            String prevTimezone = prevSchedule.getTimezone() != null
                    ? prevSchedule.getTimezone()
                    : user.getTimezone();
            if (!Objects.equals(prevSchedule.getVersion(), protocol.getVersion()) || !prevTimezone.equals(user.getTimezone())) {
                this.removeScheduleForUser(user);
            }
            newSchedule = this.scheduleGeneratorService.generateScheduleForUser(user, protocol, prevSchedule);
        }

        subjectScheduleMap.put(user.getSubjectId(), newSchedule);
        this.saveTasksAndNotifications(newSchedule.getAssessmentSchedules(), user);

        return newSchedule;
    }

    private void saveTasksAndNotifications(List<AssessmentSchedule> assessmentSchedules, User user) {
        assessmentSchedules.stream()
                .filter(Objects::nonNull)
                .filter(AssessmentSchedule::hasTasks)
                .forEach(a -> {
                    this.taskService.addTasks(a.getTasks(), user);
                    this.notificationService.addNotifications(a.getNotifications(), user);
                    this.notificationService.addNotifications(a.getReminders(), user);
                });
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

        Schedule schedule = getScheduleForSubject(user.getSubjectId());
        AssessmentSchedule a = scheduleGeneratorService.generateSingleAssessmentSchedule(
                assessment, user, Collections.emptyList(), user.getTimezone());
        schedule.addAssessmentSchedule(a);

        this.saveTasksAndNotifications(List.of(a), user);
        return schedule;
    }

    @Scheduled(fixedRate = 3_600_000)
    public void generateAllSchedules() {
        List<User> users = this.userRepository.findAll();
        log.info("Generating all schedules..");
        users.forEach(this::generateScheduleForUser);
    }

    public Schedule getScheduleForSubject(String subjectId) {
        Schedule schedule = subjectScheduleMap.get(subjectId);
        return schedule != null ? schedule : new Schedule();
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
        Project project = this.projectRepository.findByProjectId(projectId);
        if (project == null) {
            throw new NotFoundException(
                    "Project Id does not exist. Please create a project with the ID first");
        }

        User user =
                this.userRepository.findBySubjectIdAndProjectId(subjectId, project.getId());
        if (user == null) {
            throw new NotFoundException("Subject Id does not exist. Please create a user with the ID first");
        }

        return user;
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

        if (search != null && !search.isBlank()) {
            String[] searchTerms = COMMA_PATTERN.split(search);

            for (String searchTerm : searchTerms) {
                Matcher matcher = TASK_SEARCH_PATTERN.matcher(searchTerm.trim());
                if (matcher.matches()) {
                    builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
                }
            }
        }
        return builder;
    }


}
