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

package org.radarbase.appserver.controller;

import org.radarbase.appserver.dto.questionnaire.Schedule;
import org.radarbase.appserver.service.questionnaire.schedule.ScheduleGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class QuestionnaireScheduleEndpoint {

    private transient ScheduleGeneratorService scheduleGenerator;

    @Autowired
    public QuestionnaireScheduleEndpoint(ScheduleGeneratorService scheduleGenerator) {
        this.scheduleGenerator = scheduleGenerator;
    }

    @GetMapping(
            "/" + PathsUtil.USER_PATH + "/" + PathsUtil.SUBJECT_ID_CONSTANT + "/"
                    + PathsUtil.QUESTIONNAIRE_SCHEDULE_PATH)
    public Schedule generateScheduleUsingSubjectId(
            @Valid @PathVariable String subjectId) {
        return this.scheduleGenerator.getScheduleBySubjectId(subjectId);

    }


}
