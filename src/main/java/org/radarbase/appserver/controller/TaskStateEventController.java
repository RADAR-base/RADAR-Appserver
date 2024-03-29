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

import org.radarbase.appserver.config.AuthConfig.AuthEntities;
import org.radarbase.appserver.config.AuthConfig.AuthPermissions;
import org.radarbase.appserver.dto.TaskStateEventDto;
import org.radarbase.appserver.service.TaskStateEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import radar.spring.auth.common.Authorized;
import radar.spring.auth.common.PermissionOn;

import javax.naming.SizeLimitExceededException;
import java.util.List;

@CrossOrigin
@RestController
public class TaskStateEventController {

    private final transient TaskStateEventService taskStateEventService;

    public TaskStateEventController(
            TaskStateEventService taskStateEventService) {
        this.taskStateEventService = taskStateEventService;
    }

    @Authorized(permission = AuthPermissions.UPDATE, entity = AuthEntities.SUBJECT)
    @GetMapping(
            value =
                    "/"
                            + PathsUtil.QUESTIONNAIRE_SCHEDULE_PATH
                            + "/"
                            + PathsUtil.TASK_ID_CONSTANT
                            + "/"
                            + PathsUtil.QUESTIONNAIRE_STATE_EVENTS_PATH)
    public ResponseEntity<List<TaskStateEventDto>> getTaskStateEventsByTaskId(
            @PathVariable long taskId) {
        return ResponseEntity.ok(taskStateEventService.getTaskStateEventsByTaskId(taskId));
    }

    @Authorized(
            permission = AuthPermissions.UPDATE,
            entity = AuthEntities.SUBJECT,
            permissionOn = PermissionOn.SUBJECT)
    @GetMapping(
            value =
                    "/"
                            + PathsUtil.PROJECT_PATH
                            + "/"
                            + PathsUtil.PROJECT_ID_CONSTANT
                            + "/"
                            + PathsUtil.USER_PATH
                            + "/"
                            + PathsUtil.SUBJECT_ID_CONSTANT
                            + "/"
                            + PathsUtil.QUESTIONNAIRE_SCHEDULE_PATH
                            + "/"
                            + PathsUtil.TASK_ID_CONSTANT
                            + "/"
                            + PathsUtil.QUESTIONNAIRE_STATE_EVENTS_PATH)
    public ResponseEntity<List<TaskStateEventDto>> getTaskStateEvents(
            @PathVariable String projectId,
            @PathVariable String subjectId,
            @PathVariable long taskId) {
        return ResponseEntity.ok(taskStateEventService.getTaskStateEvents(projectId, subjectId, taskId));
    }

    @Authorized(
            permission = AuthPermissions.UPDATE,
            entity = AuthEntities.SUBJECT,
            permissionOn = PermissionOn.SUBJECT)
    @PostMapping(
            value =
                    "/"
                            + PathsUtil.PROJECT_PATH
                            + "/"
                            + PathsUtil.PROJECT_ID_CONSTANT
                            + "/"
                            + PathsUtil.USER_PATH
                            + "/"
                            + PathsUtil.SUBJECT_ID_CONSTANT
                            + "/"
                            + PathsUtil.QUESTIONNAIRE_SCHEDULE_PATH
                            + "/"
                            + PathsUtil.TASK_ID_CONSTANT
                            + "/"
                            + PathsUtil.QUESTIONNAIRE_STATE_EVENTS_PATH)
    public ResponseEntity<List<TaskStateEventDto>> postTaskStateEvent(
            @PathVariable String projectId,
            @PathVariable String subjectId,
            @PathVariable long taskId,
            @RequestBody TaskStateEventDto taskStateEventDto)
            throws SizeLimitExceededException {

        taskStateEventService.publishNotificationStateEventExternal(
                projectId, subjectId, taskId, taskStateEventDto);
        return getTaskStateEvents(projectId, subjectId, taskId);
    }
}
