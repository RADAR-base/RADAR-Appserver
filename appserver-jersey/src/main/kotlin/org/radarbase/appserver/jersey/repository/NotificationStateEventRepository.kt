package org.radarbase.appserver.jersey.repository

import org.radarbase.appserver.jersey.entity.NotificationStateEvent

interface NotificationStateEventRepository : BaseRepository<NotificationStateEvent> {
    suspend fun findByNotificationId(notificationId: Long): List<NotificationStateEvent>
    suspend fun countByNotificationId(notificationId: Long): Long
}
