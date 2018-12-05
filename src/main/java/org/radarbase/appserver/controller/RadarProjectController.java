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

import org.radarbase.appserver.entity.Project;
import org.radarbase.fcm.dto.FcmNotifications;
import org.radarbase.appserver.dto.RadarProjectDto;
import org.radarbase.appserver.dto.RadarProjects;
import org.radarbase.appserver.service.FcmNotificationService;
import org.radarbase.appserver.service.RadarProjectService;
import org.radarbase.appserver.service.UserService;
import org.radarbase.fcm.dto.FcmUsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.websocket.server.PathParam;

/**
 * @author yatharthranjan
 */
@RestController
public class RadarProjectController {

    @Autowired
    private RadarProjectService projectService;

    /**
     * Method for updating a project.
     * @param projectDto The project info to update
     * @return The updated Project DTO.
     * Throws {@link org.radarbase.appserver.exception.NotFoundException} if project was not found.
     */
    @PostMapping(value = "/" + Paths.PROJECT_PATH, consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<RadarProjectDto> addProject(@Valid @RequestBody RadarProjectDto projectDto) {
        return ResponseEntity.ok(this.projectService.addProject(projectDto));
    }

    /**
     * Method for updating a project.
     * @param projectDto The project info to update
     * @return The updated Project DTO.
     * Throws {@link org.radarbase.appserver.exception.NotFoundException} if project was not found.
     */
    @PutMapping(value = "/" + Paths.PROJECT_PATH, consumes = { MediaType.APPLICATION_JSON_VALUE })
    public ResponseEntity<RadarProjectDto> updateProject(@Valid @RequestBody RadarProjectDto projectDto) {
        return ResponseEntity.ok(this.projectService.updateProject(projectDto));
    }

    @GetMapping("/" + Paths.PROJECT_PATH)
    public ResponseEntity<RadarProjects> getAllProjects() {
        return ResponseEntity.ok(this.projectService.getAllProjects());
    }

    @GetMapping("/"+ Paths.PROJECT_PATH + "/project")
    public ResponseEntity<RadarProjectDto> getProjectsUsingId(@Valid @PathParam("id") Long id) {
        return ResponseEntity.ok(this.projectService.getProjectById(id));
    }

    @GetMapping("/" + Paths.PROJECT_PATH + "/{projectId}")
    public ResponseEntity<RadarProjectDto> getProjectsUsingProjectId(@Valid @PathVariable("projectId") String projectId) {
        return ResponseEntity.ok(this.projectService.getProjectByProjectId(projectId));
    }
}
