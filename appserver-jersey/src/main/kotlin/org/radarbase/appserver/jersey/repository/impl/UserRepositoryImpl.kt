package org.radarbase.appserver.jersey.repository.impl

import jakarta.inject.Provider
import jakarta.persistence.EntityManager
import jakarta.ws.rs.core.Context
import org.radarbase.appserver.jersey.entity.Project
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.repository.UserRepository
import org.radarbase.jersey.hibernate.HibernateRepository
import org.radarbase.jersey.service.AsyncCoroutineService

class UserRepositoryImpl(
    @Context em: Provider<EntityManager>,
    @Context asyncCoroutineService: AsyncCoroutineService,
) : HibernateRepository(em, asyncCoroutineService), UserRepository {
    override suspend fun get(id: Long): User? = transact {
        find(User::class.java, id)
    }

    override suspend fun getBySubjectId(subjectId: String): User? = transact {
        createQuery(
            "SELECT u FROM User u WHERE u.subjectId = :subjectId",
            User::class.java,
        ).setParameter("subjectId", subjectId).resultList.firstOrNull()
    }

    override suspend fun exists(id: Long): Boolean = get(id) != null

    override suspend fun existsBySubjectId(subjectId: String): Boolean = getBySubjectId(subjectId) != null

    override suspend fun getByProjectId(projectId: Long): User? = transact {
        val project = createQuery(
            "SELECT p FROM Project p WHERE p.id = :projectId",
            Project::class.java,
        ).setParameter("projectId", projectId)
            .resultList
            .firstOrNull()
            ?: return@transact null

        createQuery(
            "SELECT u FROM User u WHERE u.project = :project",
            User::class.java,
        ).setParameter("project", project)
            .resultList
            .firstOrNull()
    }

    override suspend fun getBySubjectIdAndProjectId(
        subjectId: String,
        projectId: Long,
    ): User? = transact {
        val project = createQuery(
            "SELECT p FROM Project p WHERE p.id = :projectId",
            Project::class.java,
        ).setParameter("projectId", projectId)
            .resultList
            .firstOrNull()
            ?: return@transact null

        createQuery(
            """
            SELECT u 
              FROM User u 
             WHERE u.subjectId = :subjectId
               AND u.project   = :project
            """.trimIndent(),
            User::class.java,
        )
            .setParameter("subjectId", subjectId)
            .setParameter("project", project)
            .resultList
            .firstOrNull()
    }

    override suspend fun getByFcmToken(fcmToken: String): User? = transact {
        createQuery(
            "SELECT u FROM User u WHERE u.fcmToken = :fcmToken",
            User::class.java,
        )
            .setParameter("fcmToken", fcmToken)
            .resultList
            .firstOrNull()
    }

    override suspend fun add(user: User): User = transact {
        user.apply(::persist)
    }

    override suspend fun update(user: User): User? = transact {
        find(User::class.java, user.id)?.apply {
            subjectId = user.subjectId
            emailAddress = user.emailAddress
            fcmToken = user.fcmToken
            project = user.project
            enrolmentDate = user.enrolmentDate
            usermetrics = user.usermetrics
            timezone = user.timezone
            language = user.language
            attributes = user.attributes
        }.also(::merge)
    }

    override suspend fun delete(id: Long): Unit = transact {
        find(User::class.java, id)?.apply {
            remove(merge(this))
        }
    }

    override suspend fun all(): List<User> = transact {
        createQuery("SELECT u FROM User u", User::class.java).resultList
    }
}
