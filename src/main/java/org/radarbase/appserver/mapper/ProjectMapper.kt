/*
 *
 *  *
 *  *  * Copyright 2018 King's College London
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *  *
 *  *
 *
 */

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