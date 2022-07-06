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

import org.radarbase.appserver.dto.protocol.Assessment;
import org.radarbase.appserver.dto.protocol.AssessmentType;
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
import org.radarbase.appserver.service.questionnaire.schedule.QuestionnaireScheduleGeneratorService;
import org.radarbase.appserver.util.CachedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class QuestionnaireScheduleService {

    private static final String TASK_SEARCH_PATTERN = "(\\w+?)(:|<|>)(\\w+?),";
    private transient ProtocolGenerator protocolGenerator;

    private transient CachedMap<String, Schedule> subjectScheduleMap;

    private final transient UserRepository userRepository;

    private final transient TaskRepository taskRepository;

    private transient QuestionnaireScheduleGeneratorService scheduleGeneratorService;

    private final transient ProjectRepository projectRepository;

    @Autowired
    public QuestionnaireScheduleService(ProtocolGenerator protocolGenerator, UserRepository userRepository, ProjectRepository projectRepository, QuestionnaireScheduleGeneratorService scheduleGeneratorService, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.taskRepository = taskRepository;
        this.protocolGenerator = protocolGenerator;
        this.protocolGenerator.init();
        this.protocolGenerator.getAllProtocols();
        subjectScheduleMap =
                new CachedMap<>(this::getAllSchedules, Duration.ofHours(2), Duration.ofHours(1));
        this.scheduleGeneratorService = scheduleGeneratorService;
    }

    // Use cached map of schedule of user
    public void getProtocolForProject(String projectId) throws IOException {
        protocolGenerator.getAllProtocols();
        subjectScheduleMap.get();
    }

    @Transactional
    public List<Task> getTasksUsingProjectIdAndSubjectId(String projectId, String subjectId) {
        User user = subjectAndProjectExistElseThrow(subjectId, projectId);
        return this.getScheduleForUser(user);
    }

    @Transactional
    public List<Task> getTasksByTypeUsingProjectIdAndSubjectId(String projectId,
                                                               String subjectId,
                                                               AssessmentType type,
                                                               String search) {

        Specification<Task> spec = getSearchBuilder(projectId, subjectId, type, search).build();
        return taskRepository.findAll(spec);

//        if (type != AssessmentType.ALL) {
//            return getTasksUsingProjectIdAndSubjectId(projectId, subjectId);
//        } else {
//            User user = subjectAndProjectExistElseThrow(subjectId, projectId);
//            return this.taskRepository.findByUserIdAndType(user.getId(), type);
//        }
    }

    @Transactional
    public List<Task> getScheduleForUser(User user) {
        return this.taskRepository.findByUserId(user.getId());
    }

    @Transactional
    public Schedule generateScheduleUsingProjectIdAndSubjectId(String projectId, String subjectId) {
        User user = subjectAndProjectExistElseThrow(subjectId, projectId);
        return this.scheduleGeneratorService.generateScheduleForUser(user, this.protocolGenerator);
    }

    @Transactional
    public Schedule generateScheduleUsingProjectIdAndSubjectIdAndAssessment(String projectId,
                                                                            String subjectId,
                                                                            Assessment assessment) {
        User user = subjectAndProjectExistElseThrow(subjectId, projectId);
        Schedule schedule = new Schedule(user);
        AssessmentSchedule a = this.scheduleGeneratorService.generateSingleAssessmentSchedule(assessment, user);
        schedule.addAssessmentSchedule(a);
        return schedule;
    }


    public Map<String, Schedule> getAllSchedules() {
        // Check if protocol hash has changed. only then update the map
        return Collections.emptyMap();
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

//        User user = subjectAndProjectExistElseThrow(subjectId, projectId);
//        if (type == AssessmentType.ALL) {
//            this.removeScheduleForUser(user);
//        } else {
//            this.taskRepository.deleteByUserIdAndType(user.getId(), type);
//        }
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
