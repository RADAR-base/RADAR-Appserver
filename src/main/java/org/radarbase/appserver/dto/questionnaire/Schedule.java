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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.radarbase.appserver.entity.User;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Schedule {
    private List<AssessmentSchedule> assessmentSchedules = new ArrayList<>();

    private User user;

    private String version = "0.0.0";

    private String timezone;

    public Schedule(User user) {
        this.user = user;
        this.timezone = user.getTimezone();
        this.assessmentSchedules = new ArrayList<>();

    }

    public Schedule(User user, List<AssessmentSchedule> assessmentSchedules) {
        this.user = user;
        this.timezone = user.getTimezone();
        this.assessmentSchedules = assessmentSchedules;
    }

    public Schedule(List<AssessmentSchedule> assessmentSchedules, User user, String version) {
        this.user = user;
        this.timezone = user.getTimezone();
        this.assessmentSchedules = assessmentSchedules;
        this.version = version != null ? version : this.version;
    }

    public Schedule addAssessmentSchedule(AssessmentSchedule assessmentSchedule) {
        this.assessmentSchedules.add(assessmentSchedule);
        return this;
    }

    public Schedule addAssessmentSchedules(List<AssessmentSchedule> assessmentSchedules) {
        this.assessmentSchedules.addAll(assessmentSchedules);
        return this;
    }

}
