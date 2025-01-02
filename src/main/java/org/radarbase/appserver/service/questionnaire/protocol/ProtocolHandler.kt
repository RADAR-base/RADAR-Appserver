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

package org.radarbase.appserver.service.questionnaire.protocol

import org.radarbase.appserver.dto.protocol.Assessment
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.entity.User

/**
 * Interface for handling protocol-related tasks. Implementations are responsible for processing
 * and updating an [AssessmentSchedule] based on the associated [protocol][Assessment] details.
 */
interface ProtocolHandler {
    /**
     * Processes the given assessment schedule and updates it with appropriate data based on the input parameters.
     *
     * @param assessmentSchedule The assessment schedule that will be updated.
     * @param assessment The assessment containing protocol details and metadata used for processing.
     * @param user The user whose information, such as timezone and enrolment date, is utilized in the processing.
     * @return The updated [AssessmentSchedule] containing the results of the handling process.
     */
    fun handle(assessmentSchedule: AssessmentSchedule, assessment: Assessment, user: User): AssessmentSchedule
}
