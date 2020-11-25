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

package org.radarbase.appserver.service.fcm;

import java.time.Duration;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.dto.ProjectDto;
import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.radarbase.appserver.dto.fcm.FcmNotifications;
import org.radarbase.appserver.dto.fcm.FcmUserDto;
import org.radarbase.appserver.exception.AlreadyExistsException;
import org.radarbase.appserver.exception.NotFoundException;
import org.radarbase.appserver.service.FcmNotificationService;
import org.radarbase.appserver.service.ProjectService;
import org.radarbase.appserver.service.UserService;
import org.radarbase.appserver.util.ExpiringMap;

@Slf4j
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class BatchedScheduleNotificationHandler implements ScheduleNotificationHandler {

  private final transient FcmNotificationService notificationService;
  private final transient ProjectService projectService;
  private final transient UserService userService;
  private final transient ExpiringMap<UserWithId, FcmNotifications> notificationDtoExpiringMap;

  public BatchedScheduleNotificationHandler(
      FcmNotificationService notificationService,
      ProjectService projectService,
      UserService userService,
      int maxSize,
      Duration expiry,
      long flushAfter) {
    this.notificationService = notificationService;
    this.projectService = projectService;
    this.userService = userService;
    notificationDtoExpiringMap =
        new ExpiringMap<>(this::addNotifications, maxSize, expiry, flushAfter);
  }

  @Override
  public void handleScheduleNotification(
      FcmNotificationDto notificationDto, FcmUserDto userDto, String projectId) {
    UserWithId user = new UserWithId(userDto.getSubjectId(), projectId, userDto);
    FcmNotifications fcmNotifications;
    try {
      fcmNotifications = notificationDtoExpiringMap.get(user);
    } catch (NoSuchElementException exc) {
      fcmNotifications = new FcmNotifications();
    }
    fcmNotifications.addNotification(notificationDto);
    notificationDtoExpiringMap.add(user, fcmNotifications);
  }

  private void addNotifications(Map<UserWithId, FcmNotifications> notificationDtoMap) {
    // Add notifications in batch for each user
    for (final Entry<UserWithId, FcmNotifications> entry : notificationDtoMap.entrySet()) {
      UserWithId user = entry.getKey();
      try {
        notificationService.addNotifications(
            entry.getValue(), user.getSubjectId(), user.getProjectId(), "true");
      } catch (NotFoundException ex) {
        if (ex.getMessage().contains("Project")) {
          try {
            projectService.addProject(new ProjectDto().setProjectId(user.getProjectId()));
            userService.saveUserInProject(user.getFcmUserDto().setProjectId(user.getProjectId()));
          } catch (Exception e) {
            log.warn("Exception while adding notification.", ex);
          }
        } else if (ex.getMessage().contains("Subject")) {
          userService.saveUserInProject(user.getFcmUserDto().setProjectId(user.getProjectId()));
        }
        notificationService.addNotifications(
            entry.getValue(), user.getSubjectId(), user.getProjectId(), "true");
      } catch (AlreadyExistsException ex) {
        log.warn("The Notification Already Exists.", ex);
      }
    }
  }

  public static class UserWithId {
    private final String subjectId;
    private final String projectId;
    private final FcmUserDto fcmUserDto;

    public UserWithId(String subjectId, String projectId, FcmUserDto fcmUserDto) {
      this.subjectId = subjectId;
      this.projectId = projectId;
      this.fcmUserDto = fcmUserDto;
    }

    public String getSubjectId() {
      return subjectId;
    }

    public String getProjectId() {
      return projectId;
    }

    public FcmUserDto getFcmUserDto() {
      return fcmUserDto;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof UserWithId)) {
        return false;
      }
      UserWithId that = (UserWithId) o;
      return Objects.equals(subjectId, that.subjectId) && Objects.equals(projectId, that.projectId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(subjectId, projectId);
    }
  }
}
