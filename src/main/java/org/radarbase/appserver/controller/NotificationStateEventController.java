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

import static org.radarbase.appserver.config.AuthConfig.AuthEntities.MEASUREMENT;
import static org.radarbase.appserver.config.AuthConfig.AuthPermissions.CREATE;
import static org.radarbase.appserver.config.AuthConfig.AuthPermissions.READ;

import java.util.List;
import javax.naming.SizeLimitExceededException;
import javax.validation.Valid;

import org.radarbase.appserver.dto.NotificationStateEventDto;
import org.radarbase.appserver.service.NotificationStateEventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import radar.spring.auth.common.Authorized;
import radar.spring.auth.common.PermissionOn;

@RestController
public class NotificationStateEventController {

    private final transient NotificationStateEventService notificationStateEventService;

    public NotificationStateEventController(
            NotificationStateEventService notificationStateEventService) {
        this.notificationStateEventService = notificationStateEventService;
    }

    @Authorized(permission = READ, entity = MEASUREMENT)
    @GetMapping("/messaging/notifications/{notificationId}/state_events")
    public ResponseEntity<List<NotificationStateEventDto>> getNotificationStateEventsByNotificationId(
            @Valid @RequestParam(value = "projectId", required = false) String projectId,
            @Valid @RequestParam(value = "subjectId", required = false) String subjectId,
            @PathVariable long notificationId) {

        if (projectId != null && subjectId != null) {
            // TODO check permission on subject if necessary
            return ResponseEntity.ok(notificationStateEventService
                    .getNotificationStateEvents(projectId, subjectId, notificationId));
        }

        return ResponseEntity.ok(notificationStateEventService
                .getNotificationStateEventsByNotificationId(notificationId));
    }

    @Authorized(permission = CREATE, entity = MEASUREMENT, permissionOn = PermissionOn.SUBJECT)
    @PostMapping("/projects/{projectId}/users/{subjectId}/messaging/notifications/{notificationId}/state_events")
    public ResponseEntity<List<NotificationStateEventDto>> postNotificationStateEvent(
            @PathVariable String projectId, @PathVariable String subjectId,
            @PathVariable long notificationId,
            @RequestBody NotificationStateEventDto notificationStateEventDto) throws
            SizeLimitExceededException {
        // this should simply be PUT notifications/event {and object should contain the data to be
        // updated) and return only updated entity

        notificationStateEventService
                .publishNotificationStateEventExternal(projectId, subjectId, notificationId,
                        notificationStateEventDto);
        return ResponseEntity.ok(notificationStateEventService
                .getNotificationStateEvents(projectId, subjectId, notificationId));
    }
}
