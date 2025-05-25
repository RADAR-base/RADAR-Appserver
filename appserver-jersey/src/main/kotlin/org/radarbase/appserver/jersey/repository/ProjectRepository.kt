package org.radarbase.appserver.jersey.repository

import org.radarbase.appserver.jersey.entity.Project

interface ProjectRepository: BaseRepository<Project> {
    suspend fun findByProjectId(projectId: String): Project?
    suspend fun existsByProjectId(projectId: String): Boolean
}
