package org.radarbase.appserver.jersey.repository

import jakarta.transaction.Transactional
import org.radarbase.appserver.jersey.dto.protocol.AssessmentType
import org.radarbase.appserver.jersey.entity.Task
import org.radarbase.appserver.jersey.search.QuerySpecification
import java.sql.Timestamp

interface TaskRepository : BaseRepository<Task> {
    suspend fun findByUserId(userId: Long): List<Task>

    suspend fun findByUserIdAndType(userId: Long, type: AssessmentType): List<Task>

    suspend fun deleteByUserId(userId: Long)

    suspend fun deleteByUserIdAndType(userId: Long, type: AssessmentType)

    suspend fun existsByIdAndUserId(id: Long, userId: Long): Boolean

    suspend fun existsByUserIdAndNameAndTimestamp(
        userId: Long,
        name: String,
        timestamp: Timestamp,
    ): Boolean

    suspend fun findByIdAndUserId(id: Long, userId: Long): Task?

    suspend fun findAll(specification: QuerySpecification<Task>): List<Task>

    suspend fun deleteAll(tasks: List<Task>)
}
