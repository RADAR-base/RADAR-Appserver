package org.radarbase.appserver.jersey.repository.impl

import jakarta.inject.Provider
import jakarta.persistence.EntityManager
import jakarta.ws.rs.core.Context
import org.radarbase.appserver.jersey.entity.Project
import org.radarbase.appserver.jersey.repository.ProjectRepository
import org.radarbase.jersey.hibernate.HibernateRepository
import org.radarbase.jersey.service.AsyncCoroutineService

class ProjectRepositoryImpl(
    @Context em: Provider<EntityManager>,
    @Context asyncCoroutineService: AsyncCoroutineService,
) : ProjectRepository, HibernateRepository(em, asyncCoroutineService) {
    override suspend fun find(id: Long): Project? = transact {
        find(Project::class.java, id)
    }

    override suspend fun findByProjectId(projectId: String): Project? = transact {
        createQuery(
            "SELECT p FROM Project p WHERE p.projectId = :projectId",
            Project::class.java,
        ).setParameter("projectId", projectId).resultList.firstOrNull()
    }

    override suspend fun exists(id: Long): Boolean = transact {
        createQuery(
            """SELECT COUNT(p)
                FROM Project p 
                WHERE p.id = :id""".trimIndent(),
            Long::class.java,
        ).setParameter(
            "id", id,
        ).singleResult > 0
    }

    override suspend fun existsByProjectId(projectId: String): Boolean = transact {
        createQuery(
            """SELECT COUNT(p) 
                FROM Project p 
                WHERE p.projectId = :projectId""".trimIndent(),
            Long::class.java,
        ).setParameter(
            "projectId", projectId,
        ).singleResult > 0
    }

    override suspend fun add(entity: Project): Project = transact {
        entity.apply(::persist)
    }

    override suspend fun delete(entity: Project) = Unit // Not needed

    override suspend fun update(entity: Project): Project? = transact {
        merge(entity)
    }

    override suspend fun findAll(): List<Project> = transact {
        createQuery("SELECT p FROM Project p", Project::class.java).resultList
    }
}
