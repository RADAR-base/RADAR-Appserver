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
