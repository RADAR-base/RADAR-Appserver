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
package org.radarbase.appserver.controller

import jakarta.validation.Valid
import org.radarbase.appserver.config.AuthConfig.AuthEntities
import org.radarbase.appserver.config.AuthConfig.AuthPermissions
import org.radarbase.appserver.dto.fcm.FcmDataMessageDto
import org.radarbase.appserver.dto.fcm.FcmDataMessages
import org.radarbase.appserver.service.FcmDataMessageService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import radar.spring.auth.common.Authorized
import radar.spring.auth.common.PermissionOn
import java.net.URI
import java.net.URISyntaxException
import java.time.LocalDateTime

/**
 * Resource Endpoint for getting and adding (scheduling) data messages on Firebase Cloud Messaging.
 *
 * @author yatharthranjan
 */
@CrossOrigin
@RestController
class FcmDataMessageController(private val dataMessageService: FcmDataMessageService) {

    @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.PROJECT)
    @GetMapping("/" + PathsUtil.MESSAGING_DATA_PATH)
    fun getAllDataMessages(): ResponseEntity<FcmDataMessages> {
        return ResponseEntity.ok(this.dataMessageService.getAllDataMessages())
    }

    @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.SUBJECT)
    @GetMapping("/" + PathsUtil.MESSAGING_DATA_PATH + "/{id}")
    fun getDataMessageUsingId(@PathVariable @Valid id: Long): ResponseEntity<FcmDataMessageDto> {
        return ResponseEntity.ok(this.dataMessageService.getDataMessageById(id))
    }

    // TODO: get notifications/data messages based on other params. Maybe use projections ?
    @GetMapping("/" + PathsUtil.MESSAGING_DATA_PATH + "/filtered")
    @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.PROJECT)
    fun getFilteredDataMessages(
        @RequestParam(value = "type", required = false) @Valid type: String,
        @RequestParam(value = "delivered", required = false) @Valid delivered: Boolean,
        @RequestParam(value = "ttlSeconds", required = false) @Valid ttlSeconds: Int,
        @RequestParam(value = "startTime", required = false) @Valid startTime: LocalDateTime,
        @RequestParam(value = "endTime", required = false) @Valid endTime: LocalDateTime,
        @RequestParam(value = "limit", required = false) @Valid limit: Int
    ): ResponseEntity<FcmDataMessages> {
        return ResponseEntity.ok(
            this.dataMessageService.getFilteredDataMessages(
                type, delivered, ttlSeconds, startTime, endTime, limit
            )
        )
    }

    @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.SUBJECT, permissionOn = PermissionOn.SUBJECT)
    @GetMapping(
        value = [("/"
                + PathsUtil.PROJECT_PATH
                + "/"
                + PathsUtil.PROJECT_ID_CONSTANT
                + "/"
                + PathsUtil.USER_PATH
                + "/"
                + PathsUtil.SUBJECT_ID_CONSTANT
                + "/"
                + PathsUtil.MESSAGING_DATA_PATH)]
    )
    fun getDataMessagesUsingProjectIdAndSubjectId(
        @PathVariable @Valid projectId: String, @PathVariable @Valid subjectId: String
    ): ResponseEntity<FcmDataMessages> {
        return ResponseEntity.ok<FcmDataMessages>(
            this.dataMessageService.getDataMessagesByProjectIdAndSubjectId(projectId, subjectId)
        )
    }

    @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.SUBJECT, permissionOn = PermissionOn.PROJECT)
    @GetMapping(
        ("/"
                + PathsUtil.PROJECT_PATH
                + "/"
                + PathsUtil.PROJECT_ID_CONSTANT
                + "/"
                + PathsUtil.MESSAGING_DATA_PATH)
    )
    fun getDataMessagesUsingProjectId(
        @PathVariable @Valid projectId: String
    ): ResponseEntity<FcmDataMessages> {
        return ResponseEntity.ok<FcmDataMessages>(this.dataMessageService.getDataMessagesByProjectId(projectId))
    }

    @Authorized(permission = AuthPermissions.UPDATE, entity = AuthEntities.SUBJECT, permissionOn = PermissionOn.SUBJECT)
    @PostMapping(
        ("/"
                + PathsUtil.PROJECT_PATH
                + "/"
                + PathsUtil.PROJECT_ID_CONSTANT
                + "/"
                + PathsUtil.USER_PATH
                + "/"
                + PathsUtil.SUBJECT_ID_CONSTANT
                + "/"
                + PathsUtil.MESSAGING_DATA_PATH)
    )
    @Throws(URISyntaxException::class)
    fun addSingleDataMessage(
        @PathVariable projectId: String,
        @PathVariable subjectId: String,
        @RequestBody @Valid dataMessage: FcmDataMessageDto
    ): ResponseEntity<FcmDataMessageDto> {
        val dataMessageDto =
            this.dataMessageService.addDataMessage(dataMessage, subjectId, projectId)
        return ResponseEntity.created(
            URI("/" + PathsUtil.MESSAGING_DATA_PATH + "/" + dataMessageDto.id)
        )
            .body(dataMessageDto)
    }

    @Authorized(permission = AuthPermissions.UPDATE, entity = AuthEntities.SUBJECT, permissionOn = PermissionOn.SUBJECT)
    @PostMapping(
        ("/"
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
    )
    fun addBatchDataMessages(
        @PathVariable projectId: String,
        @PathVariable subjectId: String,
        @RequestBody @Valid dataMessages: FcmDataMessages
    ): ResponseEntity<FcmDataMessages> {
        return ResponseEntity.ok(
            this.dataMessageService.addDataMessages(dataMessages, subjectId, projectId)
        )
    }

    @Authorized(permission = AuthPermissions.UPDATE, entity = AuthEntities.SUBJECT, permissionOn = PermissionOn.SUBJECT)
    @PutMapping(
        ("/"
                + PathsUtil.PROJECT_PATH
                + "/"
                + PathsUtil.PROJECT_ID_CONSTANT
                + "/"
                + PathsUtil.USER_PATH
                + "/"
                + PathsUtil.SUBJECT_ID_CONSTANT
                + "/"
                + PathsUtil.MESSAGING_DATA_PATH)
    )
    fun updateDataMessage(
        @PathVariable projectId: String,
        @PathVariable subjectId: String,
        @RequestBody @Valid dataMessage: FcmDataMessageDto
    ): ResponseEntity<FcmDataMessageDto> {
        return ResponseEntity.ok(
            this.dataMessageService.updateDataMessage(dataMessage, subjectId, projectId)
        )
    }

    @Authorized(permission = AuthPermissions.UPDATE, entity = AuthEntities.SUBJECT, permissionOn = PermissionOn.SUBJECT)
    @DeleteMapping(
        ("/"
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
    )
    fun deleteDataMessagesForUser(
        @PathVariable projectId: String, @PathVariable subjectId: String
    ): ResponseEntity<Any> {
        this.dataMessageService.removeDataMessagesForUser(projectId, subjectId)
        return ResponseEntity.ok().build()
    }

    @Authorized(permission = AuthPermissions.UPDATE, entity = AuthEntities.SUBJECT, permissionOn = PermissionOn.SUBJECT)
    @DeleteMapping(
        ("/"
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
    )
    fun deleteDataMessageUsingProjectIdAndSubjectIdAndDataMessageId(
        @PathVariable projectId: String, @PathVariable subjectId: String, @PathVariable id: Long
    ): ResponseEntity<Any> {
        this.dataMessageService.deleteDataMessageByProjectIdAndSubjectIdAndDataMessageId(
            projectId, subjectId, id
        )
        return ResponseEntity.ok().build()
    }
}
