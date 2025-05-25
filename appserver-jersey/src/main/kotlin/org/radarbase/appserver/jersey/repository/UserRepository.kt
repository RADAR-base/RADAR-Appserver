package org.radarbase.appserver.jersey.repository

import org.radarbase.appserver.jersey.entity.User

interface UserRepository : BaseRepository<User> {
    suspend fun findBySubjectId(subjectId: String): User?
    suspend fun findByProjectId(projectId: Long): List<User>
    suspend fun findBySubjectIdAndProjectId(subjectId: String, projectId: Long): User?
    suspend fun findByFcmToken(fcmToken: String): User?
    suspend fun existsBySubjectId(subjectId: String): Boolean
}
