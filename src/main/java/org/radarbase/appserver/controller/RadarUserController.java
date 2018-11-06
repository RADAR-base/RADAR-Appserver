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

import java.net.URI;
import java.net.URISyntaxException;

@RestController
public class RadarUserController {
    private static final Logger logger = LoggerFactory.getLogger(RadarUserController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private FcmNotificationService notificationService;

    @PostMapping("/users")
    public ResponseEntity addUser(@RequestParam(value = "projectId") String projectId,
                                       @RequestParam(value = "subjectId") String subjectId,
                                       @RequestParam(value = "sourceId") String sourceId)
            throws URISyntaxException {

        FcmUserDto user = this.userService.storeRadarUser(projectId, subjectId, sourceId);
        return ResponseEntity
                .created(new URI("/user/" + user.getId())).body(user);
    }

    @GetMapping("/users")
    public ResponseEntity<FcmUsers> getAllRadarUsers() {
        return null;
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<FcmUserDto> getRadarUserUsingId(
            @PathVariable String id) {
        return null;
    }

    @GetMapping("/users/{subjectid}")
    public ResponseEntity<FcmUserDto> getRadarUserUsingSubjectId(
            @PathVariable String subjectId) {
        return null;
    }

    @GetMapping("/users/{subjectid}/notifications")
    public ResponseEntity<FcmNotifications> getRadarNotificationsUsingSubjectId(
            @PathVariable String subjectId) {
        return ResponseEntity.ok(this.notificationService.getNotificationsBySubjectId(subjectId));
    }
}
