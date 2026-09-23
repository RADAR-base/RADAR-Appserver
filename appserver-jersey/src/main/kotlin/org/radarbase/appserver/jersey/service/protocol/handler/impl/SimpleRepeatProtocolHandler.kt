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
import org.radarbase.appserver.jersey.dto.protocol.RepeatProtocol
import org.radarbase.appserver.jersey.dto.protocol.TimePeriod
import org.radarbase.appserver.jersey.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.service.protocol.handler.ProtocolHandler
import org.radarbase.appserver.jersey.service.protocol.time.TimeCalculatorService
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.TimeZone

class SimpleRepeatProtocolHandler : ProtocolHandler {
    private val timeCalculatorService = TimeCalculatorService()

    override suspend fun handle(
        assessmentSchedule: AssessmentSchedule,
        assessment: Assessment,
        user: User,
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
        timezoneId: String,
    ): List<Instant> {
        val timezone = TimeZone.getTimeZone(timezoneId)
        val repeatProtocol: RepeatProtocol? = assessment.protocol?.repeatProtocol

        val repeatProtocolUnit: String? = repeatProtocol?.unit
        val repeatProtocolAmount: Int? = repeatProtocol?.amount

        if (repeatProtocol == null || repeatProtocolUnit == null || repeatProtocolAmount == null) {
            logger.warn("Repeat protocol is null for assessment in SimpleRepeatProtocolHandler")
            return emptyList()
        }

        val simpleRepeatProtocol = TimePeriod(repeatProtocolUnit, repeatProtocolAmount)
        val maxTaskReachMillis = calculateMaxTaskReach(assessment, timezone)
        var referenceTime: Instant = calculateValidStartTime(startTime, timezone, simpleRepeatProtocol, maxTaskReachMillis)
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

    /**
     * Calculates the maximum time span from a reference timestamp to the end of
     * the latest possible active task, accounting for both the unitsFromZero offset
     * and the completion window. This extends the lower bound so that reference
     * timestamps whose tasks are still completable are not discarded.
     */
    private fun calculateMaxTaskReach(assessment: Assessment, timezone: TimeZone): Long {
        val repeatQuestionnaire = assessment.protocol?.repeatQuestionnaire
        val unit = repeatQuestionnaire?.unit
        val maxUnit = repeatQuestionnaire?.unitsFromZero?.maxOrNull()

        val maxOffsetMillis = if (unit != null && maxUnit != null) {
            timeCalculatorService.timePeriodToMillis(TimePeriod(unit, maxUnit))
        } else {
            0L
        }

        val completionWindowMillis = assessment.protocol?.completionWindow?.let {
            timeCalculatorService.timePeriodToMillis(it)
        } ?: 0L

        return maxOffsetMillis + completionWindowMillis
    }

    private fun calculateValidStartTime(
        startTime: Instant,
        timezone: TimeZone,
        simpleRepeatProtocol: TimePeriod,
        maxTaskReachMillis: Long,
    ): Instant {
        var referenceTime = startTime
        // The lower bound accounts for the max task reach (unitsFromZero + completionWindow):
        // a reference timestamp is valid if any of its tasks are still completable
        // (i.e. reference + maxOffset + completionWindow >= now - 1 week).
        val windowStart = timeCalculatorService.advanceRepeat(Instant.now(), MINUS_ONE_WEEK, timezone)
        val defaultStartTime = windowStart.minusMillis(maxTaskReachMillis)
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
