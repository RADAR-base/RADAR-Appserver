package org.radarbase.appserver.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.persistence.*
import org.radarbase.appserver.util.equalTo
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.util.*

/**
 * Abstract base class that provides auditing fields for entity classes.
 *
 * This class includes created and updated timestamp fields that are automatically
 * managed during the lifecycle of an entity. It can be extended by other
 * entity classes to inherit these common auditing functionalities.
 *
 * @property createdAt The timestamp indicating when the entity was created. This value
 *   is set automatically and cannot be modified after the entity is persisted.
 * @property updatedAt The timestamp indicating the last time the entity was updated.
 *   This value is automatically updated whenever the entity undergoes modifications.
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
@JsonIgnoreProperties(value = ["createdAt", "updatedAt"], allowGetters = true)
abstract class AuditModel {

    /**
     * The timestamp indicating when the entity was created.
     *
     * This property is automatically populated and managed by the auditing framework.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreatedDate
    var createdAt: Date? = null

    /**
     * Timestamp indicating the last modification time of an entity.
     *
     * This field is automatically populated and updated whenever an entity
     * is modified.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    var updatedAt: Date? = null

    override fun equals(other: Any?): Boolean = equalTo(
        other,
        AuditModel::createdAt,
        AuditModel::updatedAt
    )

    override fun hashCode(): Int {
        return Objects.hash(createdAt, updatedAt)
    }
}