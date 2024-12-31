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
    val id: Long? = null,

    @field:NotNull
    @Column(name = "project_id", unique = true, nullable = false)
    val projectId: String
) : Serializable, AuditModel() {

    init {
        require(projectId.isNotBlank()) { "Project ID must not be blank." }
    }

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

    /**
     * Creates a copy of the current Project object with optional property overrides.
     *
     * This method ensures that auditing fields (`createdAt`, `updatedAt`) are preserved.
     *
     * @param id Optional id to override the current project's id. Defaults to the current id.
     * @param projectId Optional project identifier. Defaults to the current projectId.
     * @return A new Project object with the specified or inherited properties.
     */
    fun copy(
        id: Long? = this.id,
        projectId: String = this.projectId,
    ): Project = Project(id, projectId).apply {
        this.createdAt = this@Project.createdAt
        this.updatedAt = this@Project.updatedAt
    }
}