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

import org.radarbase.appserver.converter.*;
import org.radarbase.appserver.dto.RadarProjectDto;
import org.radarbase.appserver.dto.RadarProjects;
import org.radarbase.appserver.entity.Project;
import org.radarbase.appserver.exception.InvalidProjectDetailsException;
import org.radarbase.appserver.exception.NotFoundException;
import org.radarbase.appserver.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author yatharthranjan
 */
@Service
public class RadarProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public RadarProjects getAllProjects() {
        RadarProjects projects = new RadarProjects();
        return new RadarProjects().setProjects(
                ConverterFactory.getProjectConverter().entitiesToDtos(projectRepository.findAll()));
    }

    @Transactional(readOnly = true)
    public RadarProjectDto getProjectById(Long id) {
        Optional<Project> project = projectRepository.findById(id);

        if (project.isPresent()) {
            return ConverterFactory.getProjectConverter().entityToDto(project.get());
        } else {
            throw new NotFoundException("Project not found with id" + id);
        }
    }

    @Transactional(readOnly = true)
    public RadarProjectDto getProjectByProjectId(String projectId) {
        Optional<Project> project = projectRepository.findByProjectId(projectId);

        if (project.isPresent()) {
            return ConverterFactory.getProjectConverter().entityToDto(project.get());
        } else {
            throw new NotFoundException("Project not found with projectId" + projectId);
        }
    }

    @Transactional(readOnly = true)
    public Project getProjectEntityByProjectId(String projectId) {
        Optional<Project> project = projectRepository.findByProjectId(projectId);

        if (project.isPresent()) {
            return project.get();
        } else {
            throw new NotFoundException("Project not found with projectId" + projectId);
        }
    }

    @Transactional
    public RadarProjectDto addProject(RadarProjectDto projectDto) {

        Optional<Project> project;
        if (projectDto.getProjectId() != null && !projectDto.getProjectId().isEmpty()) {
            project = projectRepository.findByProjectId(projectDto.getProjectId());
        } else if (projectDto.getId() != null) {
            project = projectRepository.findById(projectDto.getId());
        } else {
            throw new InvalidProjectDetailsException(projectDto, new IllegalArgumentException("At least one of 'id' or 'project id' must be supplied"));
        }

        Project resultProject;
        if(project.isPresent()) {
            resultProject = project.get().setProjectId(projectDto.getProjectId());
        } else {
            resultProject = ConverterFactory.getProjectConverter().dtoToEntity(projectDto);
        }
        Project project1 = this.projectRepository.save(resultProject);
        return ConverterFactory.getProjectConverter().entityToDto(project1);
    }

    //TODO Test this as right now creating new project if changing projectId
    @Transactional
    public RadarProjectDto updateProject(RadarProjectDto projectDto) {

        Optional<Project> project = projectRepository.findByProjectId(projectDto.getProjectId());;
        if (project.isPresent()) {
            throw new InvalidProjectDetailsException(projectDto, new IllegalArgumentException("The provided project ID already exists"));
        } else if (projectDto.getId() != null) {
            project = projectRepository.findById(projectDto.getId());
        } else {
            throw new InvalidProjectDetailsException(projectDto, new IllegalArgumentException("At least one of 'id' or 'project id' must be supplied"));
        }

        Project resultProject;
        if (project.isPresent()) {
            resultProject = this.projectRepository.getOne(projectDto.getId());
            resultProject.setProjectId(projectDto.getProjectId());
        } else {
            throw new NotFoundException("The project could not be found with details " + projectDto);
        }

        resultProject = this.projectRepository.save(resultProject);
        return ConverterFactory.getProjectConverter().entityToDto(resultProject);
    }
}