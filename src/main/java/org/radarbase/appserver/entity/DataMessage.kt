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
package org.radarbase.appserver.entity

import org.springframework.lang.Nullable
import java.time.Instant
import java.util.*
import javax.persistence.*
import javax.validation.constraints.NotNull

/**
 * [Entity] for persisting data messages. The corresponding DTO is [FcmDataMessageDto].
 * This also includes information for scheduling the data message through the Firebase Cloud
 * Messaging(FCM) system.
 *
 * @author yatharthranjan
 * @see Scheduled
 *
 * @see org.radarbase.appserver.service.scheduler.DataMessageSchedulerService
 */
@Entity
@Table(
    name = "data_messages",
    uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "source_id", "scheduled_time", "ttl_seconds", "delivered", "dry_run"])]
)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
class DataMessage(
    id: Long? = null,
    user: @NotNull User? = null,
    sourceId: String? = null,
    scheduledTime: @NotNull Instant,
    ttlSeconds: Int = 0,
    fcmMessageId: String? = null,
    fcmTopic: String? = null,
    fcmCondition: String? = null,
    delivered: Boolean = false,
    validated: Boolean = false,
    appPackage: String? = null,
    sourceType: String? = null,
    dryRun: Boolean = false,
    priority: String? = null,
    mutableContent: Boolean = false,

    @Nullable
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "data_message_map")
    @MapKeyColumn(name = "key", nullable = true)
    @Column(name = "value")
    val dataMap: Map<String, String>? = null,
) : Message(
    id,
    user,
    sourceId,
    scheduledTime,
    ttlSeconds,
    fcmMessageId,
    fcmTopic,
    fcmCondition,
    delivered,
    validated,
    appPackage,
    sourceType,
    dryRun,
    priority,
    mutableContent
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        return if (other !is DataMessage) {
            false
        } else super.equals(other)
    }

    override fun hashCode(): Int {
        return Objects.hash(
            super.hashCode(),
            dataMap
        )
    }

    companion object {
        private const val serialVersionUID = 4L
    }
}