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
package org.radarbase.appserver.converter

import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.radarbase.appserver.dto.ProjectDto
import org.radarbase.appserver.entity.Project
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/**
 * Converter [Converter] class for [Project] entity.
 *
 * @author yatharthranjan
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class ProjectConverter : Converter<Project, ProjectDto> {
    override fun dtoToEntity(projectDto: ProjectDto): Project {
        return Project().setProjectId(projectDto.projectId)
    }

    override fun entityToDto(project: Project): ProjectDto {
        return ProjectDto()
            .setId(project.id)
            .setProjectId(project.projectId)
            .setCreatedAt(project.createdAt.toInstant())
            .setUpdatedAt(project.updatedAt.toInstant())
    }
}