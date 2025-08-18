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
package org.radarbase.appserver.controller

import jakarta.validation.Valid
import org.radarbase.appserver.config.AuthConfig.AuthEntities
import org.radarbase.appserver.config.AuthConfig.AuthPermissions
import org.radarbase.appserver.dto.protocol.Assessment
import org.radarbase.appserver.dto.protocol.AssessmentType
import org.radarbase.appserver.entity.Task
import org.radarbase.appserver.service.QuestionnaireScheduleService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import radar.spring.auth.common.Authorized
import java.net.URI
import java.net.URISyntaxException
import java.time.Instant
import java.util.*

@CrossOrigin
@RestController
class QuestionnaireScheduleEndpoint @Autowired constructor(@field:Transient @field:Autowired private val scheduleService: QuestionnaireScheduleService) {
    @PostMapping(
        (
            "/" +
                PathsUtil.PROJECT_PATH +
                "/" +
                PathsUtil.PROJECT_ID_CONSTANT +
                "/" +
                PathsUtil.USER_PATH +
                "/" +
                PathsUtil.SUBJECT_ID_CONSTANT +
                "/" +
                PathsUtil.QUESTIONNAIRE_SCHEDULE_PATH
            ),
    )
    @Authorized(permission = AuthPermissions.UPDATE, entity = AuthEntities.SUBJECT)
    @Throws(URISyntaxException::class)
    fun generateScheduleUsingProjectIdAndSubjectId(
        @PathVariable @Valid projectId: String,
        @PathVariable @Valid subjectId: String,
    ): ResponseEntity<Any> {
        this.scheduleService.generateScheduleUsingProjectIdAndSubjectId(projectId, subjectId)
        return ResponseEntity.created(
            URI("/" + PathsUtil.QUESTIONNAIRE_SCHEDULE_PATH + "/"),
        ).build()
    }

    @PutMapping(
        (
            "/" +
                PathsUtil.PROJECT_PATH +
                "/" +
                PathsUtil.PROJECT_ID_CONSTANT +
                "/" +
                PathsUtil.USER_PATH +
                "/" +
                PathsUtil.SUBJECT_ID_CONSTANT +
                "/" +
                PathsUtil.QUESTIONNAIRE_SCHEDULE_PATH
            ),
    )
    @Authorized(permission = AuthPermissions.UPDATE, entity = AuthEntities.SUBJECT)
    @Throws(URISyntaxException::class)
    fun generateScheduleUsingProtocol(
        @PathVariable projectId: String,
        @PathVariable subjectId: String,
        @RequestBody assessment: @Valid Assessment,
    ): ResponseEntity<Any> {
        return try {
            this.scheduleService.generateScheduleUsingProjectIdAndSubjectIdAndAssessment(
                projectId,
                subjectId,
                assessment,
            )
            ResponseEntity.created(
                URI("/" + PathsUtil.QUESTIONNAIRE_SCHEDULE_PATH + "/"),
            ).build()
        } catch (e: URISyntaxException) {
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message)
        }
    }

    @GetMapping(
        (
            "/" +
                PathsUtil.PROJECT_PATH +
                "/" +
                PathsUtil.PROJECT_ID_CONSTANT +
                "/" +
                PathsUtil.USER_PATH +
                "/" +
                PathsUtil.SUBJECT_ID_CONSTANT +
                "/" +
                PathsUtil.QUESTIONNAIRE_SCHEDULE_PATH
            ),
    )
    @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.SUBJECT)
    fun getScheduleUsingProjectIdAndSubjectId(
        @PathVariable @Valid projectId: String,
        @PathVariable @Valid subjectId: String,
        @RequestParam(required = false, defaultValue = "all") type: String,
        @RequestParam(required = false, defaultValue = "") search: String,
        @RequestParam(required = false) startTime: Instant? = null,
        @RequestParam(required = false) endTime: Instant? = null,
    ): List<Task> {
        val assessmentType = AssessmentType.valueOf(type.uppercase(Locale.getDefault()))
        // TODO: Use search instead of startTime and endTime
        if (startTime != null && endTime != null) {
            return this.scheduleService.getTasksForDateUsingProjectIdAndSubjectId(
                projectId,
                subjectId,
                startTime,
                endTime,
            )
        }

        if (assessmentType != AssessmentType.ALL) {
            return this.scheduleService.getTasksByTypeUsingProjectIdAndSubjectId(
                projectId,
                subjectId,
                assessmentType,
                search,
            )
        }
        return this.scheduleService.getTasksUsingProjectIdAndSubjectId(projectId, subjectId)
    }

    @DeleteMapping(
        (
            "/" +
                PathsUtil.PROJECT_PATH +
                "/" +
                PathsUtil.PROJECT_ID_CONSTANT +
                "/" +
                PathsUtil.USER_PATH +
                "/" +
                PathsUtil.SUBJECT_ID_CONSTANT +
                "/" +
                PathsUtil.QUESTIONNAIRE_SCHEDULE_PATH
            ),
    )
    @Authorized(permission = AuthPermissions.UPDATE, entity = AuthEntities.SUBJECT)
    fun deleteScheduleForUser(
        @PathVariable projectId: String,
        @PathVariable subjectId: String,
        @RequestParam(required = false, defaultValue = "all") type: String,
        @RequestParam(required = false, defaultValue = "") search: String,
    ): ResponseEntity<Any> {
        val assessmentType = AssessmentType.valueOf(type.uppercase(Locale.getDefault()))
        this.scheduleService.removeScheduleForUserUsingSubjectIdAndType(projectId, subjectId, assessmentType, search)
        return ResponseEntity.ok().build()
    }
}
