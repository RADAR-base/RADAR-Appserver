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

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.converter.Converter;
import org.radarbase.appserver.converter.ConverterFactory;
import org.radarbase.appserver.converter.NotificationConverter;
import org.radarbase.appserver.converter.ProjectConverter;
import org.radarbase.appserver.converter.UserConverter;
import org.radarbase.appserver.dto.ProjectDto;
import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.radarbase.appserver.dto.fcm.FcmUserDto;
import org.radarbase.appserver.dto.fcm.FcmUsers;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.entity.Project;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.exception.InvalidUserDetailsException;
import org.radarbase.appserver.exception.NotFoundException;
import org.radarbase.appserver.repository.ProjectRepository;
import org.radarbase.appserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link Service} for interacting with the {@link User} {@link javax.persistence.Entity} using the
 * {@link UserRepository}.
 *
 * @author yatharthranjan
 */
@Service
@Transactional
@Slf4j
public class UserService {

  @Autowired private UserRepository userRepository;

  @Autowired private ProjectRepository projectRepository;

  private static final Converter<User, FcmUserDto> userConverter = new UserConverter();
  private static final Converter<Notification, FcmNotificationDto> notificationConverter =
      new NotificationConverter();
  private static final Converter<Project, ProjectDto> projectConverter = new ProjectConverter();

  @Transactional(readOnly = true)
  public FcmUsers getAllRadarUsers() {
    return new FcmUsers().setUsers(userConverter.entitiesToDtos(userRepository.findAll()));
  }

  @Transactional(readOnly = true)
  public FcmUserDto getUserById(Long id) {
    Optional<User> user = userRepository.findById(id);

    if (user.isPresent()) {
      return userConverter.entityToDto(user.get());
    } else {
      throw new NotFoundException("No User was found with the ID - " + id);
    }
  }

  @Transactional(readOnly = true)
  public FcmUserDto getUserBySubjectId(String subjectId) {
    Optional<User> user = userRepository.findBySubjectId(subjectId);

    if (user.isPresent()) {
      return userConverter.entityToDto(user.get());
    } else {
      throw new NotFoundException("No User was found with the subject ID - " + subjectId);
    }
  }

  @Transactional(readOnly = true)
  public FcmUsers getUsersByProjectId(String projectId) {
    Optional<Project> project = projectRepository.findByProjectId(projectId);

    if (project.isEmpty()) {
      throw new NotFoundException("Project not found with projectId " + projectId);
    }

    List<User> users = this.userRepository.findByProjectId(project.get().getId());

    return new FcmUsers().setUsers(userConverter.entitiesToDtos(users));
  }

  @Transactional
  public FcmUserDto saveUserInProject(FcmUserDto userDto) {

    // TODO: Future -- If any value is null get them using the MP api using others. (eg only subject
    // id, then get project id and source ids from MP)
    // TODO: Make the above pluggable so can use others or none.

    log.debug("User DTO:" + userDto);
    Optional<Project> project = this.projectRepository.findByProjectId(userDto.getProjectId());
    if (project.isEmpty()) {
      throw new NotFoundException(
          "Project Id does not exist. Please create a project with the ID first");
    }

    Optional<User> user =
        this.userRepository.findBySubjectIdAndProjectId(
            userDto.getSubjectId(), project.get().getId());

    if (user.isPresent()) {
      throw new InvalidUserDetailsException(
          "The user with specified subject ID "
              + userDto.getSubjectId()
              + " already exists in project ID "
              + userDto.getProjectId()
              + ". Please use Update endpoint if need to update the user");
    } else {
      User newUser =
          ConverterFactory.getUserConverter().dtoToEntity(userDto).setProject(project.get());
      // maintain a bi-directional relationship
      newUser.getUsermetrics().setUser(newUser);
      return ConverterFactory.getUserConverter().entityToDto(this.userRepository.save(newUser));
    }
  }

  // TODO update to use Id instead of subjectId
  @Transactional
  public FcmUserDto updateUser(FcmUserDto userDto) {
    Optional<Project> project = this.projectRepository.findByProjectId(userDto.getProjectId());
    if (project.isEmpty()) {
      throw new NotFoundException(
          "Project Id does not exist. Please create a project with the ID first");
    }

    Optional<User> user =
        this.userRepository.findBySubjectIdAndProjectId(
            userDto.getSubjectId(), project.get().getId());

    if (user.isEmpty()) {
      throw new InvalidUserDetailsException(
          "The user with specified subject ID "
              + userDto.getSubjectId()
              + " does not exist in project ID "
              + userDto.getProjectId()
              + ". Please use CreateUser endpoint to create the user.");
    } else {
      User updatedUser =
          user.get()
              .setFcmToken(userDto.getFcmToken())
              .setUserMetrics(UserConverter.getValidUserMetrics(userDto))
              .setEnrolmentDate(userDto.getEnrolmentDate())
              .setTimezone(userDto.getTimezone());
      // maintain a bi-directional relationship
      updatedUser.getUsermetrics().setUser(updatedUser);
      return ConverterFactory.getUserConverter().entityToDto(this.userRepository.save(updatedUser));
    }
  }
}
