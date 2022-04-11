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
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.springframework.lang.Nullable
import java.io.Serializable
import java.time.Instant
import java.util.*
import javax.persistence.*

@MappedSuperclass
abstract class Message : AuditModel(), Serializable, Scheduled {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    var user: User? = null

    @Nullable
    @Column(name = "source_id")
    var sourceId: String? = null

    @Column(name = "scheduled_time", nullable = false)
    override var scheduledTime: Instant? = null

    @Column(name = "ttl_seconds")
    var ttlSeconds: Int = 0

    @Column(name = "fcm_message_id", unique = true)
    var fcmMessageId: String? = null

    // for use with the FCM admin SDK
    @Column(name = "fcm_topic")
    @Nullable
    var fcmTopic: String? = null

    // for use with the FCM admin SDK
    @Column(name = "fcm_condition")
    @Nullable
    var fcmCondition: String? = null

    // TODO: REMOVE DELIVERED AND VALIDATED. These can be handled by state lifecycle.
    var delivered: Boolean = false
    var validated: Boolean = false

    @Nullable
    @Column(name = "app_package")
    var appPackage: String? = null

    // Source Type from the Management Portal
    @Nullable
    @Column(name = "source_type")
    var sourceType: String? = null

    @Column(name = "dry_run") // for use with the FCM admin SDK
    var dryRun: Boolean = false

    var priority: String? = null

    @Column(name = "mutable_content")
    var mutableContent: Boolean = false

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is Message) {
            return false
        }
        return (ttlSeconds == other.ttlSeconds && delivered == other.delivered
                && dryRun == other.dryRun && user == other.user
                && sourceId == other.sourceId
                && scheduledTime == other.scheduledTime
                && appPackage == other.appPackage)
    }

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
        private const val serialVersionUID = -367424816328519L
    }
}