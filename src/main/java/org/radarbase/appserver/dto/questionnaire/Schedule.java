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

package org.radarbase.appserver.dto.questionnaire;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.TimeZone;

import lombok.Data;

@Data
public class Schedule {
    private List<AssessmentSchedule> assessmentSchedules;

    private Instant enrolmentDate;

    private TimeZone timezone;

    public Schedule(Instant enrolmentDate, String timezone) {

        this.enrolmentDate = enrolmentDate;
        timezone = "Europe/London";
        this.timezone = TimeZone.getTimeZone(timezone);
    }

}
