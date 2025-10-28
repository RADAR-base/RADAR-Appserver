/*
 * Copyright 2025 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.appserver.microservices.core.service

import org.radarbase.appserver.microservices.core.dto.NotificationStateEventDto
import org.radarbase.appserver.microservices.core.entity.NotificationStateEvent

interface NotificationStateEventService {
    suspend fun addNotificationStateEvent(notificationStateEvent: NotificationStateEvent)

    suspend fun getNotificationStateEvents(
        projectId: String,
        subjectId: String,
        notificationId: Long,
    ): List<NotificationStateEventDto>

    suspend fun getNotificationStateEventsByNotificationId(
        notificationId: Long,
    ): List<NotificationStateEventDto>

    suspend fun publishNotificationStateEventExternal(
        projectId: String,
        subjectId: String,
        notificationId: Long,
        notificationStateEventDto: NotificationStateEventDto,
    )
}
