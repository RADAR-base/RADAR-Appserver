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
