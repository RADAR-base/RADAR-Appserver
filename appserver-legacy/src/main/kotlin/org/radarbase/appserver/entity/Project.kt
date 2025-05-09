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

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.validation.constraints.NotNull
import org.radarbase.appserver.util.equalTo
import org.radarbase.appserver.util.stringRepresentation
import java.io.Serial
import java.io.Serializable
import java.util.*

/**
 * Represents an entity for the "projects" table in the database.
 *
 * @property id The primary key of the project, which is generated automatically.
 * @property projectId A unique identifier for the project. This field is mandatory.
 */
@Entity
@Table(name = "projects")
class Project(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,

    @field:NotNull
    @Column(name = "project_id", unique = true, nullable = false)
    var projectId: String? = null,
) : Serializable, AuditModel() {

    constructor(projectId: String) : this(null, projectId)

    companion object {
        @Serial
        private const val serialVersionUID = 12312466855464L
    }

    override fun equals(other: Any?): Boolean = equalTo(
        other,
        Project::projectId,
    )

    override fun hashCode(): Int {
        return Objects.hash(projectId)
    }

    override fun toString(): String = stringRepresentation(
        Project::id,
        Project::projectId,
        Project::createdAt,
        Project::updatedAt,
    )
}
