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

package org.radarbase.appserver.jersey.entity

import jakarta.annotation.Nullable
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.radarbase.appserver.jersey.utils.stringRepresentation
import java.time.Instant

@Entity
@Table(name = "user_metrics")
class UserMetrics(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,

    /**
     * The most recent time when the app was opened
     */
    @Nullable
    @Column(name = "last_opened")
    var lastOpened: Instant? = null,

    /**
     * The most recent time when a notification for the app was delivered.
     */
    @Nullable
    @Column(name = "last_delivered")
    var lastDelivered: Instant? = null,

    @NotNull
    @OneToOne(mappedBy = "usermetrics")
    var user: User? = null,
) : AuditModel() {

    constructor(lastOpened: Instant?, lastDelivered: Instant?) : this(
        null,
        lastOpened = lastOpened,
        lastDelivered = lastDelivered,
    )

    override fun toString(): String = stringRepresentation(
        UserMetrics::id,
        UserMetrics::lastOpened,
        UserMetrics::lastDelivered,
    )
}
