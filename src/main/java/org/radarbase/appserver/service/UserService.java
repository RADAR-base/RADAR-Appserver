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

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.converter.UserConverter;
import org.radarbase.appserver.dto.fcm.FcmUserDto;
import org.radarbase.appserver.dto.fcm.FcmUsers;
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

  private final transient UserConverter userConverter;
  private final transient UserRepository userRepository;
  private final transient ProjectRepository projectRepository;

  @Autowired
  private final transient QuestionnaireScheduleService scheduleService;

  private static final String FCM_TOKEN_PREFIX = "unregistered_";

  @Autowired
  public UserService(
      UserConverter userConverter,
      UserRepository userRepository,
      ProjectRepository projectRepository,
      QuestionnaireScheduleService scheduleService) {
    this.userConverter = userConverter;
    this.userRepository = userRepository;
    this.projectRepository = projectRepository;
    this.scheduleService = scheduleService;
  }

  @Transactional(readOnly = true)
  public FcmUsers getAllRadarUsers() {
    return new FcmUsers().setUsers(userConverter.entitiesToDtos(userRepository.findAll()));
  }

  @Transactional(readOnly = true)
  public FcmUserDto getUserById(Long id) {
    User user = userRepository.findById(id)
            .orElseThrow(() -> new NotFoundException(
                    "No User was found with the ID - " + id));

    return userConverter.entityToDto(user);
  }

  @Transactional(readOnly = true)
  public FcmUserDto getUserBySubjectId(String subjectId) {
    User user = userRepository.findBySubjectId(subjectId)
            .orElseThrow(() -> new NotFoundException(
                    "No User was found with the subject ID - " + subjectId));

    return userConverter.entityToDto(user);
  }

  @Transactional(readOnly = true)
  public FcmUsers getUsersByProjectId(String projectId) {
    Project project = projectRepository.findByProjectId(projectId)
            .orElseThrow(() -> new NotFoundException(
                    "Project not found with projectId " + projectId));

    List<User> users = this.userRepository.findByProjectId(project.getId());

    return new FcmUsers().setUsers(userConverter.entitiesToDtos(users));
  }

  @Transactional(readOnly = true)
  public FcmUserDto getUsersByProjectIdAndSubjectId(String projectId, String subjectId) {
    Project project = projectRepository.findByProjectId(projectId)
            .orElseThrow(() -> new NotFoundException(
                    "Project not found with projectId " + projectId));

    User user = this.userRepository.findBySubjectIdAndProjectId(subjectId, project.getId())
            .orElseThrow(() -> new NotFoundException(
                    "No User was found with the subject ID - " + subjectId));

    return userConverter.entityToDto(user);
  }

  @Transactional
  public void checkFcmTokenExistsAndReplace(FcmUserDto userDto) {
      this.userRepository.findByFcmToken(userDto.getFcmToken())
              .filter(user -> !user.getSubjectId().equals(userDto.getSubjectId()))
              .ifPresent(user -> {
                user.setFcmToken(FCM_TOKEN_PREFIX + Instant.now().toString());
                this.userRepository.save(user);
              });
  }

  @Transactional
  public FcmUserDto saveUserInProject(FcmUserDto userDto) {

    // TODO: Future -- If any value is null get them using the MP api using others. (eg only subject
    // id, then get project id and source ids from MP)
    // TODO: Make the above pluggable so can use others or none.

    log.debug("User DTO:" + userDto);
    Project project = this.projectRepository.findByProjectId(userDto.getProjectId())
            .orElseThrow(() -> new NotFoundException(
                    "Project Id does not exist. Please create a project with the ID first"));

    Optional<User> existingUser = this.userRepository.findBySubjectIdAndProjectId(
            userDto.getSubjectId(), project.getId());

    if (existingUser.isPresent()) {
      throw new InvalidUserDetailsException(
          "The user with specified subject ID "
              + userDto.getSubjectId()
              + " already exists in project ID "
              + userDto.getProjectId()
              + ". Please use Update endpoint if need to update the user");
    }

    User newUser = userConverter.dtoToEntity(userDto).setProject(project);
    // maintain a bi-directional relationship
    newUser.getUsermetrics().setUser(newUser);
    User savedUser = this.userRepository.save(newUser);
    // Generate schedule for user
    this.scheduleService.generateScheduleForUser(savedUser);
    return userConverter.entityToDto(savedUser);
  }

  // TODO update to use Id instead of subjectId
  @Transactional
  public FcmUserDto updateUser(FcmUserDto userDto) {
    Project project = this.projectRepository.findByProjectId(userDto.getProjectId())
            .orElseThrow(() -> new NotFoundException(
                    "Project Id does not exist. Please create a project with the ID first"));

    User user = this.userRepository.findBySubjectIdAndProjectId(
            userDto.getSubjectId(), project.getId())
            .orElseThrow(() -> new InvalidUserDetailsException(
                    "The user with specified subject ID "
                            + userDto.getSubjectId()
                            + " does not exist in project ID "
                            + userDto.getProjectId()
                            + ". Please use CreateUser endpoint to create the user."))
            .setFcmToken(userDto.getFcmToken())
            .setUserMetrics(UserConverter.getValidUserMetrics(userDto))
            .setEnrolmentDate(userDto.getEnrolmentDate())
            .setTimezone(userDto.getTimezone())
            .setAttributes(userDto.getAttributes());

    // maintain a bi-directional relationship
    user.getUsermetrics().setUser(user);
    User savedUser = this.userRepository.save(user);
    // Generate schedule for user
    this.scheduleService.generateScheduleForUser(savedUser);
    return userConverter.entityToDto(savedUser);
  }

  @Transactional
  public void updateLastDelivered(String fcmToken, Instant lastDelivered) {
    User user = userRepository.findByFcmToken(fcmToken)
            .orElseThrow(() -> new InvalidUserDetailsException(
                    "The user with specified FCM Token " + fcmToken + " does not exist."));

    user.getUsermetrics().setLastDelivered(lastDelivered);
    userRepository.save(user);
  }

  public void deleteUserByProjectIdAndSubjectId(String projectId, String subjectId) {
    Project project = this.projectRepository.findByProjectId(projectId)
            .orElseThrow(() -> new NotFoundException(
                    "Project Id does not exist. Cannot delete user without a valid project."));

    User user = this.userRepository.findBySubjectIdAndProjectId(subjectId, project.getId())
            .orElseThrow(() -> new InvalidUserDetailsException(
                    "The user with specified subject ID "
                            + subjectId
                            + " does not exist in project ID "
                            + projectId
                            + ". Please specify a valid user for deleting."));

    this.userRepository.deleteById(user.getId());
  }
}
