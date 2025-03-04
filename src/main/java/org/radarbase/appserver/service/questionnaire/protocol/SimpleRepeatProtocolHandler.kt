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
import org.radarbase.appserver.dto.protocol.RepeatProtocol
import org.radarbase.appserver.dto.protocol.TimePeriod
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.entity.User
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.*

class SimpleRepeatProtocolHandler : ProtocolHandler {
    private val timeCalculatorService = TimeCalculatorService()

    override fun handle(
        assessmentSchedule: AssessmentSchedule,
        assessment: Assessment,
        user: User
    ): AssessmentSchedule {
        val timezone = user.timezone
        requireNotNull(timezone) {
            "User timezone is null when handling SimpleRepeatProtocolHandler."
        }

        val referenceTimestamp = requireNotNull(assessmentSchedule.referenceTimestamp) {
            "Reference timestamp is null when handling SimpleRepeatProtocolHandler."
        }

        val referenceTimestamps = generateReferenceTimestamps(assessment, referenceTimestamp, timezone)
        assessmentSchedule.referenceTimestamps = referenceTimestamps
        return assessmentSchedule
    }

    private fun generateReferenceTimestamps(
        assessment: Assessment,
        startTime: Instant,
        timezoneId: String
    ): List<Instant> {
        val timezone = TimeZone.getTimeZone(timezoneId)
        val repeatProtocol: RepeatProtocol? = assessment.protocol?.repeatProtocol

        val repeatProtocolUnit: String? = repeatProtocol?.unit
        val repeatProtocolAmount: Int? = repeatProtocol?.amount

        if (repeatProtocol == null || repeatProtocolUnit == null ||  repeatProtocolAmount == null) {
            logger.warn("Repeat protocol is null for assessment in SimpleRepeatProtocolHandler")
            return emptyList()
        }

        val simpleRepeatProtocol = TimePeriod(repeatProtocolUnit, repeatProtocolAmount)
        var referenceTime: Instant = calculateValidStartTime(startTime, timezone, simpleRepeatProtocol)
        val referenceTimestamps: MutableList<Instant> = mutableListOf()
        while (isValidReferenceTimestamp(referenceTime, timezone)) {
            referenceTimestamps.add(referenceTime)
            referenceTime = timeCalculatorService.advanceRepeat(referenceTime, simpleRepeatProtocol, timezone)
        }
        return referenceTimestamps
    }

    private fun isValidReferenceTimestamp(referenceTime: Instant, timezone: TimeZone): Boolean {
        val defaultEndTime = timeCalculatorService.advanceRepeat(Instant.now(), PLUS_ONE_WEEK, timezone)
        return referenceTime.isBefore(defaultEndTime) &&
                referenceTime.atZone(timezone.toZoneId()).year < MAX_YEAR
    }

    private fun calculateValidStartTime(
        startTime: Instant,
        timezone: TimeZone,
        simpleRepeatProtocol: TimePeriod
    ): Instant {
        var referenceTime = startTime
        val defaultStartTime = timeCalculatorService.advanceRepeat(Instant.now(), MINUS_ONE_WEEK, timezone)
        while (referenceTime.isBefore(defaultStartTime)) {
            referenceTime = timeCalculatorService.advanceRepeat(referenceTime, simpleRepeatProtocol, timezone)
        }
        return referenceTime
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SimpleRepeatProtocolHandler::class.java)

        private val PLUS_ONE_WEEK = TimePeriod("week", 1)
        private val MINUS_ONE_WEEK = TimePeriod("week", -1)
        private const val MAX_YEAR = 2030
    }
}
