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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class TimeCalculatorService {
    public Instant advanceRepeat(Instant referenceTime, TimePeriod offset) {
        ZonedDateTime time = ZonedDateTime.ofInstant(referenceTime, ZoneOffset.UTC);
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
}
