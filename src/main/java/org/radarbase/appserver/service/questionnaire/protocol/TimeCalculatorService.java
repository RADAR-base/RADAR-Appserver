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

package org.radarbase.appserver.service.questionnaire.protocol;

import org.radarbase.appserver.dto.protocol.TimePeriod;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class TimeCalculatorService {
    transient int WEEK_TO_DAYS = 7;
    transient int MONTH_TO_DAYS = 31;
    transient int YEAR_TO_DAYS = 365;

    public Instant advanceRepeat(Instant referenceTime, TimePeriod offset, TimeZone timezone) {
        ZonedDateTime time = ZonedDateTime.ofInstant(referenceTime, timezone.toZoneId()).truncatedTo(ChronoUnit.MILLIS);
        switch (offset.getUnit()) {
            case "min":
                return time.plus(offset.getAmount(), ChronoUnit.MINUTES).toInstant();
            case "hour":
                return time.plus(offset.getAmount(), ChronoUnit.HOURS).toInstant();
            case "day":
                return time.plus(offset.getAmount(), ChronoUnit.DAYS).toInstant();
            case "week":
                return time.plus(offset.getAmount(), ChronoUnit.WEEKS).toInstant();
            case "month":
                return time.plus(offset.getAmount(), ChronoUnit.MONTHS).toInstant();
            case "year":
                return time.plus(offset.getAmount(), ChronoUnit.YEARS).toInstant();
            default:
                return ZonedDateTime.now().plus(2, ChronoUnit.YEARS).toInstant();
        }
    }

    public Instant setDateTimeToMidnight(Instant timestamp, TimeZone timezone) {
        ZonedDateTime time = ZonedDateTime.ofInstant(timestamp, timezone.toZoneId());
        time = time.toLocalDate().atStartOfDay(time.getZone());
        return time.toInstant();
    }

    public Long timePeriodToMillis(TimePeriod offset) {
        int amount = offset.getAmount();
        switch (offset.getUnit()) {
            case "min":
                return TimeUnit.MINUTES.toMillis(amount);
            case "hour":
                return TimeUnit.HOURS.toMillis(amount);
            case "day":
                return TimeUnit.DAYS.toMillis(amount);
            case "week":
                return TimeUnit.DAYS.toMillis(amount * WEEK_TO_DAYS);
            case "month":
                return TimeUnit.DAYS.toMillis(amount * MONTH_TO_DAYS);
            case "year":
                return TimeUnit.DAYS.toMillis(amount * YEAR_TO_DAYS);
            default:
                return TimeUnit.DAYS.toMillis(1);
        }

    }
}
