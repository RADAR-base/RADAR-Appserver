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

import org.radarbase.appserver.service.RadarProjectService;
import org.radarbase.fcm.dto.FcmUsers;
import org.radarbase.fcm.dto.FcmNotifications;
import org.radarbase.appserver.service.FcmNotificationService;
import org.radarbase.appserver.service.UserService;
import org.radarbase.fcm.dto.FcmUserDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.websocket.server.PathParam;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author yatharthranjan
 */
@RestController
public class RadarUserController {
    private static final Logger logger = LoggerFactory.getLogger(RadarUserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private FcmNotificationService notificationService;

    @Autowired
    private RadarProjectService projectService;

    @PostMapping("/users")
    public ResponseEntity addUser(@RequestBody FcmUserDto userDto)
            throws URISyntaxException {

        FcmUserDto user = this.userService.saveUserInProject(userDto);
        return ResponseEntity
                .created(new URI("/user/" + user.getId())).body(user);
    }

    @PostMapping("/projects/{projectId}/users")
    public ResponseEntity addUserToProject(@Valid @RequestBody FcmUserDto userDto,
                                           @Valid @PathVariable(value = "projectId") String projectId)
            throws URISyntaxException {
        userDto.setProjectId(projectId);
        FcmUserDto user = this.userService.saveUserInProject(userDto);
        return ResponseEntity
                .created(new URI("/user/" + user.getId())).body(user);
    }

    @PutMapping("/projects/{projectId}/users")
    public ResponseEntity updateUserInProject(@Valid @RequestBody FcmUserDto userDto,
                                              @Valid @PathVariable(value = "projectId") String projectId)
            throws URISyntaxException {
        userDto.setProjectId(projectId);
        FcmUserDto user = this.userService.updateUser(userDto);
        return ResponseEntity
                .created(new URI("/user/" + user.getId())).body(user);
    }

    @PutMapping("/users")
    public ResponseEntity updateUser(@Valid @RequestBody FcmUserDto userDto)
            throws URISyntaxException {

        FcmUserDto user = this.userService.updateUser(userDto);
        return ResponseEntity
                .created(new URI("/users/" + user.getSubjectId())).body(user);
    }

    @GetMapping("/users")
    public ResponseEntity<FcmUsers> getAllRadarUsers() {
        return ResponseEntity.ok(this.userService.getAllRadarUsers());
    }

    @GetMapping("/users/user")
    public ResponseEntity<FcmUserDto> getRadarUserUsingId(
            @PathParam("id") Long id) {
        return ResponseEntity.ok(this.userService.getUserById(id));
    }

    @GetMapping("/users/{subjectid}")
    public ResponseEntity<FcmUserDto> getRadarUserUsingSubjectId(
            @PathVariable("subjectId") String subjectId) {
        return ResponseEntity.ok(this.userService.getUserBySubjectId(subjectId));
    }

    @GetMapping("/projects/{projectId}/users")
    public ResponseEntity<FcmUsers> getUsersUsingProjectId(@Valid @PathVariable("projectId") String projectId) {
        return ResponseEntity.ok(this.userService.getUsersByProjectId(projectId));
    }

}
