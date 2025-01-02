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
import org.radarbase.appserver.dto.protocol.ReferenceTimestampType
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.entity.User
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

class SimpleProtocolHandler : ProtocolHandler {
    @Transient
    private val timeCalculatorService = TimeCalculatorService()

    override fun handle(
        assessmentSchedule: AssessmentSchedule,
        assessment: Assessment,
        user: User
    ): AssessmentSchedule {
        val referenceTimestamp = assessment.getProtocol().getReferenceTimestamp()
        val timezone = TimeZone.getTimeZone(user.timezone)
        val timezoneId = timezone.toZoneId()
        if (referenceTimestamp != null) {
            val timestamp = referenceTimestamp.getTimestamp()
            when (referenceTimestamp.getFormat()) {
                ReferenceTimestampType.DATE -> assessmentSchedule.setReferenceTimestamp(
                    LocalDate.parse(timestamp).atStartOfDay(timezoneId).toInstant()
                )

                ReferenceTimestampType.DATETIME -> assessmentSchedule.setReferenceTimestamp(
                    LocalDateTime.parse(
                        timestamp
                    ).atZone(timezoneId).toInstant()
                )

                ReferenceTimestampType.DATETIMEUTC -> assessmentSchedule.setReferenceTimestamp(
                    Instant.parse(
                        referenceTimestamp.getTimestamp()
                    )
                )

                ReferenceTimestampType.NOW -> assessmentSchedule.setReferenceTimestamp(Instant.now())
                ReferenceTimestampType.TODAY -> assessmentSchedule.setReferenceTimestamp(
                    timeCalculatorService.setDateTimeToMidnight(
                        Instant.now(), timezone
                    )
                )
            }
        } else {
            assessmentSchedule.setReferenceTimestamp(
                timeCalculatorService.setDateTimeToMidnight(
                    user.enrolmentDate!!,
                    timezone
                )
            )
        }
        assessmentSchedule.setName(assessment.getName())
        return assessmentSchedule
    }
}
