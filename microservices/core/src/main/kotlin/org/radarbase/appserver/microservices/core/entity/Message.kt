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

package org.radarbase.appserver.microservices.core.entity

import jakarta.annotation.Nullable
import jakarta.persistence.Column
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import jakarta.validation.constraints.NotNull
import org.radarbase.appserver.microservices.core.utils.equalTo
import java.io.Serial
import java.io.Serializable
import java.time.Instant
import java.util.Objects

@MappedSuperclass
class Message(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,

    @field:NotNull
    @Column(name = "user_id", nullable = false)
    var userId: Long? = null,

    @field:NotNull
    @Column(name = "subject_id", nullable = false)
    var subjectId: String? = null,

    @field:NotNull
    @Column(name = "project_id", nullable = false)
    var projectId: String? = null,

    @Column(name = "task_id", nullable = false)
    var taskId: Long? = null,

    @Column(name = "source_id")
    @field:Nullable
    var sourceId: String? = null,

    @field:NotNull
    @Column(name = "scheduled_time", nullable = false)
    override var scheduledTime: Instant? = null,

    @Column(name = "ttl_seconds")
    var ttlSeconds: Int = 0,

    @Column(name = "fcm_message_id", unique = true)
    var fcmMessageId: String? = null,

    // for use with the FCM admin SDK
    @Column(name = "fcm_topic")
    @field:Nullable
    var fcmTopic: String? = null,

    // for use with the FCM admin SDK
    @Column(name = "fcm_condition")
    @field:Nullable
    var fcmCondition: String? = null,

    // TODO: REMOVE DELIVERED AND VALIDATED. These can be handled by state lifecycle.
    var delivered: Boolean = false,

    var validated: Boolean = false,

    @Column(name = "app_package")
    @field:Nullable
    var appPackage: String? = null,

    @Column(name = "source_type")
    @field:Nullable
    var sourceType: String? = null,

    @Column(name = "dry_run")
    var dryRun: Boolean = false,

    var priority: String? = null,

    @Column(name = "mutable_content")
    var mutableContent: Boolean = false,
) : AuditModel(), Serializable, Scheduled {

    override fun equals(other: Any?): Boolean = equalTo(
        other,
        Message::ttlSeconds,
        Message::delivered,
        Message::dryRun,
        Message::userId,
        Message::subjectId,
        Message::projectId,
        Message::scheduledTime,
        Message::sourceId,
        Message::appPackage,
        Message::sourceType,
    )

    override fun hashCode(): Int {
        return Objects.hash(
            userId,
            subjectId,
            projectId,
            sourceId,
            scheduledTime,
            ttlSeconds,
            delivered,
            dryRun,
            appPackage,
            sourceType,
        )
    }

    override fun toString(): String {
        return "Message(id=$id, userId=$userId, subjectId=$subjectId, projectId=$projectId, taskId=$taskId, sourceId=$sourceId, scheduledTime=$scheduledTime, ttlSeconds=$ttlSeconds, fcmMessageId=$fcmMessageId, fcmTopic=$fcmTopic, fcmCondition=$fcmCondition, delivered=$delivered, validated=$validated, appPackage=$appPackage, sourceType=$sourceType, dryRun=$dryRun, priority=$priority, mutableContent=$mutableContent)"
    }

    companion object {
        @Serial
        private const val serialVersionUID = -367424816328519L
    }
}
