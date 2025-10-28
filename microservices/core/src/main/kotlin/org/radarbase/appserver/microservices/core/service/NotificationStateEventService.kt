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
