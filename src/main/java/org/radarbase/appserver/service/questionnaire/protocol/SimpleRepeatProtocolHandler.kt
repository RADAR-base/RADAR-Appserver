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
import org.radarbase.appserver.dto.protocol.TimePeriod
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.entity.User
import java.time.Instant
import java.util.*

class SimpleRepeatProtocolHandler : ProtocolHandler {
    @Transient
    private val timeCalculatorService = TimeCalculatorService()

    @Transient
    private val PLUS_ONE_WEEK = TimePeriod("week", 1)

    @Transient
    private val MINUS_ONE_WEEK = TimePeriod("week", -1)

    @Transient
    private val MAX_YEAR = 2030

    override fun handle(
        assessmentSchedule: AssessmentSchedule,
        assessment: Assessment,
        user: User
    ): AssessmentSchedule {
        val referenceTimestamps =
            generateReferenceTimestamps(assessment, assessmentSchedule.getReferenceTimestamp(), user.timezone)
        assessmentSchedule.setReferenceTimestamps(referenceTimestamps)
        return assessmentSchedule
    }

    private fun generateReferenceTimestamps(
        assessment: Assessment,
        startTime: Instant,
        timezoneId: String?
    ): MutableList<Instant?> {
        val timezone = TimeZone.getTimeZone(timezoneId)
        val repeatProtocol = assessment.getProtocol().getRepeatProtocol()
        val simpleRepeatProtocol = TimePeriod(repeatProtocol.getUnit(), repeatProtocol.getAmount())
        var referenceTime = calculateValidStartTime(startTime, timezone, simpleRepeatProtocol)
        val referenceTimestamps: MutableList<Instant?> = ArrayList<Instant?>()
        while (isValidReferenceTimestamp(referenceTime, timezone)) {
            referenceTimestamps.add(referenceTime)
            referenceTime = timeCalculatorService.advanceRepeat(referenceTime, simpleRepeatProtocol, timezone)
        }
        return referenceTimestamps
    }

    private fun isValidReferenceTimestamp(referenceTime: Instant, timezone: TimeZone): Boolean {
        val defaultEndTime = timeCalculatorService.advanceRepeat(Instant.now(), PLUS_ONE_WEEK, timezone)
        return referenceTime.isBefore(defaultEndTime) &&
                referenceTime.atZone(timezone.toZoneId()).getYear() < MAX_YEAR
    }

    private fun calculateValidStartTime(
        referenceTime: Instant,
        timezone: TimeZone,
        simpleRepeatProtocol: TimePeriod
    ): Instant {
        var referenceTime = referenceTime
        val defaultStartTime = timeCalculatorService.advanceRepeat(Instant.now(), MINUS_ONE_WEEK, timezone)
        while (referenceTime.isBefore(defaultStartTime)) {
            referenceTime = timeCalculatorService.advanceRepeat(referenceTime, simpleRepeatProtocol, timezone)
        }
        return referenceTime
    }
}
