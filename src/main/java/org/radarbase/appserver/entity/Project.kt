package org.radarbase.appserver.entity

import jakarta.persistence.*
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
        Project::updatedAt
    )
}