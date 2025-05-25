package org.radarbase.appserver.jersey.repository

import org.radarbase.appserver.jersey.entity.Project

interface ProjectRepository {
    suspend fun find(id: Long): Project?
    suspend fun findByProjectId(projectId: String): Project?
    suspend fun exists(id: Long): Boolean
    suspend fun existsByProjectId(projectId: String): Boolean
    suspend fun add(project: Project): Project
    suspend fun update(project: Project): Project?
    suspend fun findAll(): List<Project>
}
