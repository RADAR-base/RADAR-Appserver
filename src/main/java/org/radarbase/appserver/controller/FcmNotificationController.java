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

import java.time.LocalDateTime;
import javax.validation.Valid;
import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.radarbase.appserver.dto.fcm.FcmNotifications;
import org.radarbase.appserver.service.FcmNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Resource Endpoint for getting and adding (scheduling) notifications on Firbase Cloud Messaging.
 *
 * @author yatharthranjan
 */
@RestController
public class FcmNotificationController {

  @Autowired private transient FcmNotificationService notificationService;

  @GetMapping("/" + Paths.NOTIFICATION_PATH)
  public ResponseEntity<FcmNotifications> getAllNotifications() {
    return ResponseEntity.ok(this.notificationService.getAllNotifications());
  }

  @GetMapping("/" + Paths.NOTIFICATION_PATH + "/{id}")
  public ResponseEntity<FcmNotificationDto> getNotificationUsingId(@Valid @PathVariable Long id) {
    return ResponseEntity.ok(this.notificationService.getNotificationById(id));
  }
  // TODO: get notifications based on other params. Maybe use projections ?
  @GetMapping("/" + Paths.NOTIFICATION_PATH + "/filtered")
  public ResponseEntity<FcmNotifications> getFilteredNotifications(
      @Valid @RequestParam(value = "type", required = false) String type,
      @Valid @RequestParam(value = "delivered", required = false) boolean delivered,
      @Valid @RequestParam(value = "ttlSeconds", required = false) int ttlSeconds,
      @Valid @RequestParam(value = "startTime", required = false) LocalDateTime startTime,
      @Valid @RequestParam(value = "endTime", required = false) LocalDateTime endTime,
      @Valid @RequestParam(value = "limit", required = false) int limit) {
    return ResponseEntity.ok(
        this.notificationService.getFilteredNotifications(
            type, delivered, ttlSeconds, startTime, endTime, limit));
  }

  @GetMapping(
      value =
          "/"
              + Paths.PROJECT_PATH
              + "/"
              + Paths.PROJECT_ID_CONSTANT
              + "/"
              + Paths.USER_PATH
              + "/"
              + Paths.SUBJECT_ID_CONSTANT
              + "/"
              + Paths.NOTIFICATION_PATH)
  public ResponseEntity<FcmNotifications> getNotificationsUsingProjectIdAndSubjectId(
      @Valid @PathVariable(Paths.PROJECT_ID_CONSTANT) String projectId,
      @Valid @PathVariable(Paths.SUBJECT_ID_CONSTANT) String subjectId) {
    return ResponseEntity.ok(
        this.notificationService.getNotificationsByProjectIdAndSubjectId(projectId, subjectId));
  }

  @GetMapping(
      "/" + Paths.PROJECT_PATH + "/" + Paths.PROJECT_ID_CONSTANT + "/" + Paths.NOTIFICATION_PATH)
  public ResponseEntity<FcmNotifications> getNotificationsUsingProjectId(
      @Valid @PathVariable(Paths.PROJECT_ID_CONSTANT) String projectId) {
    return ResponseEntity.ok(this.notificationService.getNotificationsByProjectId(projectId));
  }

  @GetMapping(
      "/" + Paths.USER_PATH + "/" + Paths.SUBJECT_ID_CONSTANT + "/" + Paths.NOTIFICATION_PATH)
  public ResponseEntity<FcmNotifications> getNotificationsUsingSubjectId(
      @Valid @PathVariable(Paths.SUBJECT_ID_CONSTANT) String subjectId) {
    return ResponseEntity.ok(this.notificationService.getNotificationsBySubjectId(subjectId));
  }

  @PostMapping(
      "/"
          + Paths.PROJECT_PATH
          + "/"
          + Paths.PROJECT_ID_CONSTANT
          + "/"
          + Paths.USER_PATH
          + "/"
          + Paths.SUBJECT_ID_CONSTANT
          + "/"
          + Paths.NOTIFICATION_PATH)
  public ResponseEntity<FcmNotificationDto> addSingleNotification(
      @PathVariable String projectId,
      @PathVariable String subjectId,
      @Valid @RequestBody FcmNotificationDto notification) {
    return ResponseEntity.ok(
        this.notificationService.addNotification(notification, subjectId, projectId));
  }

  @PutMapping(
      "/"
          + Paths.PROJECT_PATH
          + "/"
          + Paths.PROJECT_ID_CONSTANT
          + "/"
          + Paths.USER_PATH
          + "/"
          + Paths.SUBJECT_ID_CONSTANT
          + "/"
          + Paths.NOTIFICATION_PATH)
  public ResponseEntity updateNotification(
      @PathVariable String projectId,
      @PathVariable String subjectId,
      @Valid @RequestBody FcmNotificationDto notification) {

    return ResponseEntity.ok(
        this.notificationService.updateNotification(notification, subjectId, projectId));
  }

  @DeleteMapping(
      "/"
          + Paths.PROJECT_PATH
          + "/"
          + Paths.PROJECT_ID_CONSTANT
          + "/"
          + Paths.USER_PATH
          + "/"
          + Paths.SUBJECT_ID_CONSTANT
          + "/"
          + Paths.NOTIFICATION_PATH)
  public ResponseEntity deleteNotificationsForUser(
      @PathVariable String projectId, @PathVariable String subjectId) {

    this.notificationService.removeNotificationsForUser(projectId, subjectId);
    return ResponseEntity.ok().build();
  }
}
