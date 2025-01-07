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

import jakarta.validation.constraints.NotNull
import org.radarbase.appserver.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.*

/** @author yatharthranjan
 */
@Repository
interface NotificationRepository : JpaRepository<Notification?, Long?> {
    fun findByUserId(userId: Long?): MutableList<Notification?>?

    fun findByUserIdAndTaskId(userId: Long?, taskId: Long?): MutableList<Notification?>?

    fun findByTaskId(id: Long?): MutableList<Notification?>?

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
        ttlSeconds: Int
    ): Boolean

    fun findByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
        userId: Long?,
        sourceId: String?,
        scheduledTime: Instant?,
        title: String?,
        body: String?,
        type: String?,
        ttlSeconds: Int
    ): Optional<Notification?>?

    fun existsByIdAndUserId(id: Long?, userId: Long?): Boolean

    override fun existsById(id: @NotNull Long?): Boolean

    fun existsByTaskId(taskId: Long?): Boolean

    fun deleteByFcmMessageId(fcmMessageId: String?)

    fun deleteByIdAndUserId(id: Long?, userId: Long?)

    fun findByFcmMessageId(fcmMessageId: String?): Optional<Notification?>?

    fun findByIdAndUserId(id: Long, userId: Long): Optional<Notification?>?
}
