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
    public ResponseEntity generateScheduleUsingProjectIdAndSubjectId(
            @Valid @PathVariable String projectId,
            @Valid @PathVariable String subjectId)
            throws URISyntaxException {
        Schedule schedule = this.scheduleService.generateScheduleUsingProjectIdAndSubjectId(projectId, subjectId);
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
    public List<Task> getScheduleUsingProjectIdAndSubjectId(
            @Valid @PathVariable String projectId,
            @Valid @PathVariable String subjectId,
            @RequestParam(required = false, defaultValue = "all") AssessmentType type,
            @RequestParam(required = false, defaultValue = "") String search) {
        return this.scheduleService.getTasksByTypeUsingProjectIdAndSubjectId(projectId, subjectId, type, search);
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
            @PathVariable String projectId,
            @PathVariable String subjectId,
            @RequestParam(required = false, defaultValue = "all") AssessmentType type,
            @RequestParam(required = false, defaultValue = "") String search) {
        this.scheduleService.removeScheduleForUserUsingSubjectIdAndType(projectId, subjectId, type, search);
        return ResponseEntity.ok().build();
    }

}
