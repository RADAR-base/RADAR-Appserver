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
import org.radarbase.appserver.dto.protocol.ReferenceTimestamp;
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule;
import org.radarbase.appserver.entity.User;

import java.time.Instant;
import java.util.TimeZone;

@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class SimpleProtocolHandler implements ProtocolHandler {
    private transient TimeCalculatorService timeCalculatorService = new TimeCalculatorService();

    @Override
    public AssessmentSchedule handle(AssessmentSchedule assessmentSchedule, Assessment assessment, User user) {
        ReferenceTimestamp referenceTimestamp = assessment.getProtocol().getReferenceTimestamp();
        TimeZone timezone = TimeZone.getTimeZone(user.getTimezone());
        if (referenceTimestamp != null) {
            switch (referenceTimestamp.getFormat()) {
                case DATE:
                case DATETIME:
                case DATETIMEUTC:
                    assessmentSchedule.setReferenceTimestamp(Instant.parse(referenceTimestamp.getTimestamp()));
                    break;
                case NOW:
                    assessmentSchedule.setReferenceTimestamp(Instant.now());
                    break;
                case TODAY:
                    assessmentSchedule.setReferenceTimestamp(timeCalculatorService.setDateTimeToMidnight(Instant.now(), timezone));
                    break;
            }
        }
        else {
            assessmentSchedule.setReferenceTimestamp(timeCalculatorService.setDateTimeToMidnight(user.getEnrolmentDate(), timezone));
        }
        assessmentSchedule.setName(assessment.getName());
        return assessmentSchedule;
    }

}
