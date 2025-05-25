package org.radarbase.appserver.jersey.repository

import org.radarbase.appserver.jersey.entity.Notification
import java.time.Instant

interface NotificationRepository : BaseRepository<Notification> {
    suspend fun findByUserId(userId: Long): List<Notification>
    suspend fun findByIdAndUserId(id: Long, userId: Long): Notification?
    suspend fun findByUserIdAndTaskId(userId: Long, taskId: Long): List<Notification>
    suspend fun findByTaskId(id: Long): List<Notification>
    suspend fun findByFcmMessageId(fcmMessageId: String): Notification?
    suspend fun findByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
        userId: Long,
        sourceId: String,
        scheduledTime: Instant,
        title: String,
        body: String,
        type: String,
        ttlSeconds: Int,
    ): Notification?

    suspend fun existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
        userId: Long,
        sourceId: String,
        scheduledTime: Instant,
        title: String,
        body: String,
        type: String,
        ttlSeconds: Int,
    ): Boolean

    suspend fun existsByIdAndUserId(id: Long, userId: Long): Boolean
    suspend fun existsByTaskId(taskId: Long): Boolean
    suspend fun deleteByUserId(userId: Long)
    suspend fun deleteByTaskId(taskId: Long)
    suspend fun deleteByUserIdAndTaskId(userId: Long, taskId: Long)
    suspend fun deleteByFcmMessageId(fcmMessageId: String)
    suspend fun deleteByIdAndUserId(id: Long, userId: Long)
}
