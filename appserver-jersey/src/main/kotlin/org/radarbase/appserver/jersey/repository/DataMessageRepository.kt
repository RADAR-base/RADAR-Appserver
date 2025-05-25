package org.radarbase.appserver.jersey.repository

import org.radarbase.appserver.jersey.entity.DataMessage
import java.time.Instant

interface DataMessageRepository : BaseRepository<DataMessage> {
    suspend fun findByUserId(userId: Long): List<DataMessage>
    suspend fun findByIdAndUserId(id: Long, userId: Long): DataMessage?
    suspend fun findByFcmMessageId(fcmMessageId: String): DataMessage?
    suspend fun existsByUserIdAndSourceIdAndScheduledTimeAndTtlSeconds(
        userId: Long,
        sourceId: String,
        scheduledTime: Instant,
        ttlSeconds: Int,
    ): Boolean
    suspend fun existsByIdAndUserId(id: Long, userId: Long): Boolean
    suspend fun deleteByUserId(userId: Long)
    suspend fun deleteByIdAndUserId(id: Long, userId: Long)
    suspend fun deleteByFcmMessageId(fcmMessageId: String)
}
