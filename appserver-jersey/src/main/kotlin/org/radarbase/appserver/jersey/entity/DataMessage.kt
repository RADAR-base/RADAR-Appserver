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

package org.radarbase.appserver.jersey.entity

import jakarta.annotation.Nullable
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Inheritance
import jakarta.persistence.InheritanceType
import jakarta.persistence.MapKeyColumn
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.io.Serial
import java.time.Instant

/**
 * [Entity] for persisting data messages. The corresponding DTO is [FcmDataMessageDto].
 * This also includes information for scheduling the data message through the Firebase Cloud
 * Messaging(FCM) system.
 *
 * @see Scheduled
 * @see org.radarbase.appserver.service.scheduler.DataMessageSchedulerService
 */
@Suppress("unused")
@Entity
@Table(
    name = "data_messages",
    uniqueConstraints = [
        UniqueConstraint(
            columnNames = [
                "user_id", "source_id", "scheduled_time", "ttl_seconds", "delivered", "dry_run",
            ],
        ),
    ],
)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
class DataMessage : Message() {
    @Nullable
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "data_message_map")
    @MapKeyColumn(name = "data_message_key", nullable = true)
    @Column(name = "data_message_value")
    var dataMap: MutableMap<String?, String?>? = null

    class DataMessageBuilder(dataMessage: DataMessage? = null) {
        var id: Long? = dataMessage?.id
        var user: User? = dataMessage?.user
        var sourceId: String? = dataMessage?.sourceId
        var scheduledTime: Instant? = dataMessage?.scheduledTime
        var ttlSeconds: Int = dataMessage?.ttlSeconds ?: 0
        var fcmMessageId: String? = dataMessage?.fcmMessageId
        var fcmTopic: String? = dataMessage?.fcmTopic
        var fcmCondition: String? = dataMessage?.fcmCondition
        var delivered: Boolean = dataMessage?.delivered == true
        var validated: Boolean = dataMessage?.validated == true
        var appPackage: String? = dataMessage?.appPackage
        var sourceType: String? = dataMessage?.sourceType
        var dryRun: Boolean = dataMessage?.dryRun == true
        var priority: String? = dataMessage?.priority
        var mutableContent: Boolean = dataMessage?.mutableContent == true
        var dataMap: MutableMap<String?, String?>? = dataMessage?.dataMap

        fun id(id: Long?): DataMessageBuilder = apply {
            this.id = id
        }

        fun user(user: User?): DataMessageBuilder = apply {
            this.user = user
        }

        fun sourceId(sourceId: String?): DataMessageBuilder = apply {
            this.sourceId = sourceId
        }

        fun scheduledTime(scheduledTime: Instant?): DataMessageBuilder = apply {
            this.scheduledTime = scheduledTime
        }

        fun ttlSeconds(ttlSeconds: Int): DataMessageBuilder = apply {
            this.ttlSeconds = ttlSeconds
        }

        fun fcmMessageId(fcmMessageId: String?): DataMessageBuilder = apply {
            this.fcmMessageId = fcmMessageId
        }

        fun fcmTopic(fcmTopic: String?): DataMessageBuilder = apply {
            this.fcmTopic = fcmTopic
        }

        fun fcmCondition(fcmCondition: String?): DataMessageBuilder = apply {
            this.fcmCondition = fcmCondition
        }

        fun delivered(delivered: Boolean): DataMessageBuilder = apply {
            this.delivered = delivered
        }

        fun appPackage(appPackage: String?): DataMessageBuilder = apply {
            this.appPackage = appPackage
        }

        fun sourceType(sourceType: String?): DataMessageBuilder = apply {
            this.sourceType = sourceType
        }

        fun dryRun(dryRun: Boolean): DataMessageBuilder = apply {
            this.dryRun = dryRun
        }

        fun priority(priority: String?): DataMessageBuilder = apply {
            this.priority = priority
        }

        fun mutableContent(mutableContent: Boolean): DataMessageBuilder = apply {
            this.mutableContent = mutableContent
        }

        fun dataMap(dataMap: MutableMap<String?, String?>?): DataMessageBuilder = apply {
            this.dataMap = dataMap
        }

        fun build(): DataMessage {
            val dataMessage = DataMessage()
            dataMessage.id = this.id
            dataMessage.user = this.user
            dataMessage.sourceId = this.sourceId
            dataMessage.scheduledTime = this.scheduledTime
            dataMessage.ttlSeconds = this.ttlSeconds
            dataMessage.fcmMessageId = this.fcmMessageId
            dataMessage.fcmTopic = this.fcmTopic
            dataMessage.fcmCondition = this.fcmCondition
            dataMessage.delivered = this.delivered
            dataMessage.validated = this.validated
            dataMessage.appPackage = this.appPackage
            dataMessage.sourceType = this.sourceType
            dataMessage.dryRun = this.dryRun
            dataMessage.priority = this.priority
            dataMessage.mutableContent = this.mutableContent
            dataMessage.dataMap = this.dataMap

            return dataMessage
        }
    }

    override fun toString(): String {
        return "DataMessage(dataMap=$dataMap)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as DataMessage

        return dataMap == other.dataMap
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (dataMap?.hashCode() ?: 0)
        return result
    }

    companion object {
        @Serial
        private const val serialVersionUID = 4L
    }
}
