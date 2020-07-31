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

import static org.radarbase.appserver.config.AuthConfig.AuthEntities.MEASUREMENT;
import static org.radarbase.appserver.config.AuthConfig.AuthEntities.SUBJECT;
import static org.radarbase.appserver.config.AuthConfig.AuthPermissions.CREATE;
import static org.radarbase.appserver.config.AuthConfig.AuthPermissions.READ;
import static org.radarbase.appserver.config.AuthConfig.AuthPermissions.UPDATE;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.radarbase.appserver.config.AuthConfig.AuthEntities;
import org.radarbase.appserver.config.AuthConfig.AuthPermissions;
import org.radarbase.appserver.dto.fcm.FcmUserDto;
import org.radarbase.appserver.dto.fcm.FcmUsers;
import org.radarbase.appserver.service.UserService;
import org.radarcns.auth.token.RadarToken;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import radar.spring.auth.common.AuthAspect;
import radar.spring.auth.common.Authorization;
import radar.spring.auth.common.Authorized;
import radar.spring.auth.common.PermissionOn;
import radar.spring.auth.exception.AuthorizationFailedException;

/**
 * Resource Endpoint for getting and adding users. Each notification {@link
 * org.radarbase.appserver.entity.Notification} needs to be associated to a user. A user may
 * represent a Management Portal subject.
 *
 * @author yatharthranjan
 * @see <a href="https://github.com/RADAR-base/ManagementPortal">Management Portal</a>
 */
@RestController
public class RadarUserController {

    private transient UserService userService;
    private transient Authorization<RadarToken> authorization;

    public RadarUserController(UserService userService,
            Optional<Authorization<RadarToken>> authorization) {
        this.userService = userService;
        this.authorization = authorization.orElse(null);
    }

    @Authorized(permission = CREATE, entity = MEASUREMENT)
    @PostMapping("/projects/{projectId}/users")
    public ResponseEntity addUserToProject(HttpServletRequest request,
            @Valid @RequestBody FcmUserDto userDto, @Valid @PathVariable String projectId) throws
            URISyntaxException {

        userDto.setProjectId(projectId);

        if (authorization != null) {
            RadarToken token = (RadarToken) request.getAttribute(AuthAspect.TOKEN_KEY);
            if (authorization
                    .hasPermission(token, CREATE, MEASUREMENT, PermissionOn.SUBJECT, projectId,
                            userDto.getSubjectId(), null)) {
                FcmUserDto user = this.userService.saveUserInProject(userDto);
                return ResponseEntity
                        .created(new URI("/" + PathsUtil.USER_PATH + "/user?id=" + user.getId()))
                        .body(user);
            } else {
                throw new AuthorizationFailedException(
                        "The provided token does not have enough privileges.");
            }
        } else {
            FcmUserDto user = this.userService.saveUserInProject(userDto);
            return ResponseEntity
                    .created(new URI("/" + PathsUtil.USER_PATH + "/user?id=" + user.getId()))
                    .body(user);
        }
    }

    @Authorized(permission = UPDATE, entity = SUBJECT, permissionOn = PermissionOn.SUBJECT)
    @PutMapping("/projects/{projectId}/users/{subjectId}")
    public ResponseEntity updateUserInProject(@Valid @RequestBody FcmUserDto userDto,
            @Valid @PathVariable String subjectId, @Valid @PathVariable String projectId) {
        userDto.setSubjectId(subjectId);
        userDto.setProjectId(projectId);
        FcmUserDto user = this.userService.updateUser(userDto);
        return ResponseEntity.ok(user);
    }

    @Authorized(permission = READ, entity = SUBJECT)
    @GetMapping("/users")
    public ResponseEntity<FcmUsers> getAllRadarUsers(HttpServletRequest request) {
        FcmUsers users = this.userService.getAllRadarUsers();
        if (authorization != null) {
            // Filter the users based on access.
            FcmUsers usersFinal = new FcmUsers().setUsers(users.getUsers().stream()
                    .filter(user -> authorization
                            .hasPermission((RadarToken) request.getAttribute(AuthAspect.TOKEN_KEY),
                                    READ, SUBJECT, PermissionOn.SUBJECT, user.getProjectId(),
                                    user.getSubjectId(), null)).collect(Collectors.toList()));
            return ResponseEntity.ok(usersFinal);
        } else {
            return ResponseEntity.ok(users);
        }
    }

    @Authorized(permission = READ, entity = SUBJECT)
    @GetMapping("/users/{subjectId}")
    public ResponseEntity<FcmUserDto> getRadarUserUsingSubjectId(HttpServletRequest request,
            @PathVariable String subjectId) {
        FcmUserDto userDto = this.userService.getUserBySubjectId(subjectId);

        return getFcmUserDtoResponseEntity(request, userDto);
    }

    private ResponseEntity<FcmUserDto> getFcmUserDtoResponseEntity(HttpServletRequest request,
            FcmUserDto userDto) {
        if (authorization != null) {
            RadarToken token = (RadarToken) request.getAttribute(AuthAspect.TOKEN_KEY);
            if (authorization.hasPermission(token, AuthPermissions.READ, AuthEntities.SUBJECT,
                    PermissionOn.SUBJECT, userDto.getProjectId(), userDto.getSubjectId(), null)) {
                return ResponseEntity.ok(userDto);
            } else {
                throw new AuthorizationFailedException(
                        "The provided token does not have enough privileges.");
            }
        } else {
            return ResponseEntity.ok(userDto);
        }
    }

    @Authorized(permission = READ, entity = SUBJECT, permissionOn = PermissionOn.PROJECT)
    @GetMapping("/projects/{projectId}/users")
    public ResponseEntity<FcmUsers> getUsersUsingProjectId(@Valid @PathVariable String projectId) {
        return ResponseEntity.ok(this.userService.getUsersByProjectId(projectId));
    }

    @Authorized(permission = READ, entity = SUBJECT, permissionOn = PermissionOn.SUBJECT)
    @GetMapping("/projects/{projectId}/users/{subjectId}")
    public ResponseEntity<FcmUserDto> getUsersUsingProjectIdAndSubjectId(
            @Valid @PathVariable String projectId, @Valid @PathVariable String subjectId) {

        return ResponseEntity
                .ok(this.userService.getUsersByProjectIdAndSubjectId(projectId, subjectId));
    }
}
