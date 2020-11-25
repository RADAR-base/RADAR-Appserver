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

import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.dto.ProjectDto;
import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.radarbase.appserver.dto.fcm.FcmUserDto;
import org.radarbase.appserver.exception.AlreadyExistsException;
import org.radarbase.appserver.exception.NotFoundException;
import org.radarbase.appserver.service.FcmNotificationService;
import org.radarbase.appserver.service.ProjectService;
import org.radarbase.appserver.service.UserService;

@Slf4j
public class SimpleScheduleNotificationHandler implements ScheduleNotificationHandler {

  private final transient FcmNotificationService notificationService;
  private final transient ProjectService projectService;
  private final transient UserService userService;

  public SimpleScheduleNotificationHandler(
      FcmNotificationService notificationService,
      ProjectService projectService,
      UserService userService) {
    this.notificationService = notificationService;
    this.projectService = projectService;
    this.userService = userService;
  }

  @Override
  public void handleScheduleNotification(
      FcmNotificationDto notificationDto, FcmUserDto userDto, String projectId) {
    try {
      notificationService.addNotification(notificationDto, userDto.getSubjectId(), projectId);
    } catch (NotFoundException ex) {
      if (ex.getMessage().contains("Project")) {
        try {
          projectService.addProject(new ProjectDto().setProjectId(projectId));
          userService.saveUserInProject(userDto.setProjectId(projectId));
        } catch (Exception e) {
          log.warn("Exception while adding notification.", ex);
        }
      } else if (ex.getMessage().contains("Subject")) {
        userService.saveUserInProject(userDto.setProjectId(projectId));
      }
      notificationService.addNotification(notificationDto, userDto.getSubjectId(), projectId);
    } catch (AlreadyExistsException ex) {
      log.warn("The Notification Already Exists.", ex);
    }
  }
}
