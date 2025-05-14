package org.radarbase.appserver.repository.impl

import jakarta.inject.Provider
import jakarta.persistence.EntityManager
import jakarta.ws.rs.core.Context
import org.radarbase.appserver.entity.Project
import org.radarbase.appserver.repository.ProjectRepository
import org.radarbase.jersey.hibernate.HibernateRepository
import org.radarbase.jersey.service.AsyncCoroutineService

class ProjectRepositoryImpl(
    @Context private val em: Provider<EntityManager>,
    @Context asyncCoroutineService: AsyncCoroutineService,
) : ProjectRepository, HibernateRepository(em, asyncCoroutineService) {
    override suspend fun get(id: Long): Project? = transact {
        find(Project::class.java, id)
    }

    override suspend fun getByProjectId(projectId: String): Project? = transact {
        createQuery(
            "SELECT p FROM Project p WHERE p.projectId = :projectId",
            Project::class.java,
        ).setParameter("projectId", projectId).resultList.firstOrNull()
    }

    override suspend fun exists(id: Long): Boolean = get(id) != null

    override suspend fun existsByProjectId(projectId: String): Boolean = getByProjectId(projectId) != null

    override suspend fun add(project: Project): Project = transact {
        project.apply(::persist)
    }

    override suspend fun update(project: Project) = transact {
        createQuery("SELECT p FROM Project p WHERE p.id = :id", Project::class.java).apply {
                setParameter(
                    "id",
                    project.id,
                )
            }.resultList.firstOrNull()?.apply {
                projectId = project.projectId
                merge(this)
            }
    }

    override suspend fun delete(id: Long): Unit = transact {
        createQuery("SELECT p FROM Project p WHERE p.id = :id", Project::class.java).apply {
                setParameter(
                    "id",
                    id,
                )
            }.resultList.firstOrNull()?.apply { remove(merge(this)) }
    }

    override suspend fun all(): List<Project> = transact {
        createQuery("SELECT p FROM Project p", Project::class.java).resultList
    }
}
