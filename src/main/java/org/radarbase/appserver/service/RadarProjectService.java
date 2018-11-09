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

import org.radarbase.appserver.converter.Converter;
import org.radarbase.appserver.converter.NotificationConverter;
import org.radarbase.appserver.converter.ProjectConverter;
import org.radarbase.appserver.converter.UserConverter;
import org.radarbase.appserver.dto.RadarProjectDto;
import org.radarbase.appserver.dto.RadarProjects;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.entity.Project;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.exception.InvalidProjectDetailsException;
import org.radarbase.appserver.exception.NotFoundException;
import org.radarbase.appserver.repository.ProjectRepository;
import org.radarbase.fcm.dto.FcmNotificationDto;
import org.radarbase.fcm.dto.FcmNotifications;
import org.radarbase.fcm.dto.FcmUserDto;
import org.radarbase.fcm.dto.FcmUsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yatharthranjan
 */
@Service
public class RadarProjectService {

    @Autowired
    private ProjectRepository projectRepository;

    private Converter<Project, RadarProjectDto> projectConverter = new ProjectConverter();
    private Converter<Notification, FcmNotificationDto> notificationConverter = new NotificationConverter();
    private Converter<User, FcmUserDto> userConverter = new UserConverter();

    @Transactional(readOnly = true)
    public RadarProjects getAllProjects() {
        RadarProjects projects = new RadarProjects();
        return new RadarProjects().setProjects(
                projectConverter.entitiesToDtos(projectRepository.findAll()));
    }

    @Transactional(readOnly = true)
    public RadarProjectDto getProjectById(Long id) {
        Optional<Project> project = projectRepository.findById(id);

        if(project.isPresent()) {
            return projectConverter.entityToDto(project.get());
        } else {
            throw new NotFoundException("Project not found with id" + id);
        }
    }

    @Transactional(readOnly = true)
    public RadarProjectDto getProjectByProjectId(String projectId) {
        Optional<Project> project = projectRepository.findByProjectId(projectId);

        if(project.isPresent()) {
            return projectConverter.entityToDto(project.get());
        } else {
            throw new NotFoundException("Project not found with projectId" + projectId);
        }
    }

    @Transactional(readOnly = true)
    public FcmNotifications getNotificationsByProjectId(String projectId) {
        Optional<Project> project = projectRepository.findByProjectId(projectId);

        if (project.isPresent()) {
            /*
            project.get().getUsers().forEach(u ->
                    notificationList.addAll(notificationConverter.entitiesToDtos(u.getNotifications())));*/

            return new FcmNotifications().setNotifications(project.get().getUsers()
                    .parallelStream()
                    .flatMap(user -> notificationConverter.entitiesToDtos(user.getNotifications()).stream())
                    .collect(Collectors.toList()));
        } else {
            throw new NotFoundException("Project not found with projectId " + projectId);
        }
    }

    @Transactional(readOnly = true)
    public FcmNotifications getNotificationsByProjectIdAndSubjectId(String projectId, String subjectId) {
        Optional<Project> project = projectRepository.findByProjectId(projectId);

        if (project.isPresent()) {

            Set<User> users = project.get().getUsers();
            if(users.isEmpty()) {
                throw new NotFoundException("User not found since there are no users associated with the projectId " + projectId);
            } else {
                for(User user: users) {
                    if(user.getSubjectId().equals(subjectId)) {
                        return new FcmNotifications().setNotifications(
                                notificationConverter.entitiesToDtos(user.getNotifications()));
                    }
                }
                throw new NotFoundException("User not found with subjectId" + subjectId + " and projectId " + projectId);
            }
        } else {
            throw new NotFoundException("Project not found with projectId " + projectId);
        }
    }

    @Transactional(readOnly = true)
    public FcmUsers getUsersByProjectId(String projectId) {
        Optional<Project> project = projectRepository.findByProjectId(projectId);

        if (project.isPresent()) {
            return new FcmUsers().setUsers(userConverter.entitiesToDtos(project.get().getUsers()));
        } else {
            throw new NotFoundException("Project not found with projectId " + projectId);
        }
    }

    @Transactional
    public RadarProjectDto addProject(RadarProjectDto projectDto) {
        Optional<Project> project;
        if (projectDto.getId() == null) {
            project = projectRepository.findByProjectId(projectDto.getProjectId());
        } else {
            project = projectRepository.findById(projectDto.getId());
        }

        Project resultProject;
        if(project.isPresent()) {
            resultProject = project.get();
            resultProject.setProjectId(projectDto.getProjectId())
                    .setUsers(new HashSet<>(userConverter.dtosToEntities(projectDto.getFcmUsers().getUsers())));
        } else {
            if(projectDto.getProjectId() == null || projectDto.getProjectId().isEmpty()) {
                throw new InvalidProjectDetailsException(projectDto, new IllegalArgumentException("'Project id' must be supplied for adding new projects."));
            }
            resultProject = projectConverter.dtoToEntity(projectDto);
        }
        Project project1 = this.projectRepository.save(resultProject);
        return this.projectConverter.entityToDto(project1);
    }

    @Transactional
    public RadarProjectDto updateProject(RadarProjectDto projectDto) {

        Optional<Project> project;
        if(projectDto.getProjectId() != null && !projectDto.getProjectId().isEmpty()) {
            project = projectRepository.findByProjectId(projectDto.getProjectId());
        } else if(projectDto.getId() != null) {
            project = projectRepository.findById(projectDto.getId());
        } else {
            throw new InvalidProjectDetailsException(projectDto, new IllegalArgumentException("At least one of 'id' or 'project id' must be supplied"));
        }

        Project resultProject;
        if(project.isPresent()) {
            resultProject = project.get();
            resultProject.setProjectId(projectDto.getProjectId())
                    .setUsers(new HashSet<>(userConverter.dtosToEntities(projectDto.getFcmUsers().getUsers())));
        } else {
            throw new NotFoundException("The project could not be found with details " + projectDto);
        }

        resultProject = this.projectRepository.save(resultProject);
        return projectConverter.entityToDto(resultProject);
    }
}