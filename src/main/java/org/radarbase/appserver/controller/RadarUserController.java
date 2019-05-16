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

package org.radarbase.appserver.controller;

import java.net.URI;
import java.net.URISyntaxException;
import javax.validation.Valid;
import javax.websocket.server.PathParam;
import org.radarbase.appserver.dto.fcm.FcmUserDto;
import org.radarbase.appserver.dto.fcm.FcmUsers;
import org.radarbase.appserver.exception.InvalidUserDetailsException;
import org.radarbase.appserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Resource Endpoint for getting and adding users. Each notification {@link
 * org.radarbase.appserver.entity.Notification} needs to be associated to a user. A user may
 * represent a Management Portal subject.
 *
 * @see <a href="https://github.com/RADAR-base/ManagementPortal">Management Portal</a>
 * @author yatharthranjan
 */
@RestController
public class RadarUserController {

  @Autowired private transient UserService userService;

  @PostMapping("/users")
  public ResponseEntity addUser(@Valid @RequestBody FcmUserDto userDto) throws URISyntaxException {

    FcmUserDto user = this.userService.saveUserInProject(userDto);
    return ResponseEntity.created(new URI("/users/user?id=" + user.getId())).body(user);
  }

  @PostMapping("/" + Paths.PROJECT_PATH + "/" + Paths.PROJECT_ID_CONSTANT + "/" + Paths.USER_PATH)
  public ResponseEntity addUserToProject(
      @Valid @RequestBody FcmUserDto userDto,
      @Valid @PathVariable(value = Paths.PROJECT_ID_CONSTANT) String projectId)
      throws URISyntaxException {
    userDto.setProjectId(projectId);
    FcmUserDto user = this.userService.saveUserInProject(userDto);
    return ResponseEntity.created(new URI("/" + Paths.USER_PATH + "/user?id=" + user.getId()))
        .body(user);
  }

  @PutMapping("/" + Paths.PROJECT_PATH + "/" + Paths.PROJECT_ID_CONSTANT + "/" + Paths.USER_PATH)
  public ResponseEntity updateUserInProject(
      @Valid @RequestBody FcmUserDto userDto,
      @Valid @PathVariable(value = Paths.PROJECT_ID_CONSTANT) String projectId)
      throws URISyntaxException {
    userDto.setProjectId(projectId);
    FcmUserDto user = this.userService.updateUser(userDto);
    return ResponseEntity.ok(user);
  }

  @PutMapping("/" + Paths.USER_PATH)
  public ResponseEntity updateUser(@Valid @RequestBody FcmUserDto userDto)
      throws URISyntaxException {
    FcmUserDto user = this.userService.updateUser(userDto);
    return ResponseEntity.ok(user);
  }

  @GetMapping("/" + Paths.USER_PATH)
  public ResponseEntity<FcmUsers> getAllRadarUsers() {
    return ResponseEntity.ok(this.userService.getAllRadarUsers());
  }

  @GetMapping("/" + Paths.USER_PATH + "/user")
  public ResponseEntity<FcmUserDto> getRadarUserUsingId(@PathParam("id") Long id) {
    if (id == null) {
      throw new InvalidUserDetailsException("The given id must not be null!");
    }
    return ResponseEntity.ok(this.userService.getUserById(id));
  }

  @GetMapping("/" + Paths.USER_PATH + "/" + Paths.SUBJECT_ID_CONSTANT)
  public ResponseEntity<FcmUserDto> getRadarUserUsingSubjectId(
      @PathVariable(Paths.SUBJECT_ID_CONSTANT) String subjectId) {
    return ResponseEntity.ok(this.userService.getUserBySubjectId(subjectId));
  }

  @GetMapping("/" + Paths.PROJECT_PATH + "/" + Paths.PROJECT_ID_CONSTANT + "/" + Paths.USER_PATH)
  public ResponseEntity<FcmUsers> getUsersUsingProjectId(
      @Valid @PathVariable(Paths.PROJECT_ID_CONSTANT) String projectId) {
    return ResponseEntity.ok(this.userService.getUsersByProjectId(projectId));
  }

  @GetMapping(
      "/"
          + Paths.PROJECT_PATH
          + "/"
          + Paths.PROJECT_ID_CONSTANT
          + "/"
          + Paths.USER_PATH
          + "/"
          + Paths.SUBJECT_ID_CONSTANT)
  public ResponseEntity<FcmUserDto> getUsersUsingProjectIdAndSubjectId(
      @Valid @PathVariable(Paths.PROJECT_ID_CONSTANT) String projectId,
      @Valid @PathVariable(Paths.SUBJECT_ID_CONSTANT) String subjectId) {

    return ResponseEntity.ok(
        this.userService.getUsersByProjectIdAndSubjectId(projectId, subjectId));
  }
}
