/*
 * Copyright 2025 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.appserver.jersey.repository.impl

import jakarta.inject.Provider
import jakarta.persistence.EntityManager
import jakarta.ws.rs.core.Context
import org.radarbase.appserver.jersey.dto.ProjectDto
import org.radarbase.appserver.jersey.entity.Project
import org.radarbase.appserver.jersey.exception.InvalidProjectDetailsException
import org.radarbase.appserver.jersey.repository.ProjectRepository
import org.radarbase.jersey.exception.HttpNotFoundException
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

    override suspend fun update(entity: Project): Project = transact {
        merge(entity)
    }

    /**
     * Efficient way to update the project by retrieving and updating the retrieved persistent project entity Instead of doing it in two transactions.
     */
    override suspend fun updateEfficiently(dto: ProjectDto): Project = transact {
        val projectId = try {
            requireNotNull(dto.id)
        } catch (_: IllegalArgumentException) {
            throw InvalidProjectDetailsException("The 'id' of the project must be supplied for updating project")
        }

        val project = find(Project::class.java, projectId) ?: throw HttpNotFoundException(
            "project_not_found",
            "Project with id $projectId does not exists. Please create project first",
        )

        val projectExists = createQuery(
            """SELECT COUNT(p) 
                FROM Project p 
                WHERE p.projectId = :projectId""".trimIndent(),
            Long::class.java,
        ).setParameter(
            "projectId", dto.projectId,
        ).singleResult > 0

        if (projectExists) {
            throw InvalidProjectDetailsException("Project with id $projectId already exists.")
        }

        project.apply {
            this.projectId = dto.projectId
        }
    }

    override suspend fun findAll(): List<Project> = transact {
        createQuery("SELECT p FROM Project p", Project::class.java).resultList
    }
}
