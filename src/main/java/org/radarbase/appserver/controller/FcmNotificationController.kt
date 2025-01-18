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
import org.radarbase.appserver.dto.fcm.FcmNotificationDto
import org.radarbase.appserver.dto.fcm.FcmNotifications
import org.radarbase.appserver.service.FcmNotificationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import radar.spring.auth.common.Authorized
import radar.spring.auth.common.PermissionOn
import java.net.URI
import java.net.URISyntaxException
import java.time.LocalDateTime

/**
 * Resource Endpoint for getting and adding (scheduling) notifications on Firebase Cloud Messaging.
 *
 * @author yatharthranjan
 */
@CrossOrigin
@RestController
class FcmNotificationController(private val notificationService: FcmNotificationService) {

    @GetMapping("/" + PathsUtil.MESSAGING_NOTIFICATION_PATH)
    @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.PROJECT)
    fun  getAllNotifications(): ResponseEntity<FcmNotifications> {
        return ResponseEntity.ok(this.notificationService.getAllNotifications())
    }

    @Authorized(permission = AuthPermissions.UPDATE, entity = AuthEntities.SUBJECT)
    @GetMapping("/" + PathsUtil.MESSAGING_NOTIFICATION_PATH + "/{id}")
    fun getNotificationUsingId(@PathVariable id: @Valid Long): ResponseEntity<FcmNotificationDto> {
        return ResponseEntity.ok(this.notificationService.getNotificationById(id))
    }

    // TODO: get notifications based on other params. Maybe use projections ?
    @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.PROJECT)
    @GetMapping("/" + PathsUtil.MESSAGING_NOTIFICATION_PATH + "/filtered")
    fun getFilteredNotifications(
        @RequestParam(value = "type", required = false) @Valid type: String?,
        @RequestParam(value = "delivered", required = false) @Valid delivered: Boolean,
        @RequestParam(value = "ttlSeconds", required = false) @Valid ttlSeconds: Int,
        @RequestParam(value = "startTime", required = false) @Valid startTime: LocalDateTime?,
        @RequestParam(value = "endTime", required = false) @Valid endTime: LocalDateTime?,
        @RequestParam(value = "limit", required = false) @Valid limit: Int
    ): ResponseEntity<FcmNotifications> {
        return ResponseEntity.ok(
            this.notificationService.getFilteredNotifications(
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
                + PathsUtil.MESSAGING_NOTIFICATION_PATH)]
    )
    fun getNotificationsUsingProjectIdAndSubjectId(
        @PathVariable @Valid projectId: String, @PathVariable @Valid subjectId: String
    ): ResponseEntity<FcmNotifications> {
        return ResponseEntity.ok(
            this.notificationService.getNotificationsByProjectIdAndSubjectId(projectId, subjectId)
        )
    }

    @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.SUBJECT, permissionOn = PermissionOn.PROJECT)
    @GetMapping(
        ("/"
                + PathsUtil.PROJECT_PATH
                + "/"
                + PathsUtil.PROJECT_ID_CONSTANT
                + "/"
                + PathsUtil.MESSAGING_NOTIFICATION_PATH)
    )
    fun getNotificationsUsingProjectId(
        @PathVariable @Valid projectId: String
    ): ResponseEntity<FcmNotifications> {
        return ResponseEntity.ok(this.notificationService.getNotificationsByProjectId(projectId))
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
                + PathsUtil.MESSAGING_NOTIFICATION_PATH)
    )
    @Throws(URISyntaxException::class)
    fun addSingleNotification(
        @PathVariable projectId: String?,
        @PathVariable subjectId: String?,
        @RequestParam(required = false, defaultValue = "true") schedule: Boolean,
        @RequestBody notification: @Valid FcmNotificationDto
    ): ResponseEntity<FcmNotificationDto> {
        val notificationDto =
            this.notificationService.addNotification(notification, subjectId, projectId, schedule)
        return ResponseEntity.created(
            URI("/" + PathsUtil.MESSAGING_NOTIFICATION_PATH + "/" + notificationDto.id)
        )
            .body(notificationDto)
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
                + PathsUtil.MESSAGING_NOTIFICATION_PATH
                + "/schedule")
    )
    @Throws(URISyntaxException::class)
    fun scheduleUserNotifications(
        @PathVariable projectId: String,
        @PathVariable subjectId: String
    ): ResponseEntity<FcmNotifications> {
        return ResponseEntity.ok(
            this.notificationService.scheduleAllUserNotifications(subjectId, projectId)
        )
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
                + PathsUtil.MESSAGING_NOTIFICATION_PATH
                + "/"
                + PathsUtil.NOTIFICATION_ID_CONSTANT
                + "/schedule")
    )
    @Throws(URISyntaxException::class)
    fun scheduleUserNotification(
        @PathVariable projectId: String,
        @PathVariable subjectId: String,
        @PathVariable notificationId: Long
    ): ResponseEntity<FcmNotificationDto> {
        return ResponseEntity.ok(
            this.notificationService.scheduleNotification(subjectId, projectId, notificationId)
        )
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
                + PathsUtil.MESSAGING_NOTIFICATION_PATH
                + "/batch")
    )
    fun addBatchNotifications(
        @PathVariable projectId: String,
        @PathVariable subjectId: String,
        @RequestParam(required = false, defaultValue = "true") schedule: Boolean,
        @RequestBody @Valid notifications: FcmNotifications
    ): ResponseEntity<FcmNotifications> {
        return ResponseEntity.ok(
            this.notificationService.addNotifications(notifications, subjectId, projectId, schedule)
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
                + PathsUtil.MESSAGING_NOTIFICATION_PATH)
    )
    fun updateNotification(
        @PathVariable projectId: String,
        @PathVariable subjectId: String,
        @RequestBody @Valid notification: FcmNotificationDto
    ): ResponseEntity<FcmNotificationDto> {
        return ResponseEntity.ok(
            this.notificationService.updateNotification(notification, subjectId, projectId)
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
                + PathsUtil.MESSAGING_NOTIFICATION_PATH
                + "/"
                + PathsUtil.ALL_KEYWORD)
    )
    fun deleteNotificationsForUser(
        @PathVariable projectId: String, @PathVariable subjectId: String
    ): ResponseEntity<*> {
        this.notificationService.removeNotificationsForUser(projectId, subjectId)
        return ResponseEntity.ok().build<Any?>()
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
                + PathsUtil.MESSAGING_NOTIFICATION_PATH
                + "/"
                + PathsUtil.NOTIFICATION_ID_CONSTANT)
    )
    fun deleteNotificationUsingProjectIdAndSubjectIdAndNotificationId(
        @PathVariable projectId: String, @PathVariable subjectId: String, @PathVariable notificationId: Long
    ): ResponseEntity<Any> {
        this.notificationService.deleteNotificationByProjectIdAndSubjectIdAndNotificationId(
            projectId, subjectId, notificationId
        )
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
                + PathsUtil.MESSAGING_NOTIFICATION_PATH
                + "/"
                + PathsUtil.TASK_PATH
                + "/{id}")
    )
    fun deleteNotificationUsingProjectIdAndSubjectIdAndTaskId(
        @PathVariable projectId: String, @PathVariable subjectId: String, @PathVariable id: Long
    ): ResponseEntity<Any> {
        this.notificationService.removeNotificationsForUserUsingTaskId(
            projectId, subjectId, id
        )
        return ResponseEntity.ok().build()
    }
}
