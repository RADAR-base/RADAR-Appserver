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
import org.radarbase.appserver.dto.protocol.Protocol;
import org.radarbase.appserver.dto.protocol.TimePeriod;
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule;
import org.radarbase.appserver.dto.questionnaire.Schedule;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class SimpleRepeatProtocolHandler implements RepeatProtocolHandler {
    private transient TimeCalculatorService timeCalculatorService = new TimeCalculatorService();
    private transient TimePeriod defaultTimePeriod = new TimePeriod("years", 0, "", 0);
    private transient Instant defaultEndTime = timeCalculatorService.advanceRepeat(Instant.now(), defaultTimePeriod);

    @Override
    public Schedule handle(Schedule schedule, Protocol protocol) {
        List<Assessment> assessments = protocol.getProtocols();
        List<AssessmentSchedule> assessmentSchedules = schedule.getAssessmentSchedules();
        ListIterator<AssessmentSchedule> assessmentScheduleIter = assessmentSchedules.listIterator();
        while (assessmentScheduleIter.hasNext()) {
            AssessmentSchedule assessmentSchedule = assessmentScheduleIter.next();
            List<Instant> referenceTimestamps = generateReferenceTimestamps(assessments.get(assessmentScheduleIter.nextIndex() - 1), assessmentSchedule.getReferenceTimestamp());
            assessmentSchedule.setReferenceTimestamps(referenceTimestamps);
        }
        return schedule;
    }

    private List<Instant> generateReferenceTimestamps(Assessment assessment, Instant startTime) {
        List<Instant> referenceTimestamps = new ArrayList<>();
        TimePeriod repeatProtocol = assessment.getProtocol().getRepeatProtocol();
        Instant referenceTime = startTime;
        while (referenceTime.isBefore(defaultEndTime)) {
            referenceTimestamps.add(referenceTime);
            referenceTime = timeCalculatorService.advanceRepeat(referenceTime, repeatProtocol);
        }
        return referenceTimestamps;
    }
}
