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

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import javax.validation.Valid;
import org.radarbase.appserver.config.AuthConfig.AuthEntities;
import org.radarbase.appserver.config.AuthConfig.AuthPermissions;
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

  @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.PROJECT)
  @GetMapping("/" + PathsUtil.MESSAGING_NOTIFICATION_PATH)
  public ResponseEntity<FcmNotifications> getAllNotifications() {
    return ResponseEntity.ok(this.notificationService.getAllNotifications());
  }

  @Authorized(permission = AuthPermissions.UPDATE, entity = AuthEntities.SUBJECT)
  @GetMapping("/" + PathsUtil.MESSAGING_NOTIFICATION_PATH + "/{id}")
  public ResponseEntity<FcmNotificationDto> getNotificationUsingId(@Valid @PathVariable Long id) {
    return ResponseEntity.ok(this.notificationService.getNotificationById(id));
  }
  // TODO: get notifications based on other params. Maybe use projections ?
  @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.PROJECT)
  @GetMapping("/" + PathsUtil.MESSAGING_NOTIFICATION_PATH + "/filtered")
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

  @Authorized(
      permission = AuthPermissions.READ,
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
              + PathsUtil.MESSAGING_NOTIFICATION_PATH)
  public ResponseEntity<FcmNotifications> getNotificationsUsingProjectIdAndSubjectId(
      @Valid @PathVariable String projectId, @Valid @PathVariable String subjectId) {
    return ResponseEntity.ok(
        this.notificationService.getNotificationsByProjectIdAndSubjectId(projectId, subjectId));
  }

  @Authorized(
      permission = AuthPermissions.READ,
      entity = AuthEntities.SUBJECT,
      permissionOn = PermissionOn.PROJECT)
  @GetMapping(
      "/"
          + PathsUtil.PROJECT_PATH
          + "/"
          + PathsUtil.PROJECT_ID_CONSTANT
          + "/"
          + PathsUtil.MESSAGING_NOTIFICATION_PATH)
  public ResponseEntity<FcmNotifications> getNotificationsUsingProjectId(
      @Valid @PathVariable String projectId) {
    return ResponseEntity.ok(this.notificationService.getNotificationsByProjectId(projectId));
  }

  @Authorized(
      permission = AuthPermissions.UPDATE,
      entity = AuthEntities.SUBJECT,
      permissionOn = PermissionOn.SUBJECT)
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
          + PathsUtil.MESSAGING_NOTIFICATION_PATH)
  public ResponseEntity<FcmNotificationDto> addSingleNotification(
      @PathVariable String projectId,
      @PathVariable String subjectId,
      @RequestParam(defaultValue = "") String schedule,
      @Valid @RequestBody FcmNotificationDto notification)
      throws URISyntaxException {
    FcmNotificationDto notificationDto =
        this.notificationService.addNotification(notification, subjectId, projectId, schedule);
    return ResponseEntity.created(
            new URI("/" + PathsUtil.MESSAGING_NOTIFICATION_PATH + "/" + notificationDto.getId()))
        .body(notificationDto);
  }

  @Authorized(
      permission = AuthPermissions.UPDATE,
      entity = AuthEntities.SUBJECT,
      permissionOn = PermissionOn.SUBJECT)
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
          + PathsUtil.MESSAGING_NOTIFICATION_PATH
          + "/schedule")
  public ResponseEntity<FcmNotifications> scheduleUserNotifications(
      @PathVariable String projectId,
      @PathVariable String subjectId)
      throws URISyntaxException {
        return ResponseEntity.ok(
            this.notificationService.scheduleAllUserNotifications(subjectId, projectId));
    }

    @Authorized(
        permission = AuthPermissions.UPDATE,
        entity = AuthEntities.SUBJECT,
        permissionOn = PermissionOn.SUBJECT)
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
            + PathsUtil.MESSAGING_NOTIFICATION_PATH
            + "/"
            + PathsUtil.NOTIFICATION_ID_CONSTANT
            + "/schedule")
    public ResponseEntity<FcmNotificationDto> scheduleUserNotification(
        @PathVariable String projectId,
        @PathVariable String subjectId,
        @PathVariable long notificationId)
        throws URISyntaxException {
          return ResponseEntity.ok(
              this.notificationService.scheduleNotification(subjectId, projectId, notificationId));
      }

  @Authorized(
      permission = AuthPermissions.UPDATE,
      entity = AuthEntities.SUBJECT,
      permissionOn = PermissionOn.SUBJECT)
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
          + PathsUtil.MESSAGING_NOTIFICATION_PATH
          + "/batch")
  public ResponseEntity<FcmNotifications> addBatchNotifications(
      @PathVariable String projectId,
      @PathVariable String subjectId,
      @RequestParam(defaultValue = "") String schedule,
      @Valid @RequestBody FcmNotifications notifications) {
    return ResponseEntity.ok(
        this.notificationService.addNotifications(notifications, subjectId, projectId, schedule));
  }

  @Authorized(
      permission = AuthPermissions.UPDATE,
      entity = AuthEntities.SUBJECT,
      permissionOn = PermissionOn.SUBJECT)
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
          + PathsUtil.MESSAGING_NOTIFICATION_PATH)
  public ResponseEntity<FcmNotificationDto> updateNotification(
      @PathVariable String projectId,
      @PathVariable String subjectId,
      @Valid @RequestBody FcmNotificationDto notification) {

    return ResponseEntity.ok(
        this.notificationService.updateNotification(notification, subjectId, projectId));
  }

  @Authorized(
      permission = AuthPermissions.UPDATE,
      entity = AuthEntities.SUBJECT,
      permissionOn = PermissionOn.SUBJECT)
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
          + PathsUtil.MESSAGING_NOTIFICATION_PATH)
  public ResponseEntity deleteNotificationsForUser(
      @PathVariable String projectId, @PathVariable String subjectId) {

    this.notificationService.removeNotificationsForUser(projectId, subjectId);
    return ResponseEntity.ok().build();
  }

  @Authorized(
      permission = AuthPermissions.UPDATE,
      entity = AuthEntities.SUBJECT,
      permissionOn = PermissionOn.SUBJECT)
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
          + PathsUtil.MESSAGING_NOTIFICATION_PATH
          + "/{id}")
  public ResponseEntity deleteNotificationUsingProjectIdAndSubjectIdAndNotificationId(
      @PathVariable String projectId, @PathVariable String subjectId, @PathVariable Long id) {

    this.notificationService.deleteNotificationByProjectIdAndSubjectIdAndNotificationId(
        projectId, subjectId, id);
    return ResponseEntity.ok().build();
  }
}
