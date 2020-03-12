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
import org.radarbase.appserver.service.TaskService;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class RandomRepeatQuestionnaireHandler implements ProtocolHandler {
    private transient TimeCalculatorService timeCalculatorService = new TimeCalculatorService();
    private transient TaskGeneratorService taskGeneratorService = new TaskGeneratorService();
    private transient TaskService taskService;

    public RandomRepeatQuestionnaireHandler(TaskService taskService) {
        this.taskService = taskService;
    }

    public AssessmentSchedule handle(AssessmentSchedule assessmentSchedule, Assessment assessment, User user) {
        List<Task> tasks = generateTasks(assessment, assessmentSchedule.getReferenceTimestamps(), user);
        assessmentSchedule.setTasks(tasks);
        return assessmentSchedule;
    }

    private List<Task> generateTasks(Assessment assessment, List<Instant> referenceTimestamps, User user) {
        TimeZone timezone = TimeZone.getTimeZone(user.getTimezone());
        RepeatQuestionnaire repeatQuestionnaire = assessment.getProtocol().getRepeatQuestionnaire();
        List<Integer[]> randomUnitsFromZeroBetween = repeatQuestionnaire.getRandomUnitsFromZeroBetween();
        Iterator<Instant> referenceTimestampsIter = referenceTimestamps.iterator();
        List<Task> tasks = new ArrayList<>();
        while (referenceTimestampsIter.hasNext()) {
            Instant referenceTimestamp = referenceTimestampsIter.next();
            TimePeriod timePeriod = new TimePeriod();
            timePeriod.setUnit(repeatQuestionnaire.getUnit());
            Iterator<Integer[]> rangeIter = randomUnitsFromZeroBetween.iterator();
            while (rangeIter.hasNext()) {
                Integer[] range = rangeIter.next();
                timePeriod.setAmount(this.getRandomAmountInRange(range));
                Instant taskTime = timeCalculatorService.advanceRepeat(referenceTimestamp, timePeriod, timezone);
                Task task = taskGeneratorService.buildTask(assessment, taskTime);
                task.setUser(user);
                task = this.taskService.addTask(task);
                tasks.add(task);
            }
        }
        return tasks;
    }

    private Integer getRandomAmountInRange(Integer[] range) {
        Integer lowerLimit = range[0];
        Integer upperLimit = range[1];
        return (int) (Math.random() * (upperLimit - lowerLimit + 1)) + lowerLimit;
    }


}
