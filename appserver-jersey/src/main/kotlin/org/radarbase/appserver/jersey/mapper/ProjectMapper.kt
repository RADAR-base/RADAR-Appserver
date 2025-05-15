package org.radarbase.appserver.jersey.mapper

import org.radarbase.appserver.jersey.dto.ProjectDto
import org.radarbase.appserver.jersey.entity.Project

class ProjectMapper : Mapper<ProjectDto, Project> {

    /**
     * Converts a ProjectDTO object to a Project entity.
     */
    override suspend fun dtoToEntity(dto: ProjectDto): Project {
        return Project(id = dto.id, projectId = dto.projectId)
    }

    /**
     * Converts a Project entity to a ProjectDTO object.
     */
    override suspend fun entityToDto(entity: Project): ProjectDto = ProjectDto(
        id = entity.id,
        projectId = entity.projectId,
        createdAt = entity.createdAt,
        updatedAt = entity.updatedAt
    )
}
