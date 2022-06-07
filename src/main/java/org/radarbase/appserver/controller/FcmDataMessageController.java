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
import org.radarbase.appserver.dto.fcm.FcmDataMessageDto;
import org.radarbase.appserver.dto.fcm.FcmDataMessages;
import org.radarbase.appserver.service.FcmDataMessageService;
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
 * Resource Endpoint for getting and adding (scheduling) data messages on Firebase Cloud Messaging.
 *
 * @author yatharthranjan
 */
@RestController
public class FcmDataMessageController {

  private final transient FcmDataMessageService dataMessageService;

  public FcmDataMessageController(FcmDataMessageService dataMessageService) {
    this.dataMessageService = dataMessageService;
  }

  @GetMapping("/" + PathsUtil.MESSAGING_DATA_PATH)
  @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.PROJECT)
  public ResponseEntity<FcmDataMessages> getAllDataMessages() {
    return ResponseEntity.ok(this.dataMessageService.getAllDataMessages());
  }

  @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.SUBJECT)
  @GetMapping("/" + PathsUtil.MESSAGING_DATA_PATH + "/{id}")
  public ResponseEntity<FcmDataMessageDto> getDataMessageUsingId(@Valid @PathVariable Long id) {
    return ResponseEntity.ok(this.dataMessageService.getDataMessageById(id));
  }

  // TODO: get notifications/data messages based on other params. Maybe use projections ?
  @GetMapping("/" + PathsUtil.MESSAGING_DATA_PATH + "/filtered")
  @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.PROJECT)
  public ResponseEntity<FcmDataMessages> getFilteredDataMessages(
      @Valid @RequestParam(value = "type", required = false) String type,
      @Valid @RequestParam(value = "delivered", required = false) boolean delivered,
      @Valid @RequestParam(value = "ttlSeconds", required = false) int ttlSeconds,
      @Valid @RequestParam(value = "startTime", required = false) LocalDateTime startTime,
      @Valid @RequestParam(value = "endTime", required = false) LocalDateTime endTime,
      @Valid @RequestParam(value = "limit", required = false) int limit) {
    return ResponseEntity.ok(
        this.dataMessageService.getFilteredDataMessages(
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
              + PathsUtil.MESSAGING_DATA_PATH)
  public ResponseEntity<FcmDataMessages> getDataMessagesUsingProjectIdAndSubjectId(
      @Valid @PathVariable String projectId, @Valid @PathVariable String subjectId) {
    return ResponseEntity.ok(
        this.dataMessageService.getDataMessagesByProjectIdAndSubjectId(projectId, subjectId));
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
          + PathsUtil.MESSAGING_DATA_PATH)
  public ResponseEntity<FcmDataMessages> getDataMessagesUsingProjectId(
      @Valid @PathVariable String projectId) {
    return ResponseEntity.ok(this.dataMessageService.getDataMessagesByProjectId(projectId));
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
          + PathsUtil.MESSAGING_DATA_PATH)
  public ResponseEntity<FcmDataMessageDto> addSingleDataMessage(
      @PathVariable String projectId,
      @PathVariable String subjectId,
      @Valid @RequestBody FcmDataMessageDto dataMessage)
      throws URISyntaxException {
    FcmDataMessageDto dataMessageDto =
        this.dataMessageService.addDataMessage(dataMessage, subjectId, projectId);
    return ResponseEntity.created(
            new URI("/" + PathsUtil.MESSAGING_DATA_PATH + "/" + dataMessageDto.getId()))
        .body(dataMessageDto);
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
          + PathsUtil.MESSAGING_DATA_PATH
          + "/batch")
  public ResponseEntity<FcmDataMessages> addBatchDataMessages(
      @PathVariable String projectId,
      @PathVariable String subjectId,
      @Valid @RequestBody FcmDataMessages dataMessages) {
    return ResponseEntity.ok(
        this.dataMessageService.addDataMessages(dataMessages, subjectId, projectId));
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
          + PathsUtil.MESSAGING_DATA_PATH)
  public ResponseEntity<FcmDataMessageDto> updateDataMessage(
      @PathVariable String projectId,
      @PathVariable String subjectId,
      @Valid @RequestBody FcmDataMessageDto dataMessage) {

    return ResponseEntity.ok(
        this.dataMessageService.updateDataMessage(dataMessage, subjectId, projectId));
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
          + PathsUtil.MESSAGING_DATA_PATH
          + "/"
          + PathsUtil.ALL_KEYWORD)
  public ResponseEntity deleteDataMessagesForUser(
      @PathVariable String projectId, @PathVariable String subjectId) {

    this.dataMessageService.removeDataMessagesForUser(projectId, subjectId);
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
          + PathsUtil.MESSAGING_DATA_PATH
          + "/{id}")
  public ResponseEntity deleteDataMessageUsingProjectIdAndSubjectIdAndDataMessageId(
      @PathVariable String projectId, @PathVariable String subjectId, @PathVariable Long id) {

    this.dataMessageService.deleteDataMessageByProjectIdandSubjectIdAndDataMessageId(
        projectId, subjectId, id);
    return ResponseEntity.ok().build();
  }
}
