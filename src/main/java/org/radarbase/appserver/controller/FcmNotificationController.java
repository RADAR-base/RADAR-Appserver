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

import org.radarbase.fcm.dto.FcmNotificationDto;
import org.radarbase.fcm.dto.FcmNotifications;
import org.radarbase.appserver.service.FcmNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.LocalDateTime;

/**
 * @author yatharthranjan
 */
@RestController
public class FcmNotificationController {

    @Autowired
    private FcmNotificationService notificationService;

    @GetMapping("/notifications")
    public ResponseEntity<FcmNotifications> getAllNotifications() {
        return ResponseEntity.ok(this.notificationService.getAllNotifications());
    }

    @GetMapping("/notifications/{id}")
    public ResponseEntity<FcmNotificationDto> getNotificationUsingId(@Valid @PathVariable Long id) {
        return ResponseEntity.ok(this.notificationService.getNotificationById(id));
    }
    // TODO: get notifications based on other params
    @GetMapping("/notifications/filtered")
    public ResponseEntity<FcmNotifications> getFilteredNotifications(@Valid @RequestParam(value = "type", required = false) String type,
                                                                     @Valid @RequestParam(value = "delivered", required = false) boolean delivered,
                                                                     @Valid @RequestParam(value = "ttlSeconds", required = false) int ttlSeconds,
                                                                     @Valid @RequestParam(value = "startTime", required = false) LocalDateTime startTime,
                                                                     @Valid @RequestParam(value = "endTime", required = false) LocalDateTime endTime) {
        return ResponseEntity.ok(this.notificationService.getFilteredNotifications(type, delivered, ttlSeconds, startTime, endTime));
    }

    @GetMapping("/projects/{projectId}/users/{subjectId}/notifications")
    public ResponseEntity<FcmNotifications> getUsersUsingProjectIdAndSubjectId(@Valid @PathVariable("projectId") String projectId,
                                                                               @Valid @PathVariable("subjectId") String subjectId) {
        return ResponseEntity.ok(this.notificationService.getNotificationsByProjectIdAndSubjectId(projectId, subjectId));
    }

    @GetMapping("/projects/{projectId}/notifications")
    public ResponseEntity<FcmNotifications> getNotificationsUsingProjectId(@Valid @PathVariable("projectId") String projectId) {
        return ResponseEntity.ok(this.notificationService.getNotificationsByProjectId(projectId));
    }


    @GetMapping("/users/{subjectid}/notifications")
    public ResponseEntity<FcmNotifications> getRadarNotificationsUsingSubjectId(
            @Valid @PathVariable("subjectId") String subjectId) {
        return ResponseEntity.ok(this.notificationService.getNotificationsBySubjectId(subjectId));
    }
}
