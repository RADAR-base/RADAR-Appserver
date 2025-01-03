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
import org.radarbase.appserver.dto.protocol.ReferenceTimestamp
import org.radarbase.appserver.dto.protocol.ReferenceTimestampType.*
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.entity.User
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

/**
 * A handler for processing simple protocols. This implementation defines the logic to update
 * the [AssessmentSchedule] based on the protocol's reference timestamp.
 *
 * The `SimpleProtocolHandler` utilizes [TimeCalculatorService] for timestamp calculations
 */
class SimpleProtocolHandler : ProtocolHandler {

    private val timeCalculatorService = TimeCalculatorService()

    /**
     * Processes the given assessment schedule and updates it with appropriate timestamps
     *
     * @param assessmentSchedule The assessment schedule to be updated.
     * @param assessment The assessment containing protocol details and additional metadata.
     * @param user The user whose timezone and enrolment date are used in processing.
     * @return The updated [AssessmentSchedule] with a populated reference timestamp and name.
     * @throws IllegalArgumentException If the user's enrolment date is null when the reference timestamp
     * is not provided.
     */
    override fun handle(
        assessmentSchedule: AssessmentSchedule, assessment: Assessment, user: User
    ): AssessmentSchedule {

        val referenceTimestamp: ReferenceTimestamp? = assessment.protocol?.referenceTimestamp
        val timezone = TimeZone.getTimeZone(user.timezone)
        val timezoneId = timezone.toZoneId()
        assessmentSchedule.referenceTimestamp = if (referenceTimestamp != null) {
            val timestamp = requireNotNull(referenceTimestamp.timestamp) {
                "Reference timestamp is null when handling SimpleProtocolHandler."
            }
            val timestampFormat = requireNotNull(referenceTimestamp.format) {
                "Reference timestamp format is null when handling SimpleProtocolHandler."

            }
            when (timestampFormat) {
                DATE -> LocalDate.parse(timestamp).atStartOfDay(timezoneId).toInstant()

                DATETIME -> LocalDateTime.parse(timestamp).atZone(timezoneId).toInstant()

                DATETIMEUTC -> Instant.parse(timestamp)

                NOW -> Instant.now()

                TODAY -> timeCalculatorService.setDateTimeToMidnight(Instant.now(), timezone)
            }
        } else {
            val userEnrolmentDate = user.enrolmentDate
            requireNotNull(userEnrolmentDate) {
                "User enrolment date is null when handling SimpleProtocolHandler."
            }
            timeCalculatorService.setDateTimeToMidnight(userEnrolmentDate, timezone)
        }
        assessmentSchedule.name = assessment.name
        return assessmentSchedule
    }
}
