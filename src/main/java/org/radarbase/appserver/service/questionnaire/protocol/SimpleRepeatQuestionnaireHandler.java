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

import java.time.Instant;
import java.util.*;

@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class SimpleRepeatQuestionnaireHandler implements RepeatQuestionnaireHandler {
    private transient TimeCalculatorService timeCalculatorService = new TimeCalculatorService();

    @Override
    public AssessmentSchedule handle(AssessmentSchedule assessmentSchedule, Assessment assessment, TimeZone timezone) {
        List<Task> tasks = generateTasks(assessment, assessmentSchedule.getReferenceTimestamps(), timezone);
        assessmentSchedule.setTasks(tasks);
        return assessmentSchedule;
    }

    public List<Task> generateTasks(Assessment assessment, List<Instant> referenceTimestamps, TimeZone timezone) {
        RepeatQuestionnaire repeatQuestionnaire = assessment.getProtocol().getRepeatQuestionnaire();
        List<Integer> unitsFromZero = repeatQuestionnaire.getUnitsFromZero();
        Iterator<Instant> referenceTimestampsIter = referenceTimestamps.iterator();
        List<Task> tasks = new ArrayList<>();
        while (referenceTimestampsIter.hasNext()) {
            Instant referenceTimestamp = referenceTimestampsIter.next();
            TimePeriod timePeriod = new TimePeriod();
            timePeriod.setUnit(repeatQuestionnaire.getUnit());
            Iterator<Integer> unitsFromZeroIter = unitsFromZero.iterator();
            while (unitsFromZeroIter.hasNext()) {
                timePeriod.setAmount(unitsFromZeroIter.next());
                Instant taskTime = timeCalculatorService.advanceRepeat(referenceTimestamp, timePeriod, timezone);
                Task task = buildTask(assessment, taskTime);
                tasks.add(task);
            }
        }
        return tasks;
    }

    private Task buildTask(Assessment assessment, Instant timestamp) {
        System.out.println(assessment);
        // TODO: To add other keys
        Task task = new Task.TaskBuilder()
                .name(assessment.getName())
                .estimatedCompletionTime(assessment.getEstimatedCompletionTime())
                .completionWindow(assessment.getCompletionWindow())
                .timestamp(timestamp)
                .build();
        return task;
    }
}
