package org.radarbase.appserver.jersey.repository

import org.radarbase.appserver.jersey.entity.User

interface UserRepository {
    suspend fun find(id: Long): User?
    suspend fun findBySubjectId(subjectId: String): User?
    suspend fun findByProjectId(projectId: Long): List<User>
    suspend fun findBySubjectIdAndProjectId(subjectId: String, projectId: Long): User?
    suspend fun findByFcmToken(fcmToken: String): User?
    suspend fun exists(id: Long): Boolean
    suspend fun existsBySubjectId(subjectId: String): Boolean
    suspend fun add(user: User): User
    suspend fun update(user: User): User?
    suspend fun delete(user: User)
    suspend fun findAll(): List<User>
}
