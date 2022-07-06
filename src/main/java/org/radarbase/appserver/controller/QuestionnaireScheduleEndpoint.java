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

import org.radarbase.appserver.dto.protocol.AssessmentType;
import org.radarbase.appserver.dto.questionnaire.Schedule;
import org.radarbase.appserver.entity.Task;
import org.radarbase.appserver.service.QuestionnaireScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

@RestController
public class QuestionnaireScheduleEndpoint {

    @Autowired
    private transient QuestionnaireScheduleService scheduleService;

    @Autowired
    public QuestionnaireScheduleEndpoint(QuestionnaireScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @PostMapping(
            "/"
                    + PathsUtil.PROJECT_PATH
                    + "/"
                    + PathsUtil.PROJECT_ID_CONSTANT
                    + "/"
                    + PathsUtil.USER_PATH
                    + "/"
                    + PathsUtil.SUBJECT_ID_CONSTANT
                    + "/"
                    + PathsUtil.QUESTIONNAIRE_SCHEDULE_PATH)
    public ResponseEntity generateScheduleUsingSubjectId(
            @PathVariable String subjectId)
            throws URISyntaxException {
        Schedule schedule = this.scheduleService.generateScheduleUsingSubjectId(subjectId);
        return ResponseEntity.created(
                new URI("/" + PathsUtil.QUESTIONNAIRE_SCHEDULE_PATH + "/")).build();
    }

    @GetMapping(
            "/"
                    + PathsUtil.PROJECT_PATH
                    + "/"
                    + PathsUtil.PROJECT_ID_CONSTANT
                    + "/"
                    + PathsUtil.USER_PATH
                    + "/"
                    + PathsUtil.SUBJECT_ID_CONSTANT
                    + "/"
                    + PathsUtil.QUESTIONNAIRE_SCHEDULE_PATH)
    public List<Task> getScheduleUsingSubjectId(
            @Valid @PathVariable String subjectId,
            @RequestParam(required = false, defaultValue = "all") String type) {
        AssessmentType assessmentType = AssessmentType.valueOf(type.toUpperCase());
        if (assessmentType != AssessmentType.ALL) {
            return this.scheduleService.getTasksByTypeUsingSubjectId(subjectId, assessmentType);
        }
        return this.scheduleService.getTasksUsingSubjectId(subjectId);

    }

    @DeleteMapping(
            "/"
                    + PathsUtil.PROJECT_PATH
                    + "/"
                    + PathsUtil.PROJECT_ID_CONSTANT
                    + "/"
                    + PathsUtil.USER_PATH
                    + "/"
                    + PathsUtil.SUBJECT_ID_CONSTANT
                    + "/"
                    + PathsUtil.QUESTIONNAIRE_SCHEDULE_PATH)
    public ResponseEntity deleteScheduleForUser(
            @PathVariable String subjectId) {
        this.scheduleService.removeScheduleForUserUsingSubjectId(subjectId);
        return ResponseEntity.ok().build();
    }

}
