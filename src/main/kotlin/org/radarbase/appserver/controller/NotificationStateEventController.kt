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

import org.radarbase.appserver.config.AuthConfig.AuthEntities
import org.radarbase.appserver.config.AuthConfig.AuthPermissions
import org.radarbase.appserver.dto.NotificationStateEventDto
import org.radarbase.appserver.service.NotificationStateEventService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import radar.spring.auth.common.Authorized
import radar.spring.auth.common.PermissionOn
import javax.naming.SizeLimitExceededException

@CrossOrigin
@RestController
class NotificationStateEventController(private val notificationStateEventService: NotificationStateEventService) {
    @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.PROJECT)
    @GetMapping(
        value = [("/"
                + PathsUtil.MESSAGING_NOTIFICATION_PATH
                + "/"
                + PathsUtil.NOTIFICATION_ID_CONSTANT
                + "/"
                + PathsUtil.NOTIFICATION_STATE_EVENTS_PATH)]
    )
    fun getNotificationStateEventsByNotificationId(
        @PathVariable notificationId: Long
    ): ResponseEntity<List<NotificationStateEventDto>> {
        return ResponseEntity.ok(
            notificationStateEventService.getNotificationStateEventsByNotificationId(notificationId)
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
                + PathsUtil.MESSAGING_NOTIFICATION_PATH
                + "/"
                + PathsUtil.NOTIFICATION_ID_CONSTANT
                + "/"
                + PathsUtil.NOTIFICATION_STATE_EVENTS_PATH)]
    )
    fun getNotificationStateEvents(
        @PathVariable projectId: String?,
        @PathVariable subjectId: String?,
        @PathVariable notificationId: Long
    ): ResponseEntity<List<NotificationStateEventDto>> {
        return ResponseEntity.ok(
            notificationStateEventService.getNotificationStateEvents(
                projectId, subjectId, notificationId
            )
        )
    }

    @Authorized(permission = AuthPermissions.UPDATE, entity = AuthEntities.SUBJECT, permissionOn = PermissionOn.SUBJECT)
    @PostMapping(
        value = [("/"
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
                + "/"
                + PathsUtil.NOTIFICATION_STATE_EVENTS_PATH)]
    )
    @Throws(SizeLimitExceededException::class)
    fun postNotificationStateEvent(
        @PathVariable projectId: String?,
        @PathVariable subjectId: String?,
        @PathVariable notificationId: Long,
        @RequestBody notificationStateEventDto: NotificationStateEventDto
    ): ResponseEntity<List<NotificationStateEventDto>> {
        notificationStateEventService.publishNotificationStateEventExternal(
            projectId, subjectId, notificationId, notificationStateEventDto
        )
        return getNotificationStateEvents(projectId, subjectId, notificationId)
    }
}
