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

import java.time.Instant
import java.util.*
import javax.persistence.*

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
class DataMessage : Message() {

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "data_message_map")
    @MapKeyColumn(name = "key", nullable = true)
    @Column(name = "value")
    var dataMap: Map<String, String?>? = null

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

    @JvmOverloads
    fun copy(
        id: Long? = this.id,
        user: User? = this.user,
        scheduledTime: Instant? = this.scheduledTime,
        sourceId: String? = this.sourceId,
        sourceType: String? = this.sourceType,
        ttlSeconds: Int = this.ttlSeconds,
        fcmMessageId: String = this.fcmMessageId,
        fcmTopic: String? = this.fcmTopic,
        fcmCondition: String? = this.fcmCondition,
        appPackage: String? = this.appPackage,
        dryRun: Boolean = this.dryRun,
        delivered: Boolean = this.delivered,
        validated: Boolean = this.validated,
        priority: String? = this.priority,
        mutableContent: Boolean = this.mutableContent,
    ): DataMessage {
        return DataMessage().apply {
            this.id = id
            this.user = user
            this.scheduledTime = scheduledTime
            this.sourceId = sourceId
            this.sourceType = sourceType
            this.ttlSeconds = ttlSeconds
            this.fcmMessageId = fcmMessageId
            this.fcmTopic = fcmTopic
            this.fcmCondition = fcmCondition
            this.appPackage = appPackage
            this.dryRun = dryRun
            this.delivered = delivered
            this.validated = validated
            this.priority = priority
            this.mutableContent = mutableContent
        }
    }

    override fun toString(): String {
        return "DataMessage(id=$id, dataMap=$dataMap)"
    }


    companion object {
        private const val serialVersionUID = 4L
    }
}