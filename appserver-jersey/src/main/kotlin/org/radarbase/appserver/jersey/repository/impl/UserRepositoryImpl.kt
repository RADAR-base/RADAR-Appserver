package org.radarbase.appserver.jersey.repository.impl

import jakarta.inject.Provider
import jakarta.persistence.EntityManager
import jakarta.ws.rs.core.Context
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.repository.UserRepository
import org.radarbase.jersey.hibernate.HibernateRepository
import org.radarbase.jersey.service.AsyncCoroutineService

class UserRepositoryImpl(
    @Context em: Provider<EntityManager>,
    @Context asyncCoroutineService: AsyncCoroutineService,
) : HibernateRepository(em, asyncCoroutineService), UserRepository {
    override suspend fun find(id: Long): User? = transact {
        find(User::class.java, id)
    }

    override suspend fun findBySubjectId(subjectId: String): User? = transact {
        createQuery(
            "SELECT u FROM User u WHERE u.subjectId = :subjectId",
            User::class.java,
        ).setParameter("subjectId", subjectId).resultList.firstOrNull()
    }

    override suspend fun exists(id: Long): Boolean = find(id) != null

    override suspend fun existsBySubjectId(subjectId: String): Boolean = findBySubjectId(subjectId) != null

    override suspend fun findByProjectId(projectId: Long): List<User> = transact {
        createQuery(
            "SELECT u FROM User u WHERE u.project.id = :projectId",
            User::class.java,
        )
            .setParameter("projectId", projectId)
            .resultList
    }

    override suspend fun findBySubjectIdAndProjectId(
        subjectId: String,
        projectId: Long,
    ): User? = transact {
        createQuery(
            """SELECT u 
                        FROM User u 
                        WHERE u.subjectId = :subjectId 
                        AND u.project.id = :projectId
                        """.trimIndent(),
            User::class.java,
        )
            .setParameter("subjectId", subjectId)
            .setParameter("projectId", projectId)
            .resultList
            .firstOrNull()

    }

    override suspend fun findByFcmToken(fcmToken: String): User? = transact {
        createQuery(
            "SELECT u FROM User u WHERE u.fcmToken = :fcmToken",
            User::class.java,
        )
            .setParameter("fcmToken", fcmToken)
            .resultList
            .firstOrNull()
    }

    override suspend fun add(entity: User): User = transact {
        entity.apply(::persist)
    }

    override suspend fun update(entity: User): User? = transact {
        merge(entity)
    }

    override suspend fun delete(entity: User): Unit = transact {
            remove(merge(entity))
    }

    override suspend fun findAll(): List<User> = transact {
        createQuery("SELECT u FROM User u", User::class.java).resultList
    }
}
