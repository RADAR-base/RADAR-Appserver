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
import org.radarbase.appserver.dto.ProjectDto;
import org.radarbase.appserver.dto.ProjectDtos;
import org.radarbase.appserver.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * Resource Endpoint for getting and adding projects. Each user {@link
 * org.radarbase.appserver.entity.User} needs to be associated to a project. A project may represent
 * a Management Portal project.
 *
 * @see <a href="https://github.com/RADAR-base/ManagementPortal">Management Portal</a>
 * @author yatharthranjan
 */
@RestController
public class RadarProjectController {

  @Autowired private transient ProjectService projectService;

  /**
   * Method for updating a project.
   *
   * @param projectDto The project info to update
   * @return The updated Project DTO. Throws {@link
   *     org.radarbase.appserver.exception.NotFoundException} if project was not found.
   */
  @PreAuthorize(
      AuthContants.PERMISSION_ON_PROJECT_MEASUREMENT_CREATE
          + AuthContants.ACCESSOR
          + "projectDto.getProjectId()"
          + ")")
  @PostMapping(
      value = "/" + Paths.PROJECT_PATH,
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ProjectDto> addProject(@Valid @RequestBody ProjectDto projectDto)
      throws URISyntaxException {

    ProjectDto projectDtoNew = this.projectService.addProject(projectDto);
    return ResponseEntity.created(new URI("/projects/project?id=" + projectDtoNew.getId()))
        .body(projectDtoNew);
  }

  /**
   * Method for updating a project.
   *
   * @param projectDto The project info to update
   * @return The updated Project DTO. Throws {@link
   *     org.radarbase.appserver.exception.NotFoundException} if project was not found.
   */
  @PreAuthorize(
      AuthContants.PERMISSION_ON_PROJECT_MEASUREMENT_CREATE
          + AuthContants.ACCESSOR
          + "projectDto.getProjectId()"
          + ")")
  @PutMapping(
      value = "/" + Paths.PROJECT_PATH,
      consumes = {MediaType.APPLICATION_JSON_VALUE})
  public ResponseEntity<ProjectDto> updateProject(@Valid @RequestBody ProjectDto projectDto) {
    return ResponseEntity.ok(this.projectService.updateProject(projectDto));
  }

  @PreAuthorize("hasAuthority('ROLE_SYS_ADMIN') or hasRole('ADMIN')")
  @GetMapping("/" + Paths.PROJECT_PATH)
  public ResponseEntity<ProjectDtos> getAllProjects() {
    return ResponseEntity.ok(this.projectService.getAllProjects());
  }

  @PostAuthorize(
      AuthContants.PERMISSION_ON_PROJECT_MEASUREMENT_CREATE
          + "returnObject.body.getProjectId()"
          + ")")
  @GetMapping("/" + Paths.PROJECT_PATH + "/project")
  public ResponseEntity<ProjectDto> getProjectsUsingId(@Valid @PathParam("id") Long id) {
    return ResponseEntity.ok(this.projectService.getProjectById(id));
  }

  @PreAuthorize(
      AuthContants.PERMISSION_ON_PROJECT_MEASUREMENT_CREATE
          + AuthContants.ACCESSOR
          + AuthContants.PROJECT_ID
          + ")")
  @GetMapping("/" + Paths.PROJECT_PATH + "/" + Paths.PROJECT_ID_CONSTANT)
  public ResponseEntity<ProjectDto> getProjectsUsingProjectId(
      @Valid @PathVariable String projectId) {
    return ResponseEntity.ok(this.projectService.getProjectByProjectId(projectId));
  }
}
