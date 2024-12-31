package org.radarbase.appserver.service

import org.radarbase.appserver.converter.ProjectConverter
import org.radarbase.appserver.dto.ProjectDTO
import org.radarbase.appserver.dto.ProjectDTOs
import org.radarbase.appserver.entity.Project
import org.radarbase.appserver.exception.AlreadyExistsException
import org.radarbase.appserver.exception.InvalidProjectDetailsException
import org.radarbase.appserver.exception.NotFoundException
import org.radarbase.appserver.repository.ProjectRepository
import org.radarbase.appserver.util.checkInvalidProjectDetails
import org.radarbase.appserver.util.checkPresence
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Service class for managing projects.
 *
 * [Projects][Project] represent the same entities as in the [ManagementPortal](https://github.com/RADAR-base/ManagementPortal) and their names
 * should align with the projects in the [ManagementPortal](https://github.com/RADAR-base/ManagementPortal).
 *
 * It uses [ProjectRepository] for persistence operations and [ProjectConverter]
 * for converting between entity and DTO objects.
 */
@Service
class ProjectService(
    private val projectRepository: ProjectRepository,
    private val projectConverter: ProjectConverter
) {
    /**
     * Retrieves all projects from the repository.
     *
     * @return [ProjectDTOs] object containing a list of all projects as DTOs.
     */
    @Transactional(readOnly = true)
    fun getAllProjects(): ProjectDTOs {
        return ProjectDTOs(projectConverter.entitiesToDtos(projectRepository.findAll()))
    }

    /**
     * Retrieves a project by its unique identifier (ID).
     *
     * @param id the unique ID of the project
     * @return the [ProjectDTO] of the project
     * @throws NotFoundException if no project with the given ID exists
     */
    @Transactional(readOnly = true)
    fun getProjectById(id: Long): ProjectDTO {
        val project: Project = checkPresence(projectRepository.findByIdOrNull(id)) { "Project with id $id not found" }

        return projectConverter.entityToDto(project)
    }

    /**
     * Retrieves a project by its ManagementPortal project ID.
     *
     * @param projectId the unique project ID in the ManagementPortal
     * @return the [ProjectDTO] of the project
     * @throws NotFoundException if no project with the given project ID exists
     */
    @Transactional(readOnly = true)
    fun getProjectByProjectId(projectId: String): ProjectDTO {
        val project =
            checkPresence(projectRepository.findByProjectId(projectId)) { "Project with projectId $projectId not found" }

        return projectConverter.entityToDto(project)
    }

    /**
     * Creates a new project in the repository.
     *
     * @param projectDTO the [ProjectDTO] containing details of the project to create
     * @return the [ProjectDTO] of the newly created project
     * @throws InvalidProjectDetailsException if the input contains invalid data
     * @throws AlreadyExistsException if the project is already present
     */
    @Transactional
    fun addProject(projectDTO: ProjectDTO): ProjectDTO {
        val projectId: String? = projectDTO.projectId

        checkInvalidProjectDetails(
            projectDTO,
            { projectDTO.id != null },
            { "'id' must not be supplied when creating a project, it is autogenerated" }
        )

        checkInvalidProjectDetails(
            projectDTO,
            { projectId == null },
            { "At least 'project id' must be supplied" }
        )

        if (projectRepository.existsByProjectId(projectId!!)) {
            throw AlreadyExistsException(
                "The project with specified project-id (${projectDTO.projectId}) already exists. Use Update endpoint if need to update the project.",
            )
        }

        return projectConverter.entityToDto(projectRepository.save<Project>(projectConverter.dtoToEntity(projectDTO)))
    }

    /**
     * Updates an existing project in the repository.
     *
     * @param projectDTO the [ProjectDTO] containing updated details of the project
     * @return the updated [ProjectDTO]
     * @throws InvalidProjectDetailsException if the input contains invalid project data
     * @throws NotFoundException if the project to update does not exist
     */
    @Transactional
    fun updateProject(projectDTO: ProjectDTO): ProjectDTO {
        checkInvalidProjectDetails (
            projectDTO,
            { projectDTO.id == null },
            { "The 'id' of the project must be supplied for updating project" }
        )

        val existingProject: Project =
            checkPresence(projectRepository.findByIdOrNull(projectDTO.id)) { "Project with id ${projectDTO.id} not found" }

        val updatedProject = existingProject.copy(projectId = projectDTO.projectId)
        val savedProject = projectRepository.save(updatedProject)

        return projectConverter.entityToDto(savedProject)
    }
}