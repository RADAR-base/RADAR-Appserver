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

import org.radarbase.appserver.dto.protocol.TimePeriod
import java.time.Instant
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit

class TimeCalculatorService {
    @Transient
    var WEEK_TO_DAYS: Int = 7

    @Transient
    var MONTH_TO_DAYS: Int = 31

    @Transient
    var YEAR_TO_DAYS: Int = 365

    fun advanceRepeat(referenceTime: Instant, offset: TimePeriod, timezone: TimeZone): Instant? {
        val time = ZonedDateTime.ofInstant(referenceTime, timezone.toZoneId()).truncatedTo(ChronoUnit.MILLIS)
        when (offset.getUnit()) {
            "min" -> return time.plus(offset.getAmount().toLong(), ChronoUnit.MINUTES).toInstant()
            "hour" -> return time.plus(offset.getAmount().toLong(), ChronoUnit.HOURS).toInstant()
            "day" -> return time.plus(offset.getAmount().toLong(), ChronoUnit.DAYS).toInstant()
            "week" -> return time.plus(offset.getAmount().toLong(), ChronoUnit.WEEKS).toInstant()
            "month" -> return time.plus(offset.getAmount().toLong(), ChronoUnit.MONTHS).toInstant()
            "year" -> return time.plus(offset.getAmount().toLong(), ChronoUnit.YEARS).toInstant()
            else -> return ZonedDateTime.now().plus(2, ChronoUnit.YEARS).toInstant()
        }
    }

    fun setDateTimeToMidnight(timestamp: Instant, timezone: TimeZone): Instant? {
        var time = ZonedDateTime.ofInstant(timestamp, timezone.toZoneId())
        time = time.toLocalDate().atStartOfDay(time.getZone())
        return time.toInstant()
    }

    fun timePeriodToMillis(offset: TimePeriod): Long {
        val amount = offset.getAmount()
        when (offset.getUnit()) {
            "min" -> return TimeUnit.MINUTES.toMillis(amount.toLong())
            "hour" -> return TimeUnit.HOURS.toMillis(amount.toLong())
            "day" -> return TimeUnit.DAYS.toMillis(amount.toLong())
            "week" -> return TimeUnit.DAYS.toMillis((amount * WEEK_TO_DAYS).toLong())
            "month" -> return TimeUnit.DAYS.toMillis((amount * MONTH_TO_DAYS).toLong())
            "year" -> return TimeUnit.DAYS.toMillis((amount * YEAR_TO_DAYS).toLong())
            else -> return TimeUnit.DAYS.toMillis(1)
        }
    }
}
