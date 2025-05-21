package org.radarbase.appserver.jersey.repository

import org.radarbase.appserver.jersey.entity.User

interface UserRepository {
    suspend fun get(id: Long): User?
    suspend fun getBySubjectId(subjectId: String): User?
    suspend fun exists(id: Long): Boolean
    suspend fun existsBySubjectId(subjectId: String): Boolean
    suspend fun add(user: User): User
    suspend fun update(user: User): User?
    suspend fun delete(id: Long)
    suspend fun all(): List<User>
}
