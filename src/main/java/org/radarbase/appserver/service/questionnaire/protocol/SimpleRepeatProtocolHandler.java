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
import org.radarbase.appserver.dto.protocol.RepeatProtocol;
import org.radarbase.appserver.dto.protocol.TimePeriod;
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule;
import org.radarbase.appserver.entity.User;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class SimpleRepeatProtocolHandler implements ProtocolHandler {
    private transient TimeCalculatorService timeCalculatorService = new TimeCalculatorService();
    // Test timeperiod
    private transient TimePeriod PLUS_ONE_WEEK = new TimePeriod("week", 1);
    private transient TimePeriod MINUS_ONE_WEEK = new TimePeriod("week", -1);

    private final transient int MAX_YEAR = 2030;

    @Override
    public AssessmentSchedule handle(AssessmentSchedule assessmentSchedule, Assessment assessment, User user) {
        List<Instant> referenceTimestamps = generateReferenceTimestamps(assessment, assessmentSchedule.getReferenceTimestamp(), user.getTimezone());
        assessmentSchedule.setReferenceTimestamps(referenceTimestamps);
        return assessmentSchedule;
    }

    private List<Instant> generateReferenceTimestamps(Assessment assessment, Instant startTime, String timezoneId) {
        TimeZone timezone = TimeZone.getTimeZone(timezoneId);
        RepeatProtocol repeatProtocol = assessment.getProtocol().getRepeatProtocol();
        TimePeriod simpleRepeatProtocol = new TimePeriod(repeatProtocol.getUnit(), repeatProtocol.getAmount());
        Instant referenceTime = calculateValidStartTime(startTime, timezone, simpleRepeatProtocol);
        List<Instant> referenceTimestamps = new ArrayList<>();
        while (isValidReferenceTimestamp(referenceTime, timezone)) {
            referenceTimestamps.add(referenceTime);
            referenceTime = timeCalculatorService.advanceRepeat(referenceTime, simpleRepeatProtocol, timezone);
        }
        return referenceTimestamps;
    }

    private boolean isValidReferenceTimestamp(Instant referenceTime, TimeZone timezone) {
        Instant defaultEndTime = timeCalculatorService.advanceRepeat(Instant.now(), PLUS_ONE_WEEK, timezone);
        return referenceTime.isBefore(defaultEndTime) &&
                referenceTime.atZone(timezone.toZoneId()).getYear() < MAX_YEAR;
    }

    private Instant calculateValidStartTime(Instant referenceTime, TimeZone timezone, TimePeriod simpleRepeatProtocol) {
        Instant defaultStartTime = timeCalculatorService.advanceRepeat(Instant.now(), MINUS_ONE_WEEK, timezone);
        while (referenceTime.isBefore(defaultStartTime)) {
            referenceTime = timeCalculatorService.advanceRepeat(referenceTime, simpleRepeatProtocol, timezone);
        }
        return referenceTime;
    }
}
