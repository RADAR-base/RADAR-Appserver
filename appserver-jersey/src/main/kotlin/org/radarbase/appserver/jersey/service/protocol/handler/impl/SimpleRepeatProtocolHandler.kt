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
