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

    override suspend fun exists(id: Long): Boolean = find(id) != null

    override suspend fun existsByProjectId(projectId: String): Boolean = findByProjectId(projectId) != null

    override suspend fun add(project: Project): Project = transact {
        project.apply(::persist)
    }

    override suspend fun update(project: Project): Project? = transact {
        merge(project)
    }
    
    override suspend fun findAll(): List<Project> = transact {
        createQuery("SELECT p FROM Project p", Project::class.java).resultList
    }
}
