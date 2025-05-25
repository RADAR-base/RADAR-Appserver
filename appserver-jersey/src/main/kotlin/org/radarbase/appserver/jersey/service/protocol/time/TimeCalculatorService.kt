package org.radarbase.appserver.jersey.service.protocol.time

import org.radarbase.appserver.jersey.dto.protocol.TimePeriod
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

/**
 * A service for performing time-related calculations such as advancing a timestamp by a given time period
 * or converting time periods to milliseconds. This utility helps in handling various time-based operations,
 * considering specific time zones.
 */
class TimeCalculatorService {

    /**
     * Calculates an adjusted time based on a reference time, an offset, and a specific timezone.
     *
     * @param referenceTime the base time from which adjustments will be calculated
     * @param offset the time period adjustment to be applied, containing the unit and amount
     * @param timezone the timezone used to convert and calculate the adjusted time
     * @return the calculated Instant time with the adjustment applied
     */
    fun advanceRepeat(referenceTime: Instant, offset: TimePeriod, timezone: TimeZone): Instant {
        val time = referenceTime.atZone(timezone.toZoneId()).truncatedTo(ChronoUnit.MILLIS)
        val amount = requireNotNull(offset.amount) { "Amount cannot be null in time calculator service" }
        return when (offset.unit) {
            "min" -> time.plus(amount.toLong(), ChronoUnit.MINUTES).toInstant()
            "hour" -> time.plus(amount.toLong(), ChronoUnit.HOURS).toInstant()
            "day" -> time.plus(amount.toLong(), ChronoUnit.DAYS).toInstant()
            "week" -> time.plus(amount.toLong(), ChronoUnit.WEEKS).toInstant()
            "month" -> time.plus(amount.toLong(), ChronoUnit.MONTHS).toInstant()
            "year" -> time.plus(amount.toLong(), ChronoUnit.YEARS).toInstant()
            else -> ZonedDateTime.now().plus(2, ChronoUnit.YEARS).toInstant()
        }
    }

    /**
     * Adjusts the given timestamp to midnight in the specified timezone.
     *
     * @param timestamp The original timestamp to be adjusted.
     * @param timezone The timezone in which the timestamp should be set to midnight.
     * @return The adjusted timestamp set to midnight in the specified timezone.
     */
    fun setDateTimeToMidnight(timestamp: Instant, timezone: TimeZone): Instant {
        val baseTime = timestamp.atZone(timezone.toZoneId())
        return baseTime.toLocalDate().atStartOfDay(baseTime.zone).toInstant()
    }

    /**
     * Converts a given time period into its equivalent duration in milliseconds.
     *
     * @param offset the time period to be converted, consisting of an amount and a unit,
     * such as minutes, hours, days, weeks, months, or years.
     * @return the time period's equivalent duration in milliseconds as a long value.
     */
    fun timePeriodToMillis(offset: TimePeriod): Long {
        val amount = requireNotNull(offset.amount) { "Amount cannot be null in time calculator service" }
        val duration: Duration = when (offset.unit) {
            "min" -> amount.minutes
            "hour" -> amount.hours
            "day" -> amount.days
            "week" -> (amount * WEEK_TO_DAYS).days
            "month" -> (amount * MONTH_TO_DAYS).days
            "year" -> (amount * YEAR_TO_DAYS).days
            else -> 1.days
        }
        return duration.inWholeMilliseconds
    }

    companion object {
        /**
         * Represents the number of days in a week.
         *
         * This constant is used for conversions and calculations involving weeks
         * and days, where a week is assumed to always contain 7 days.
         */
        private const val WEEK_TO_DAYS = 7
        /**
         * Constant representing the number of days in a month. It is used as an approximate value
         * where 31 days is considered the standard number of days in a month for calculations.
         */
        private const val MONTH_TO_DAYS = 31
        /**
         * Represents the number of days in a standard non-leap year.
         *
         * This constant is used in date and time calculations where a year is approximated
         * as 365 days. It does not account for leap years or variations in calendar systems.
         */
        private const val YEAR_TO_DAYS = 365
    }
}
