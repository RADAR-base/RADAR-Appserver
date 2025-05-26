package org.radarbase.appserver.jersey.service.protocol.handler.impl

import org.radarbase.appserver.jersey.dto.protocol.Assessment
import org.radarbase.appserver.jersey.dto.protocol.ReferenceTimestamp
import org.radarbase.appserver.jersey.dto.protocol.ReferenceTimestampType
import org.radarbase.appserver.jersey.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.service.protocol.handler.ProtocolHandler
import org.radarbase.appserver.jersey.service.protocol.time.TimeCalculatorService
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.TimeZone


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
                ReferenceTimestampType.DATE -> LocalDate.parse(timestamp).atStartOfDay(timezoneId).toInstant()
                ReferenceTimestampType.DATETIME -> LocalDateTime.parse(timestamp).atZone(timezoneId).toInstant()
                ReferenceTimestampType.DATETIMEUTC -> Instant.parse(timestamp)
                ReferenceTimestampType.NOW -> Instant.now()
                ReferenceTimestampType.TODAY -> timeCalculatorService.setDateTimeToMidnight(Instant.now(), timezone)
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
