/*
 * Copyright 2025 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        createdAt = entity.createdAt?.toInstant(),
        updatedAt = entity.updatedAt?.toInstant(),
    )
}
