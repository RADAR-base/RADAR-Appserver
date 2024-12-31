package org.radarbase.appserver.mapper

import org.radarbase.appserver.dto.ProjectDTO
import org.radarbase.appserver.entity.Project
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/**
 * Mapper implementation for converting between ProjectDTO and Project entity objects.
 *
 * This class provides methods to map [ProjectDTO] objects to [Project] entities and vice versa.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class ProjectMapper : Mapper<ProjectDTO, Project> {

    /**
     * Converts a ProjectDTO object to a Project entity.
     */
    override fun dtoToEntity(projectDTO: ProjectDTO): Project {
        return Project(id = projectDTO.id, projectId = projectDTO.projectId)
    }

    /**
     * Converts a Project entity to a ProjectDTO object.
     */
    override fun entityToDto(project: Project): ProjectDTO = ProjectDTO(
        id = project.id,
        projectId = project.projectId,
        createdAt = project.createdAt?.toInstant(),
        updatedAt = project.updatedAt?.toInstant()
    )
}