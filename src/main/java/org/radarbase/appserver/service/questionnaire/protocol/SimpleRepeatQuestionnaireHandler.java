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

package org.radarbase.appserver.service.questionnaire.protocol;

import org.radarbase.appserver.dto.protocol.Assessment;
import org.radarbase.appserver.dto.protocol.RepeatQuestionnaire;
import org.radarbase.appserver.dto.protocol.TimePeriod;
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule;
import org.radarbase.appserver.entity.Task;
import org.radarbase.appserver.entity.User;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class SimpleRepeatQuestionnaireHandler implements ProtocolHandler {
    private transient Long DefaultTaskCompletionWindow = 86400000L;

    private transient TimeCalculatorService timeCalculatorService = new TimeCalculatorService();
    private transient TaskGeneratorService taskGeneratorService = new TaskGeneratorService();

    public SimpleRepeatQuestionnaireHandler() { }

    public AssessmentSchedule handle(AssessmentSchedule assessmentSchedule, Assessment assessment, User user) {
        List<Task> tasks = generateTasks(assessment, assessmentSchedule.getReferenceTimestamps(), user);
        assessmentSchedule.setTasks(tasks);
        return assessmentSchedule;
    }

    private List<Task> generateTasks(Assessment assessment, List<Instant> referenceTimestamps, User user) {
        TimeZone timezone = TimeZone.getTimeZone(user.getTimezone());
        RepeatQuestionnaire repeatQuestionnaire = assessment.getProtocol().getRepeatQuestionnaire();
        List<Integer> unitsFromZero = repeatQuestionnaire.getUnitsFromZero();
        Long completionWindow = this.calculateCompletionWindow(assessment.getProtocol().getCompletionWindow());

        List<Task> tasks = referenceTimestamps.parallelStream()
                .flatMap(referenceTimestamp -> {
                    TimePeriod timePeriod = new TimePeriod();
                    timePeriod.setUnit(repeatQuestionnaire.getUnit());
                    List<Task> t = unitsFromZero.parallelStream()
                            .map(unitFromZero -> {
                                timePeriod.setAmount(unitFromZero);
                                Instant taskTime = timeCalculatorService.advanceRepeat(referenceTimestamp, timePeriod, timezone);
                                Task task = taskGeneratorService.buildTask(assessment, taskTime, completionWindow);
                                task.setUser(user);
                                return task;
                            }).collect(Collectors.toList());
                    return t.stream();
                }).collect(Collectors.toList());

        return tasks;
    }

    private Long calculateCompletionWindow(TimePeriod completionWindow) {
        if (completionWindow == null)
            return DefaultTaskCompletionWindow;
        return timeCalculatorService.timePeriodToMillis(completionWindow);
    }

}
