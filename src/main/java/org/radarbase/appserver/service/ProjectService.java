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

package org.radarbase.appserver.service;

import java.util.Optional;
import org.radarbase.appserver.converter.ProjectConverter;
import org.radarbase.appserver.dto.ProjectDto;
import org.radarbase.appserver.dto.ProjectDtos;
import org.radarbase.appserver.entity.Project;
import org.radarbase.appserver.exception.InvalidProjectDetailsException;
import org.radarbase.appserver.exception.NotFoundException;
import org.radarbase.appserver.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link Service} for interacting with the {@link Project} {@link javax.persistence.Entity} using
 * the {@link ProjectRepository}.
 *
 * @author yatharthranjan
 */
@Service
public class ProjectService {

  private final transient ProjectRepository projectRepository;
  private final transient ProjectConverter projectConverter;

  @Autowired
  public ProjectService(ProjectRepository projectRepository, ProjectConverter projectConverter) {
    this.projectRepository = projectRepository;
    this.projectConverter = projectConverter;
  }

  @Transactional(readOnly = true)
  public ProjectDtos getAllProjects() {
    return new ProjectDtos()
        .setProjects(projectConverter.entitiesToDtos(projectRepository.findAll()));
  }

  @Transactional(readOnly = true)
  public ProjectDto getProjectById(Long id) {
    Optional<Project> project = projectRepository.findById(id);

    if (project.isPresent()) {
      return projectConverter.entityToDto(project.get());
    } else {
      throw new NotFoundException("Project not found with id" + id);
    }
  }

  @Transactional(readOnly = true)
  public ProjectDto getProjectByProjectId(String projectId) {
    Optional<Project> project = projectRepository.findByProjectId(projectId);

    if (project.isPresent()) {
      return projectConverter.entityToDto(project.get());
    } else {
      throw new NotFoundException("Project not found with projectId" + projectId);
    }
  }

  @Transactional
  public ProjectDto addProject(ProjectDto projectDto) {
    if (projectDto.getId() != null) {
      throw new InvalidProjectDetailsException(
          projectDto,
          new IllegalArgumentException(
              "'id' must not be supplied when creating a project as it is autogenerated."));
    }

    if (projectDto.getProjectId() == null || projectDto.getProjectId().isEmpty()) {
      throw new InvalidProjectDetailsException(
          projectDto, new IllegalArgumentException("At least 'project id' must be supplied"));
    }

    if (projectRepository.existsByProjectId(projectDto.getProjectId())) {
      throw new InvalidProjectDetailsException(
          String.format(
              "The project with specified project-id %s already exists. Use Update endpoint if need to update the project.",
              projectDto.getProjectId()));
    }

    return projectConverter.entityToDto(
            this.projectRepository.save(projectConverter.dtoToEntity(projectDto)));
  }

  @Transactional
  public ProjectDto updateProject(ProjectDto projectDto) {
    if (projectDto.getId() == null) {
      throw new InvalidProjectDetailsException(
          projectDto,
          new IllegalArgumentException("The 'id' of the project must be supplied for updating."));
    }
    Optional<Project> project = projectRepository.findById(projectDto.getId());

    Project resultProject;
    if (project.isPresent()) {
      resultProject = project.get().setProjectId(projectDto.getProjectId());
    } else {
      throw new NotFoundException("The project could not be found with details " + projectDto);
    }

    Project savedProject = this.projectRepository.save(resultProject);
    return projectConverter.entityToDto(savedProject);
  }
}
