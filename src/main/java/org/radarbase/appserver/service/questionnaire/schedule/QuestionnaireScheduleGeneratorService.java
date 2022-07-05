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

import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.dto.protocol.*;
import org.radarbase.appserver.service.FcmNotificationService;
import org.radarbase.appserver.service.TaskService;
import org.radarbase.appserver.service.questionnaire.protocol.ProtocolHandler;
import org.radarbase.appserver.service.questionnaire.protocol.factory.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class QuestionnaireScheduleGeneratorService implements ScheduleGeneratorService {
    private transient FcmNotificationService notificationService;
    private transient TaskService taskService;

    @Autowired
    public QuestionnaireScheduleGeneratorService(FcmNotificationService notificationService, TaskService taskService) {
        this.notificationService = notificationService;
        this.taskService = taskService;
    }

    @Override
    public ProtocolHandler getProtocolHandler(Assessment assessment) {
        if (assessment.getType() == AssessmentType.CLINICAL)
            return ProtocolHandlerFactory.getProtocolHandler(ProtocolHandlerType.CLINICAL);
        else
            return ProtocolHandlerFactory.getProtocolHandler(ProtocolHandlerType.SIMPLE);
    }

    @Override
    public ProtocolHandler getRepeatProtocolHandler(Assessment assessment) {
        if (assessment.getType() == AssessmentType.CLINICAL) return null;

        RepeatProtocolHandlerType type = RepeatProtocolHandlerType.SIMPLE;
        RepeatProtocol repeatProtocol = assessment.getProtocol().getRepeatProtocol();
        if (repeatProtocol.getDayOfWeek() != null)
            type = RepeatProtocolHandlerType.DAYOFWEEK;
        return RepeatProtocolHandlerFactory.getRepeatProtocolHandler(type);
    }

    @Override
    public ProtocolHandler getRepeatQuestionnaireHandler(Assessment assessment) {
        if (assessment.getType() == AssessmentType.CLINICAL) return null;

        RepeatQuestionnaireHandlerType type = RepeatQuestionnaireHandlerType.SIMPLE;
        RepeatQuestionnaire repeatQuestionnaire = assessment.getProtocol().getRepeatQuestionnaire();
        if (repeatQuestionnaire.getDayOfWeekMap() != null)
            type = RepeatQuestionnaireHandlerType.DAYOFWEEKMAP;
        if (repeatQuestionnaire.getRandomUnitsFromZeroBetween() != null)
            type = RepeatQuestionnaireHandlerType.RANDOM;
        return RepeatQuestionnaireHandlerFactory.getRepeatQuestionnaireHandler(type, taskService);
    }

    @Override
    public ProtocolHandler getNotificationHandler(Assessment assessment) {
        return NotificationHandlerFactory.getNotificationHandler(NotificationHandlerType.SIMPLE, notificationService);
    }


}
