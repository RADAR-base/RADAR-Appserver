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
package org.radarbase.appserver.repository

import org.radarbase.appserver.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

/** @author yatharthranjan
 */
@Suppress("unused")
@Repository
interface NotificationRepository : JpaRepository<Notification, Long> {
    fun findByUserId(userId: Long?): List<Notification>

    fun findByUserIdAndTaskId(userId: Long?, taskId: Long?): List<Notification>

    fun findByTaskId(id: Long?): List<Notification>

    fun deleteByUserId(userId: Long?)

    fun deleteByTaskId(taskId: Long?)

    fun deleteByUserIdAndTaskId(userId: Long?, taskId: Long?)

    fun existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
        userId: Long?,
        sourceId: String?,
        scheduledTime: Instant?,
        title: String?,
        body: String?,
        type: String?,
        ttlSeconds: Int,
    ): Boolean

    fun findByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
        userId: Long?,
        sourceId: String?,
        scheduledTime: Instant?,
        title: String?,
        body: String?,
        type: String?,
        ttlSeconds: Int,
    ): Notification?

    fun existsByIdAndUserId(id: Long?, userId: Long?): Boolean

    fun existsByTaskId(taskId: Long?): Boolean

    fun deleteByFcmMessageId(fcmMessageId: String?)

    fun deleteByIdAndUserId(id: Long?, userId: Long?)

    fun findByFcmMessageId(fcmMessageId: String?): Notification?

    fun findByIdAndUserId(id: Long, userId: Long): Notification?
}
