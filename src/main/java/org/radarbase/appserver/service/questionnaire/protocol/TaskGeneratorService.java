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
import org.radarbase.appserver.dto.protocol.TimePeriod;
import org.radarbase.appserver.entity.Task;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class TaskGeneratorService {
    private transient TimeCalculatorService timeCalculatorService = new TimeCalculatorService();
    private transient Long DefaultTaskCompletionWindow = 86400000L;

    public Task buildTask(Assessment assessment, Instant timestamp) {
        Task task = new Task.TaskBuilder()
                .name(assessment.getName())
                .estimatedCompletionTime(assessment.getEstimatedCompletionTime())
                .completionWindow(this.calculateCompletionWindow(assessment.getProtocol().getCompletionWindow()))
                .order(assessment.getOrder())
                .timestamp(timestamp)
                .showInCalendar(assessment.getShowInCalendar())
                .isDemo(assessment.getIsDemo())
                .nQuestions(assessment.getNQuestions())
                .build();
        return task;
    }

    private Long calculateCompletionWindow(TimePeriod completionWindow) {
        if (completionWindow == null)
            return DefaultTaskCompletionWindow;
        return timeCalculatorService.timePeriodToMillis(completionWindow);
    }
}