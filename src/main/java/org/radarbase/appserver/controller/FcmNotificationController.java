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
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author yatharthranjan
 */
@RestController
public class FcmNotificationController {

    @Autowired
    private FcmNotificationService notificationService;

    @GetMapping("/" + Paths.NOTIFICATION_PATH)
    public ResponseEntity<FcmNotifications> getAllNotifications() {
        return ResponseEntity.ok(this.notificationService.getAllNotifications());
    }

    @GetMapping("/" + Paths.NOTIFICATION_PATH + "/{id}")
    public ResponseEntity<FcmNotificationDto> getNotificationUsingId(@Valid @PathVariable Long id) {
        return ResponseEntity.ok(this.notificationService.getNotificationById(id));
    }
    // TODO: get notifications based on other params
    @GetMapping("/" + Paths.NOTIFICATION_PATH + "/filtered")
    public ResponseEntity<FcmNotifications> getFilteredNotifications(@Valid @RequestParam(value = "type", required = false) String type,
                                                                     @Valid @RequestParam(value = "delivered", required = false) boolean delivered,
                                                                     @Valid @RequestParam(value = "ttlSeconds", required = false) int ttlSeconds,
                                                                     @Valid @RequestParam(value = "startTime", required = false) LocalDateTime startTime,
                                                                     @Valid @RequestParam(value = "endTime", required = false) LocalDateTime endTime) {
        return ResponseEntity.ok(this.notificationService.getFilteredNotifications(type, delivered, ttlSeconds, startTime, endTime));
    }

    @GetMapping("/" + Paths.PROJECT_PATH + "/{projectId}/ " + Paths.USER_PATH + "/{subjectId}/" + Paths.NOTIFICATION_PATH)
    public ResponseEntity<FcmNotifications> getUsersUsingProjectIdAndSubjectId(@Valid @PathVariable("projectId") String projectId,
                                                                               @Valid @PathVariable("subjectId") String subjectId) {
        return ResponseEntity.ok(this.notificationService.getNotificationsByProjectIdAndSubjectId(projectId, subjectId));
    }

    @GetMapping("/" + Paths.PROJECT_PATH + "/{projectId}/" + Paths.NOTIFICATION_PATH)
    public ResponseEntity<FcmNotifications> getNotificationsUsingProjectId(@Valid @PathVariable("projectId") String projectId) {
        return ResponseEntity.ok(this.notificationService.getNotificationsByProjectId(projectId));
    }


    @GetMapping("/" + Paths.USER_PATH + "/{subjectid}/" + Paths.NOTIFICATION_PATH)
    public ResponseEntity<FcmNotifications> getRadarNotificationsUsingSubjectId(
            @Valid @PathVariable("subjectId") String subjectId) {
        return ResponseEntity.ok(this.notificationService.getNotificationsBySubjectId(subjectId));
    }


    @PostMapping("/" + Paths.PROJECT_PATH + "/{projectId}/" + Paths.USER_PATH + "/{subjectId}/" + Paths.NOTIFICATION_PATH)
    public ResponseEntity<FcmNotificationDto> scheduleSingleNotification( @PathVariable String projectId,
                                                                          @PathVariable String subjectId,
                                                                          @Valid @RequestBody FcmNotificationDto notification) {

        // Call scheduler service to add -> which calls other appropriate services to put in db
        // And also schedules using quarts maybe

        return ResponseEntity.ok(this.notificationService.addNotification(notification, subjectId, projectId));

    }

    @PostMapping("/" + Paths.PROJECT_PATH + "/{projectId}/" + Paths.USER_PATH + "/{userId}/" + Paths.NOTIFICATION_PATH + "/multi")
    public ResponseEntity<FcmNotificationDto> scheduleMultipleNotification( @PathVariable String projectId,
                                                                            @PathVariable String userId,
                                                                            @Valid @RequestBody List<FcmNotificationDto> notifications) {

        return null;
    }


    @PostMapping("/" + Paths.PROJECT_PATH + "/{projectId}/" + Paths.USER_PATH + "/{userId}/notifications/now")
    public ResponseEntity<FcmNotificationDto> scheduleNotificationNow( @PathVariable String projectId,
                                                                       @PathVariable String userId,
                                                                       @Valid @RequestBody FcmNotificationDto notification) {

        // No need to add to the database, Directly send via Firebase
        return null;

    }


    @DeleteMapping("/" + Paths.PROJECT_PATH + "/{projectId}/" + Paths.USER_PATH + "/{userId}/" + Paths.NOTIFICATION_PATH)
    public ResponseEntity deleteNotificationsForUser( @PathVariable String projectId,
                                                      @PathVariable String userId) {

        this.notificationService.removeNotificationsForUser(projectId, userId);
        return ResponseEntity.ok().build();
    }
}
