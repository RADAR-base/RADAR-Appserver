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

package org.radarbase.appserver.converter;

import org.radarbase.appserver.dto.ProjectDTO;
import org.radarbase.appserver.entity.Project;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Converter {@link Converter} class for {@link Project} entity.
 *
 * @author yatharthranjan
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class ProjectConverter implements Converter<Project, ProjectDTO> {

  @Override
  public Project dtoToEntity(ProjectDTO projectDto) {

    return new Project(projectDto.getProjectId());
  }

  @Override
  public ProjectDTO entityToDto(Project project) {

    return new ProjectDTO(project.getId(), project.getProjectId(), project.getCreatedAt().toInstant(), project.getUpdatedAt().toInstant());
  }
}
