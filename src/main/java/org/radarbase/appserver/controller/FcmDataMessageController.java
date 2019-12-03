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

import org.radarbase.appserver.dto.fcm.FcmDataMessageDto;
import org.radarbase.appserver.dto.fcm.FcmDataMessages;
import org.radarbase.appserver.service.FcmDataMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;

/**
 * Resource Endpoint for getting and adding (scheduling) data messages on Firebase Cloud Messaging.
 *
 * @author yatharthranjan
 */
@RestController
public class FcmDataMessageController {

    @Autowired
    private transient FcmDataMessageService dataMessageService;

    @GetMapping("/" + PathsUtil.MESSAGING_DATA_PATH)
    @PreAuthorize(AuthConstantsUtil.IS_ADMIN)
    public ResponseEntity<FcmDataMessages> getAllDataMessages() {
        return ResponseEntity.ok(this.dataMessageService.getAllDataMessages());
    }

    @PreAuthorize(AuthConstantsUtil.IS_ADMIN)
    @GetMapping("/" + PathsUtil.MESSAGING_DATA_PATH + "/{id}")
    public ResponseEntity<FcmDataMessageDto> getDataMessageUsingId(@Valid @PathVariable Long id) {
        return ResponseEntity.ok(this.dataMessageService.getDataMessageById(id));
    }

    // TODO: get notifications/data messages based on other params. Maybe use projections ?
    @GetMapping("/" + PathsUtil.MESSAGING_DATA_PATH + "/filtered")
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

    @PreAuthorize(
            "hasPermissionOnProject(T(org.radarcns.auth.authorization.Permission).SUBJECT_READ, "
                    + AuthConstantsUtil.ACCESSOR
                    + AuthConstantsUtil.PROJECT_ID
                    + ")")
    @GetMapping(
            "/" + PathsUtil.PROJECT_PATH + "/" + PathsUtil.PROJECT_ID_CONSTANT + "/"
                    + PathsUtil.MESSAGING_DATA_PATH)
    public ResponseEntity<FcmDataMessages> getDataMessagesUsingProjectId(
            @Valid @PathVariable String projectId) {
        return ResponseEntity.ok(this.dataMessageService.getDataMessagesByProjectId(projectId));
    }


    // TODO: Edit this as this needs to be on the Subject level.
    @PreAuthorize("hasPermission(T(org.radarcns.auth.authorization.Permission).SUBJECT_READ" + ")")
    @GetMapping(
            "/" + PathsUtil.USER_PATH + "/" + PathsUtil.SUBJECT_ID_CONSTANT + "/"
                    + PathsUtil.MESSAGING_DATA_PATH)
    public ResponseEntity<FcmDataMessages> getDataMessagesUsingSubjectId(
            @Valid @PathVariable String subjectId) {
        return ResponseEntity.ok(this.dataMessageService.getDataMessagesBySubjectId(subjectId));
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

    @PreAuthorize(
            AuthConstantsUtil.PERMISSION_ON_SUBJECT_MEASUREMENT_CREATE
                    + AuthConstantsUtil.ACCESSOR
                    + AuthConstantsUtil.PROJECT_ID
                    + ", "
                    + AuthConstantsUtil.ACCESSOR
                    + AuthConstantsUtil.SUBJECT_ID
                    + ")")
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

    @PreAuthorize(
            AuthConstantsUtil.PERMISSION_ON_SUBJECT_MEASUREMENT_CREATE
                    + AuthConstantsUtil.ACCESSOR
                    + AuthConstantsUtil.PROJECT_ID
                    + ", "
                    + AuthConstantsUtil.ACCESSOR
                    + AuthConstantsUtil.SUBJECT_ID
                    + ")")
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
    public ResponseEntity updateDataMessage(
            @PathVariable String projectId,
            @PathVariable String subjectId,
            @Valid @RequestBody FcmDataMessageDto dataMessage) {

        return ResponseEntity.ok(
                this.dataMessageService.updateDataMessage(dataMessage, subjectId, projectId));
    }

    @PreAuthorize(
            AuthConstantsUtil.PERMISSION_ON_SUBJECT_MEASUREMENT_CREATE
                    + AuthConstantsUtil.ACCESSOR
                    + AuthConstantsUtil.PROJECT_ID
                    + ", "
                    + AuthConstantsUtil.ACCESSOR
                    + AuthConstantsUtil.SUBJECT_ID
                    + ")")
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
                    + PathsUtil.MESSAGING_DATA_PATH)
    public ResponseEntity deleteDataMessagesForUser(
            @PathVariable String projectId, @PathVariable String subjectId) {

        this.dataMessageService.removeDataMessagesForUser(projectId, subjectId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize(
            AuthConstantsUtil.PERMISSION_ON_SUBJECT_MEASUREMENT_CREATE
                    + AuthConstantsUtil.ACCESSOR
                    + AuthConstantsUtil.PROJECT_ID
                    + ", "
                    + AuthConstantsUtil.ACCESSOR
                    + AuthConstantsUtil.SUBJECT_ID
                    + ")")
    @DeleteMapping("/" + PathsUtil.MESSAGING_DATA_PATH + "/{fcmMessageId}")
    public ResponseEntity deleteDataMessageUsingFcmMessageId(
            @PathVariable String fcmMessageId) {

        this.dataMessageService.deleteDataMessageByFcmMessageId(fcmMessageId);
        return ResponseEntity.ok().build();
    }
}

