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

import java.util.List;
import org.radarbase.appserver.dto.NotificationStateEventDto;
import org.radarbase.appserver.service.NotificationStateEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class NotificationStateEventController {

  @Autowired private transient NotificationStateEventService notificationStateEventService;

  @PreAuthorize(AuthConstantsUtil.IS_ADMIN)
  @GetMapping(
      value =
          "/"
              + Paths.NOTIFICATION_PATH
              + "/"
              + Paths.NOTIFICATION_ID_CONSTANT
              + "/"
              + Paths.NOTIFICATION_STATE_EVENTS_PATH)
  public ResponseEntity<List<NotificationStateEventDto>> getNotificationStateEventsByNotificationId(
      @PathVariable long notificationId) {
    return ResponseEntity.ok(
        notificationStateEventService.getNotificationStateEventsByNotificationId(notificationId));
  }

  @PreAuthorize(
      "hasPermissionOnSubject(T(org.radarcns.auth.authorization.Permission).SUBJECT_READ, "
          + AuthConstantsUtil.ACCESSOR
          + AuthConstantsUtil.PROJECT_ID
          + ", "
          + AuthConstantsUtil.ACCESSOR
          + AuthConstantsUtil.SUBJECT_ID
          + ")")
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
              + Paths.NOTIFICATION_PATH
              + "/"
              + Paths.NOTIFICATION_ID_CONSTANT
              + "/"
              + Paths.NOTIFICATION_STATE_EVENTS_PATH)
  public ResponseEntity<List<NotificationStateEventDto>> getNotificationStateEvents(
      @PathVariable String projectId,
      @PathVariable String subjectId,
      @PathVariable long notificationId) {
    return ResponseEntity.ok(
        notificationStateEventService.getNotificationStateEvents(
            projectId, subjectId, notificationId));
  }

  @PreAuthorize(
      AuthConstantsUtil.PERMISSION_ON_SUBJECT_MEASUREMENT_CREATE
          + AuthConstantsUtil.ACCESSOR
          + AuthConstantsUtil.PROJECT_ID
          + ", "
          + AuthConstantsUtil.ACCESSOR
          + AuthConstantsUtil.SUBJECT_ID
          + ")")
  @PostMapping(
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
              + Paths.NOTIFICATION_PATH
              + "/"
              + Paths.NOTIFICATION_ID_CONSTANT
              + "/"
              + Paths.NOTIFICATION_STATE_EVENTS_PATH)
  public ResponseEntity<List<NotificationStateEventDto>> postNotificationStateEvent(
      @PathVariable String projectId,
      @PathVariable String subjectId,
      @PathVariable long notificationId,
      @RequestBody NotificationStateEventDto notificationStateEventDto) {

    notificationStateEventService.publishNotificationStateEventExternal(
        projectId, subjectId, notificationId, notificationStateEventDto);
    return getNotificationStateEvents(projectId, subjectId, notificationId);
  }
}
