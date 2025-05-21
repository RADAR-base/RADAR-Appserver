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

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.MapKeyColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.radarbase.appserver.jersey.utils.equalTo
import org.radarbase.appserver.jersey.utils.stringRepresentation
import java.time.Instant
import java.util.Objects

/**
 * [Entity] for persisting users. The corresponding DTO is [org.radarbase.appserver.dto.fcm.FcmUserDto].
 * [Project] can have multiple [User] (Many-to-One).
 */
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["subject_id", "project_id"]),
        UniqueConstraint(columnNames = ["fcm_token"]),
    ],
)
@Entity
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,

    @field:NotNull
    @Column(name = "subject_id", nullable = false)
    var subjectId: String? = null,

    @Column(name = "email")
    var emailAddress: String? = null,

    @field:NotNull
    @Column(name = "fcm_token", nullable = false, unique = true)
    var fcmToken: String? = null,

    @field:NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    var project: Project? = null,

    @field:NotNull
    @Column(name = "enrolment_date")
    var enrolmentDate: Instant? = null,

    @OneToOne(cascade = [CascadeType.ALL])
    var usermetrics: UserMetrics? = null,

    /**
     * Timezone of the user based on tz database names
     */
    @field:NotNull
    @Column(name = "timezone")
    var timezone: String? = null,

    @field:NotEmpty
    @Column(name = "language")
    var language: String? = null,

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "attributes_map")
    @MapKeyColumn(name = "key", nullable = true)
    @Column(name = "value")
    var attributes: Map<String?, String?>? = HashMap(),
) : AuditModel() {

    override fun toString(): String = stringRepresentation(
        User::id,
        User::subjectId,
        User::emailAddress,
        User::fcmToken,
        User::project,
        User::enrolmentDate,
        User::usermetrics,
        User::timezone,
        User::language,
    )


    override fun equals(other: Any?): Boolean = equalTo(
        other,
        User::subjectId,
        User::fcmToken,
        User::project,
        User::enrolmentDate,
    )

    override fun hashCode(): Int {
        return Objects.hash(subjectId, fcmToken, project, enrolmentDate)
    }
}
