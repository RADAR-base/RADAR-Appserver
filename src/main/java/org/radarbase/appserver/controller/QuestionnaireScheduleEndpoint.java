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

import org.radarbase.appserver.dto.protocol.Assessment;
import org.radarbase.appserver.dto.protocol.AssessmentType;
import org.radarbase.appserver.dto.questionnaire.Schedule;
import org.radarbase.appserver.entity.Task;
import org.radarbase.appserver.service.QuestionnaireScheduleService;
import org.radarbase.appserver.config.AuthConfig.AuthEntities;
import org.radarbase.appserver.config.AuthConfig.AuthPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import radar.spring.auth.common.Authorized;

import jakarta.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@RestController
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
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
    @Authorized(permission = AuthPermissions.UPDATE, entity = AuthEntities.SUBJECT)               
    public ResponseEntity generateScheduleUsingProjectIdAndSubjectId(
            @Valid @PathVariable String projectId,
            @Valid @PathVariable String subjectId)
            throws URISyntaxException {
        this.scheduleService.generateScheduleUsingProjectIdAndSubjectId(projectId, subjectId);
        return ResponseEntity.created(
                new URI("/" + PathsUtil.QUESTIONNAIRE_SCHEDULE_PATH + "/")).build();
    }

    @PutMapping(
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
    @Authorized(permission = AuthPermissions.UPDATE, entity = AuthEntities.SUBJECT) 
    public ResponseEntity generateScheduleUsingProtocol(
            @PathVariable String projectId,
            @PathVariable String subjectId,
            @Valid @RequestBody Assessment assessment)
            throws URISyntaxException {
        try {
            this.scheduleService.generateScheduleUsingProjectIdAndSubjectIdAndAssessment(projectId, subjectId, assessment);
            return ResponseEntity.created(
                    new URI("/" + PathsUtil.QUESTIONNAIRE_SCHEDULE_PATH + "/")).build();

        } catch (URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
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
    @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.SUBJECT)
    public List<Task> getScheduleUsingProjectIdAndSubjectId(
            @Valid @PathVariable String projectId,
            @Valid @PathVariable String subjectId,
            @RequestParam(required = false, defaultValue = "all") String type,
            @RequestParam(required = false, defaultValue = "") String search,
            @RequestParam(required = false ) Optional<Instant> startTime,
            @RequestParam(required = false ) Optional<Instant> endTime) {
        AssessmentType assessmentType = AssessmentType.valueOf(type.toUpperCase(Locale.getDefault()));
        // TODO: Use search instead of startTime and endTime
        if (startTime.isPresent() && endTime.isPresent()) {
            return this.scheduleService.getTasksForDateUsingProjectIdAndSubjectId(projectId, subjectId, startTime.get(), endTime.get());
        }
        if (assessmentType != AssessmentType.ALL) {
            return this.scheduleService.getTasksByTypeUsingProjectIdAndSubjectId(projectId, subjectId, assessmentType, search);
        }
        return this.scheduleService.getTasksUsingProjectIdAndSubjectId(projectId, subjectId);
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
    @Authorized(permission = AuthPermissions.UPDATE, entity = AuthEntities.SUBJECT)                 
    public ResponseEntity deleteScheduleForUser(
            @PathVariable String projectId,
            @PathVariable String subjectId,
            @RequestParam(required = false, defaultValue = "all") String type,
            @RequestParam(required = false, defaultValue = "") String search) {
        AssessmentType assessmentType = AssessmentType.valueOf(type.toUpperCase(Locale.getDefault()));
        this.scheduleService.removeScheduleForUserUsingSubjectIdAndType(projectId, subjectId, assessmentType, search);
        return ResponseEntity.ok().build();
    }

}
