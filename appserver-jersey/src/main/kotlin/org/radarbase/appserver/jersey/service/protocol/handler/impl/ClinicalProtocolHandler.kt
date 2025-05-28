/*
 * Copyright 2025 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.appserver.jersey.service.protocol.handler.impl

import org.radarbase.appserver.jersey.dto.protocol.Assessment
import org.radarbase.appserver.jersey.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.service.protocol.handler.ProtocolHandler

/**
 * Handles the processing and management of clinical protocols.
 */
class ClinicalProtocolHandler : ProtocolHandler {
    /**
     * Handles the processing of an assessment schedule by updating its name based on the provided assessment.
     *
     * @param assessmentSchedule The assessment schedule to be updated.
     * @param assessment The assessment containing details used to update the schedule.
     * @param user The user associated with the assessment. This parameter may be null.
     * @return The updated assessment schedule with the name set based on the assessment's name.
     */
    override suspend fun handle(
        assessmentSchedule: AssessmentSchedule,
        assessment: Assessment,
        user: User
    ): AssessmentSchedule {
        assessmentSchedule.name = assessment.name
        return assessmentSchedule
    }
}
