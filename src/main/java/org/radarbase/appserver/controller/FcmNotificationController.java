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

import static org.radarbase.appserver.config.AuthConfig.AuthEntities.*;
import static org.radarbase.appserver.config.AuthConfig.AuthPermissions.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import javax.validation.Valid;

import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.radarbase.appserver.dto.fcm.FcmNotifications;
import org.radarbase.appserver.service.FcmNotificationService;
import org.springframework.http.ResponseEntity;
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

/**
 * Resource Endpoint for getting and adding (scheduling) notifications on Firebase Cloud Messaging.
 *
 * @author yatharthranjan
 */
@RestController
public class FcmNotificationController {

    private transient FcmNotificationService notificationService;

    public FcmNotificationController(FcmNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Authorized(permission = READ, entity = MEASUREMENT)
    @GetMapping("/messaging/notifications")
    public ResponseEntity<FcmNotifications> getAllNotifications(
            @Valid @RequestParam(value = "projectId", required = false) String projectId,
            @Valid @RequestParam(value = "subjectId", required = false) String subjectId,
            @Valid @RequestParam(value = "type", required = false) String type,
            @Valid @RequestParam(value = "delivered", required = false) boolean delivered,
            @Valid @RequestParam(value = "ttlSeconds", required = false) int ttlSeconds,
            @Valid @RequestParam(value = "startTime", required = false) LocalDateTime startTime,
            @Valid @RequestParam(value = "endTime", required = false) LocalDateTime endTime,
            @Valid @RequestParam(value = "limit", required = false) int limit) {
        // TODO: process query parameters
        if (projectId != null && subjectId != null) {
            return ResponseEntity.ok(this.notificationService
                    .getNotificationsByProjectIdAndSubjectId(projectId, subjectId));
        } else if (projectId != null) {
            return ResponseEntity
                    .ok(this.notificationService.getNotificationsByProjectId(projectId));
        } else if (subjectId != null) {
            return ResponseEntity
                    .ok(this.notificationService.getNotificationsBySubjectId(subjectId));
        }
        return ResponseEntity.ok(this.notificationService.getAllNotifications());
    }

    @Authorized(permission = READ, entity = MEASUREMENT)
    @GetMapping("/messaging/notifications/{id}")
    public ResponseEntity<FcmNotificationDto> getNotificationUsingId(@Valid @PathVariable Long id) {
        return ResponseEntity.ok(this.notificationService.getNotificationById(id));
    }


    @Authorized(permission = CREATE, entity = MEASUREMENT, permissionOn = PermissionOn.SUBJECT)
    @PostMapping("/projects/{projectId}/users/{subjectId}/messaging/notifications")
    public ResponseEntity<FcmNotificationDto> addSingleNotification(@PathVariable String projectId,
            @PathVariable String subjectId,
            @Valid @RequestBody FcmNotificationDto notification) throws URISyntaxException {
        FcmNotificationDto notificationDto =
                this.notificationService.addNotification(notification, subjectId, projectId);
        return ResponseEntity.created(
                new URI("/" + PathsUtil.MESSAGING_NOTIFICATION_PATH + "/" + notificationDto
                        .getId())).body(notificationDto);
    }

    @Authorized(permission = CREATE, entity = MEASUREMENT, permissionOn = PermissionOn.SUBJECT)
    @PostMapping("/projects/{projectId}/users/{subjectId}/messaging/notifications/batch")
    public ResponseEntity<FcmNotifications> addBatchNotifications(@PathVariable String projectId,
            @PathVariable String subjectId, @Valid @RequestBody FcmNotifications notifications) {
        return ResponseEntity
                .ok(this.notificationService.addNotifications(notifications, subjectId, projectId));
    }

    @Authorized(permission = CREATE, entity = MEASUREMENT, permissionOn = PermissionOn.SUBJECT)
    @PutMapping("/projects/{projectId}/users/{subjectId}/messaging/notifications")
    public ResponseEntity updateNotification(@PathVariable String projectId,
            @PathVariable String subjectId, @Valid @RequestBody FcmNotificationDto notification) {

        return ResponseEntity.ok(this.notificationService
                .updateNotification(notification, subjectId, projectId));
    }

    @Authorized(permission = CREATE, entity = MEASUREMENT, permissionOn = PermissionOn.SUBJECT)
    @DeleteMapping("/projects/{projectId}/users/{subjectId}/messaging/notifications")
    public ResponseEntity deleteNotificationsForUser(@PathVariable String projectId,
            @PathVariable String subjectId) {

        this.notificationService.removeNotificationsForUser(projectId, subjectId);
        return ResponseEntity.ok().build();
    }

    @Authorized(permission = CREATE, entity = MEASUREMENT, permissionOn = PermissionOn.SUBJECT)
    @DeleteMapping("/projects/{projectId}/users/{subjectId}/messaging/notifications/{id}")
    public ResponseEntity deleteNotificationUsingProjectIdAndSubjectIdAndNotificationId(
            @PathVariable String projectId, @PathVariable String subjectId, @PathVariable Long id) {

        this.notificationService
                .deleteNotificationByProjectIdAndSubjectIdAndNotificationId(projectId, subjectId,
                        id);
        return ResponseEntity.ok().build();
    }
}
