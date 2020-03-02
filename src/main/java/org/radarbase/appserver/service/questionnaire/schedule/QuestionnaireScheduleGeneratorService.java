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

package org.radarbase.appserver.service.questionnaire.schedule;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.dto.protocol.*;
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule;
import org.radarbase.appserver.dto.questionnaire.Schedule;
import org.radarbase.appserver.entity.Task;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.repository.UserRepository;
import org.radarbase.appserver.service.questionnaire.protocol.*;
import org.radarbase.appserver.util.CachedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QuestionnaireScheduleGeneratorService implements ScheduleGeneratorService {

    private transient ProtocolGenerator protocolGenerator;

    private transient CachedMap<String, Schedule> subjectScheduleMap;

    private final transient UserRepository userRepository;

    @Autowired
    public QuestionnaireScheduleGeneratorService(ProtocolGenerator protocolGenerator, UserRepository userRepository) {
        this.userRepository = userRepository;
        this.protocolGenerator = protocolGenerator;
        protocolGenerator.init();
        subjectScheduleMap =
                new CachedMap<>(this::getAllSchedules, Duration.ofHours(2), Duration.ofHours(1));
    }

    // Use cached map of schedule of user
    public void getProtocolForProject(String projectId) throws IOException {
        protocolGenerator.getAllProtocols();
        subjectScheduleMap.get();
    }

    public Schedule getScheduleBySubjectId(String subjectId) {
        Optional<User> user = userRepository.findBySubjectId(subjectId);
        if (user.isPresent()) {
            User u = user.get();
            return this.generateScheduleForUser(u, this.protocolGenerator);
        }
        return null;
    }

    public Schedule getScheduleForUser(User user) {
        return null;
    }

    public Map<String, Schedule> getAllSchedules() {
        // Check if protocol hash has changed. only then update the map
        return Collections.emptyMap();
    }

    @Override
    public Schedule handleProtocol(Schedule schedule, Protocol protocol) {
        List<Assessment> assessments = protocol.getProtocols();
        List<AssessmentSchedule> assessmentSchedules = new ArrayList<>();
        Iterator<Assessment> assessmentIter = assessments.iterator();
        while (assessmentIter.hasNext()) {
            Assessment assessment = assessmentIter.next();
            AssessmentSchedule assessmentSchedule = ProtocolHandlerFactory.getProtocolHandler(this.getProtocolHandlerType(assessment)).handle(schedule, assessment);
            assessmentSchedules.add(assessmentSchedule);
        }
        schedule.setAssessmentSchedules(assessmentSchedules);
        return schedule;
    }

    @Override
    public Schedule handleRepeatProtocol(Schedule schedule, Protocol protocol) {
        List<Assessment> assessments = protocol.getProtocols();
        List<AssessmentSchedule> assessmentSchedules = schedule.getAssessmentSchedules();
        ListIterator<AssessmentSchedule> assessmentScheduleIter = assessmentSchedules.listIterator();
        while (assessmentScheduleIter.hasNext()) {
            AssessmentSchedule assessmentSchedule = assessmentScheduleIter.next();
            Assessment assessment = assessments.get(assessmentScheduleIter.nextIndex() - 1);
            assessmentSchedule = RepeatProtocolHandlerFactory.getRepeatProtocolHandler(this.getRepeatProtocolHandlerType(assessment)).handle(assessmentSchedule, assessment, schedule.getTimezone());

        }
        return schedule;
    }

    @Override
    public Schedule handleRepeatQuestionnaire(Schedule schedule, Protocol protocol) {
        List<Assessment> assessments = protocol.getProtocols();
        List<AssessmentSchedule> assessmentSchedules = schedule.getAssessmentSchedules();
        ListIterator<AssessmentSchedule> assessmentScheduleIter = assessmentSchedules.listIterator();
        while (assessmentScheduleIter.hasNext()) {
            AssessmentSchedule assessmentSchedule = assessmentScheduleIter.next();
            Assessment assessment = assessments.get(assessmentScheduleIter.nextIndex() - 1);
            assessmentSchedule = RepeatQuestionnaireHandlerFactory.getRepeatQuestionnaireHandler(this.getRepeatQuestionnaireHandlerType(assessment)).handle(assessmentSchedule, assessment, schedule.getTimezone());
        }
        return schedule;
    }

    @Override
    public Schedule handleClinicalProtocol(Schedule schedule, Protocol protocol) {
        return schedule;
    }

    private ProtocolHandlerType getProtocolHandlerType(Assessment assessment) {
        return ProtocolHandlerType.SIMPLE;
    }

    private RepeatProtocolHandlerType getRepeatProtocolHandlerType(Assessment assessment) {
        AssessmentProtocol protocol = assessment.getProtocol();
        TimePeriod repeatProtocol = protocol.getRepeatProtocol();
        if (repeatProtocol.getDayOfWeek() != null)
            return RepeatProtocolHandlerType.DAYOFTHEWEEK;
        return RepeatProtocolHandlerType.SIMPLE;
    }

    private RepeatQuestionnaireHandlerType getRepeatQuestionnaireHandlerType(Assessment assessment) {
        AssessmentProtocol protocol = assessment.getProtocol();
        RepeatQuestionnaire repeatQuestionnaire = protocol.getRepeatQuestionnaire();
        if (repeatQuestionnaire.getUnitsFromZero() != null)
            return RepeatQuestionnaireHandlerType.SIMPLE;
        return RepeatQuestionnaireHandlerType.SIMPLE;

    }
}
