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
import org.radarbase.appserver.converter.ConverterFactory;
import org.radarbase.appserver.dto.ProjectDto;
import org.radarbase.appserver.dto.Projects;
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

  @Autowired private ProjectRepository projectRepository;

  @Transactional(readOnly = true)
  public Projects getAllProjects() {
    Projects projects = new Projects();
    return new Projects()
        .setProjects(
            ConverterFactory.getProjectConverter().entitiesToDtos(projectRepository.findAll()));
  }

  @Transactional(readOnly = true)
  public ProjectDto getProjectById(Long id) {
    Optional<Project> project = projectRepository.findById(id);

    if (project.isPresent()) {
      return ConverterFactory.getProjectConverter().entityToDto(project.get());
    } else {
      throw new NotFoundException("Project not found with id" + id);
    }
  }

  @Transactional(readOnly = true)
  public ProjectDto getProjectByProjectId(String projectId) {
    Optional<Project> project = projectRepository.findByProjectId(projectId);

    if (project.isPresent()) {
      return ConverterFactory.getProjectConverter().entityToDto(project.get());
    } else {
      throw new NotFoundException("Project not found with projectId" + projectId);
    }
  }

  @Transactional
  public ProjectDto addProject(ProjectDto projectDto) {

    Optional<Project> project;
    if (projectDto.getProjectId() != null && !projectDto.getProjectId().isEmpty()) {
      project = projectRepository.findByProjectId(projectDto.getProjectId());
    } else if (projectDto.getId() != null) {
      project = projectRepository.findById(projectDto.getId());
    } else {
      throw new InvalidProjectDetailsException(
          projectDto,
          new IllegalArgumentException("At least one of 'id' or 'project id' must be supplied"));
    }

    Project resultProject;
    if (project.isPresent()) {
      throw new InvalidProjectDetailsException(
          "The project with specified ID "
              + projectDto.getId()
              + " or project-id "
              + projectDto.getProjectId()
              + " already exists. Please use Update endpoint if need to update the project.");
    } else {
      resultProject = ConverterFactory.getProjectConverter().dtoToEntity(projectDto);
    }
    Project project1 = this.projectRepository.save(resultProject);
    return ConverterFactory.getProjectConverter().entityToDto(project1);
  }

  // TODO Test this as right now creating new project if changing projectId
  @Transactional
  public ProjectDto updateProject(ProjectDto projectDto) {
    Optional<Project> project;
    if (projectDto.getId() != null) {
      project = projectRepository.findById(projectDto.getId());
    } else {
      throw new InvalidProjectDetailsException(
          projectDto,
          new IllegalArgumentException("The 'id' of the project must be supplied for updating."));
    }

    Project resultProject;
    if (project.isPresent()) {
      resultProject = project.get().setProjectId(projectDto.getProjectId());
    } else {
      throw new NotFoundException("The project could not be found with details " + projectDto);
    }

    resultProject = this.projectRepository.save(resultProject);
    return ConverterFactory.getProjectConverter().entityToDto(resultProject);
  }
}
