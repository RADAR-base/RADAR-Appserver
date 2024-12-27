package org.radarbase.appserver.dto

import jakarta.validation.constraints.Size

data class ProjectDTOs (
    @field:Size(max = 500)
    val projects: MutableList<ProjectDTO> = mutableListOf(),
) {
    fun withProjects(projects: MutableList<ProjectDTO>) {
        this.projects.clear()
        this.projects.addAll(projects)
    }

    fun addProject(projectDto: ProjectDTO) {
        this.projects.add(projectDto)
    }
}