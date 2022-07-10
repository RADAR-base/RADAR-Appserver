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
import java.util.List;

import lombok.Data;
import org.radarbase.appserver.entity.User;

@Data
public class Schedule {
    private List<AssessmentSchedule> assessmentSchedules;

    private User user;

    public Schedule(User user) {
        String timezone = "Europe/London";
        this.user = user;
        this.user.setTimezone(timezone);
        this.assessmentSchedules = new ArrayList<>();

    }

    public Schedule(User user, List<AssessmentSchedule> assessmentSchedules) {
        String timezone = "Europe/London";
        this.user = user;
        this.user.setTimezone(timezone);
        this.assessmentSchedules = assessmentSchedules;
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
