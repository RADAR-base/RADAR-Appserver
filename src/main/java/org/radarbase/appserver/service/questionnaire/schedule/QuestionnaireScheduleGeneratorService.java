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

import java.time.Instant;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.dto.protocol.*;
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule;
import org.radarbase.appserver.dto.questionnaire.Schedule;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.service.FcmNotificationService;
import org.radarbase.appserver.service.TaskService;
import org.radarbase.appserver.service.questionnaire.protocol.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class QuestionnaireScheduleGeneratorService implements ScheduleGeneratorService {
    private transient FcmNotificationService notificationService;
    private transient TaskService taskService;

    @Autowired
    public QuestionnaireScheduleGeneratorService(FcmNotificationService notificationService, TaskService taskService) {
        this.notificationService = notificationService;
        this.taskService = taskService;
    }


    @Override
    public AssessmentSchedule handleProtocol(AssessmentSchedule schedule, Assessment assessment, User user) {
        schedule = ProtocolHandlerFactory.getProtocolHandler(this.getProtocolHandlerType(assessment)).handle(schedule, assessment, user);
        return schedule;
    }

    @Override
    public AssessmentSchedule handleRepeatProtocol(AssessmentSchedule schedule, Assessment assessment, User user) {
        schedule = RepeatProtocolHandlerFactory.getRepeatProtocolHandler(this.getRepeatProtocolHandlerType(assessment)).handle(schedule, assessment, user);
        return schedule;
    }

    @Override
    public AssessmentSchedule handleRepeatQuestionnaire(AssessmentSchedule schedule, Assessment assessment, User user) {
        schedule = RepeatQuestionnaireHandlerFactory.getRepeatQuestionnaireHandler(this.getRepeatQuestionnaireHandlerType(assessment), taskService).handle(schedule, assessment, user);
        return schedule;
    }

    @Override
    public AssessmentSchedule handleNotifications(AssessmentSchedule schedule, Assessment assessment, User user) {
        schedule = NotificationHandlerFactory.getNotificationHandler(this.getNotificationHandlerType(assessment), notificationService).handle(schedule, assessment, user);
        return schedule;
    }

    @Override
    public AssessmentSchedule handleClinicalProtocol(AssessmentSchedule schedule, Assessment assessment, User user) {
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

    private NotificationHandlerType getNotificationHandlerType(Assessment assessment) {
        return NotificationHandlerType.SIMPLE;
    }
}
