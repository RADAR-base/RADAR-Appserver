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

import org.radarbase.appserver.dto.questionnaire.Schedule;
import org.radarbase.appserver.entity.Task;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.repository.TaskRepository;
import org.radarbase.appserver.repository.UserRepository;
import org.radarbase.appserver.service.questionnaire.protocol.ProtocolGenerator;
import org.radarbase.appserver.service.questionnaire.schedule.QuestionnaireScheduleGeneratorService;
import org.radarbase.appserver.util.CachedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class QuestionnaireScheduleService {
    private transient ProtocolGenerator protocolGenerator;

    private transient CachedMap<String, Schedule> subjectScheduleMap;

    private final transient UserRepository userRepository;

    private final transient TaskRepository taskRepository;

    private transient QuestionnaireScheduleGeneratorService scheduleGeneratorService;

    @Autowired

    public QuestionnaireScheduleService(ProtocolGenerator protocolGenerator, UserRepository userRepository, QuestionnaireScheduleGeneratorService scheduleGeneratorService, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.protocolGenerator = protocolGenerator;
        protocolGenerator.init();
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
    public List<Task> getScheduleUsingSubjectId(String subjectId) {
        Optional<User> user = this.userRepository.findBySubjectId(subjectId);
        if (user.isPresent())
            return this.getScheduleForUser(user.get());

        return null;
    }

    @Transactional
    public List<Task> getScheduleForUser(User user) {
        return this.taskRepository.findByUserId(user.getId());

    }

    @Transactional
    public Schedule generateScheduleUsingSubjectId(String subjectId) {
        Optional<User> user = this.userRepository.findBySubjectId(subjectId);
        if (user.isPresent()) {
            Schedule schedule = this.scheduleGeneratorService.generateScheduleForUser(user.get(), this.protocolGenerator);
            return schedule;
        }
        return null;
    }

    public Map<String, Schedule> getAllSchedules() {
        // Check if protocol hash has changed. only then update the map
        return Collections.emptyMap();
    }

    @Transactional
    public void removeScheduleForUserUsingSubjectId(String subjectId) {
        Optional<User> user = this.userRepository.findBySubjectId(subjectId);
        if (user.isPresent())
            this.removeScheduleForUser(user.get());
    }

    @Transactional
    public void removeScheduleForUser(User user) {
        this.taskRepository.deleteByUserId(user.getId());
    }

}
