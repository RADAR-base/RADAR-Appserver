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
import static org.radarbase.appserver.config.AuthConfig.AuthEntities.PROJECT;
import static org.radarbase.appserver.config.AuthConfig.AuthPermissions.CREATE;
import static org.radarbase.appserver.config.AuthConfig.AuthPermissions.READ;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.websocket.server.PathParam;

import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.config.AuthConfig.AuthEntities;
import org.radarbase.appserver.config.AuthConfig.AuthPermissions;
import org.radarbase.appserver.dto.ProjectDto;
import org.radarbase.appserver.dto.ProjectDtos;
import org.radarbase.appserver.service.ProjectService;
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
 * Resource Endpoint for getting and adding projects. Each user {@link
 * org.radarbase.appserver.entity.User} needs to be associated to a project. A project may represent
 * a Management Portal project.
 *
 * @author yatharthranjan
 * @see <a href="https://github.com/RADAR-base/ManagementPortal">Management Portal</a>
 */
@RestController
@Slf4j
public class RadarProjectController {

    private transient ProjectService projectService;
    private transient Authorization<RadarToken> authorization;

    public RadarProjectController(ProjectService projectService,
            Optional<Authorization<RadarToken>> authorization) {
        this.projectService = projectService;
        this.authorization = authorization.orElse(null);
    }

    /**
     * Method for creating a project.
     *
     * @param projectDto The project info to update
     * @return The updated Project DTO. Throws {@link
     * AuthorizationFailedException} if user does not have authority to create a project.
     */
    @Authorized(permission = CREATE, entity = MEASUREMENT)
    @PostMapping("/projects")
    public ResponseEntity<ProjectDto> addProject(HttpServletRequest request,
            @Valid @RequestBody ProjectDto projectDto) throws URISyntaxException {

        if (authorization != null) {
            RadarToken token = (RadarToken) request.getAttribute(AuthAspect.TOKEN_KEY);
            if (authorization.hasPermission(token, "CREATE", "MEASUREMENT", PermissionOn.PROJECT,
                    projectDto.getProjectId(), null, null)) {
                ProjectDto projectDtoNew = this.projectService.addProject(projectDto);
                return ResponseEntity
                        .created(new URI("/projects/project?id=" + projectDtoNew.getId()))
                        .body(projectDtoNew);
            } else {
                throw new AuthorizationFailedException(
                        "The token does not have permission for the project " + projectDto
                                .getProjectId());
            }
        } else {
            ProjectDto projectDtoNew = this.projectService.addProject(projectDto);
            return ResponseEntity.created(new URI("/projects/project?id=" + projectDtoNew.getId()))
                    .body(projectDtoNew);
        }
    }

    /**
     * Method for updating a project.
     *
     * @param projectDto The project info to update
     * @return The updated Project DTO. Throws {@link
     * org.radarbase.appserver.exception.NotFoundException} if project was not found.
     */
    @Authorized(permission = CREATE, entity = MEASUREMENT, permissionOn = PermissionOn.PROJECT)
    @PutMapping("/projects/{projectId}")
    public ResponseEntity<ProjectDto> updateProject(@Valid @PathParam("projectId") String projectId,
            @Valid @RequestBody ProjectDto projectDto) {
        ProjectDto projectDto1 = this.projectService.updateProject(projectDto);
        return ResponseEntity.ok(projectDto1);
    }

    @Authorized(permission = READ, entity = PROJECT)
    @GetMapping("/projects")
    public ResponseEntity<ProjectDtos> getAllProjects(HttpServletRequest request) {

        ProjectDtos projectDtos = this.projectService.getAllProjects();
        if (authorization != null) {
            ProjectDtos finalProjectDtos = new ProjectDtos().setProjects(
                    projectDtos.getProjects().stream().filter(project -> authorization
                            .hasPermission((RadarToken) request.getAttribute(AuthAspect.TOKEN_KEY),
                                    AuthPermissions.READ, AuthEntities.PROJECT,
                                    PermissionOn.PROJECT, project.getProjectId(), null, null))
                            .collect(Collectors.toList()));
            return ResponseEntity.ok(finalProjectDtos);
        } else {
            return ResponseEntity.ok(projectDtos);
        }
    }

    @Authorized(permission = CREATE, entity = MEASUREMENT, permissionOn = PermissionOn.PROJECT)
    @GetMapping("/projects/{projectId}")
    public ResponseEntity<ProjectDto> getProjectsUsingProjectId(
            @Valid @PathVariable String projectId) {
        return ResponseEntity.ok(this.projectService.getProjectByProjectId(projectId));
    }
}
