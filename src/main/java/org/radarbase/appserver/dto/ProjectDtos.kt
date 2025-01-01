package org.radarbase.appserver.dto

import jakarta.validation.constraints.Size

data class ProjectDtos (
    @field:Size(max = 500)
    val projects: MutableList<ProjectDto> = mutableListOf(),
) {
    fun withProjects(projects: MutableList<ProjectDto>): ProjectDtos = apply{
        this.projects.clear()
        this.projects.addAll(projects)
    }

    fun addProject(projectDto: ProjectDto): ProjectDtos = apply {
        this.projects.add(projectDto)
    }
}