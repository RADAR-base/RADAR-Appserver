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
package org.radarbase.appserver.dto.fcm

import jakarta.validation.constraints.Size
import org.radarbase.appserver.util.equalTo
import org.radarbase.appserver.util.stringRepresentation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Objects

/** @author yatharthranjan
 */
class FcmNotifications {

    @field:Size(max = 200)
    private var _notifications: MutableList<FcmNotificationDto> = mutableListOf<FcmNotificationDto>()

    val notifications: List<FcmNotificationDto>
        get() = _notifications

    fun withNotifications(notifications: List<FcmNotificationDto>): FcmNotifications = apply {
        this._notifications = notifications.toMutableList()
    }

    fun addNotification(notificationDto: FcmNotificationDto): FcmNotifications = apply {
        if (!_notifications.contains(notificationDto)) {
            this._notifications.add(notificationDto)
        } else {
            logger.info("Notification {} already exists in the Fcm Notifications.", notificationDto)
        }
    }

    override fun equals(other: Any?): Boolean = equalTo(
        other,
        FcmNotifications::_notifications,
    )

    override fun toString(): String = stringRepresentation(
        FcmNotifications::_notifications,
    )

    override fun hashCode(): Int {
        return Objects.hash(_notifications)
    }


    companion object {
        private val logger: Logger = LoggerFactory.getLogger(FcmNotifications::class.java)
    }
}
