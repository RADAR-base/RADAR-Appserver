package org.radarbase.appserver.repository

import org.radarbase.appserver.entity.Project
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProjectRepository : JpaRepository<Project, Long> {
    fun findByProjectId(projectId: String?): Project?
    fun existsByProjectId(projectId: String?): Boolean
}