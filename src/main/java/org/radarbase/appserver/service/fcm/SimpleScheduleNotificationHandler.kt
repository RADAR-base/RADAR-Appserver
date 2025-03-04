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
package org.radarbase.appserver.service.fcm

import org.radarbase.appserver.dto.ProjectDto
import org.radarbase.appserver.dto.fcm.FcmNotificationDto
import org.radarbase.appserver.dto.fcm.FcmUserDto
import org.radarbase.appserver.exception.AlreadyExistsException
import org.radarbase.appserver.exception.NotFoundException
import org.radarbase.appserver.service.FcmNotificationService
import org.radarbase.appserver.service.ProjectService
import org.radarbase.appserver.service.UserService
import org.slf4j.LoggerFactory

class SimpleScheduleNotificationHandler(
    private val notificationService: FcmNotificationService,
    private val projectService: ProjectService,
    private val userService: UserService
) : ScheduleNotificationHandler {
    override fun handleScheduleNotification(
        notificationDto: FcmNotificationDto, userDto: FcmUserDto, projectId: String?
    ) {
        try {
            notificationService.addNotification(notificationDto, userDto.subjectId, projectId)
        } catch (ex: NotFoundException) {
            if (ex.message!!.contains("Project")) {
                try {
                    projectService.addProject(ProjectDto(null, projectId, null, null))
                    userDto.projectId = projectId
                    userService.saveUserInProject(userDto)
                } catch (_: Exception) {
                    logger.warn("Exception while adding notification.", ex)
                }
            } else if (ex.message!!.contains("Subject")) {
                userDto.projectId = projectId
                userService.saveUserInProject(userDto)
            }
            notificationService.addNotification(notificationDto, userDto.subjectId, projectId)
        } catch (ex: AlreadyExistsException) {
            logger.warn("The Notification Already Exists.", ex)
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(SimpleScheduleNotificationHandler::class.java)
    }
}
