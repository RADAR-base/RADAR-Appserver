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
import org.radarbase.appserver.dto.protocol.AssessmentProtocol;
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule;
import org.radarbase.appserver.dto.questionnaire.Schedule;

import java.time.Instant;

@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class SimpleProtocolHandler implements ProtocolHandler {

    @Override
    public AssessmentSchedule handle(Schedule schedule, Assessment assessment) {
        AssessmentProtocol assessmentProtocol = assessment.getProtocol();
        Instant referenceTimestamp = schedule.getEnrolmentDate();
        if (assessmentProtocol.getReferenceTimestamp() != null)
            referenceTimestamp = assessmentProtocol.getReferenceTimestamp();
        AssessmentSchedule assessmentSchedule = new AssessmentSchedule();
        assessmentSchedule.setReferenceTimestamp(referenceTimestamp);
        assessmentSchedule.setName(assessment.getName());
        return assessmentSchedule;
    }

}