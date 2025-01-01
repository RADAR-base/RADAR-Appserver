package org.radarbase.appserver.repository

import org.radarbase.appserver.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findBySubjectId(subjectId: String?): User?
    fun findByProjectId(projectId: Long?): List<User>
    fun findBySubjectIdAndProjectId(subjectId: String?, projectId: Long?): User?
    fun findByFcmToken(fcmToken: String?): User?

    override fun deleteById(id: Long)
}