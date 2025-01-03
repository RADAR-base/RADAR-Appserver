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

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.radarbase.appserver.util.equalTo
import org.springframework.lang.Nullable
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
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    var user: User? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = true, cascade = [CascadeType.ALL])
    @JoinColumn(name = "task_id", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    var task: Task? = null,

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
    var mutableContent: Boolean = false
) : AuditModel(), Serializable, Scheduled {

    override fun equals(other: Any?): Boolean = equalTo(
        other,
        Message::ttlSeconds,
        Message::delivered,
        Message::dryRun,
        Message::user,
        Message::scheduledTime,
        Message::sourceId,
        Message::appPackage,
        Message::sourceType,
    )

    override fun hashCode(): Int {
        return Objects.hash(
            user,
            sourceId,
            scheduledTime,
            ttlSeconds,
            delivered,
            dryRun,
            appPackage,
            sourceType
        )
    }

    companion object {
        @Serial
        private const val serialVersionUID = -367424816328519L
    }
}