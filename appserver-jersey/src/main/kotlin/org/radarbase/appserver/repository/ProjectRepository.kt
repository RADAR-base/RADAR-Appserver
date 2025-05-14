package org.radarbase.appserver.repository

import org.radarbase.appserver.entity.Project

interface ProjectRepository {
    suspend fun get(id: Long): Project?
    suspend fun getByProjectId(projectId: String): Project?
    suspend fun exists(id: Long): Boolean
    suspend fun existsByProjectId(projectId: String): Boolean
    suspend fun add(project: Project): Project
    suspend fun update(project: Project): Project?
    suspend fun delete(id: Long)
    suspend fun all(): List<Project>
}
