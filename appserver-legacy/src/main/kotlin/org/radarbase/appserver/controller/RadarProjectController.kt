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
package org.radarbase.appserver.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import jakarta.websocket.server.PathParam
import lombok.extern.slf4j.Slf4j
import org.radarbase.appserver.config.AuthConfig.AuthEntities
import org.radarbase.appserver.config.AuthConfig.AuthPermissions
import org.radarbase.appserver.dto.ProjectDto
import org.radarbase.appserver.dto.ProjectDtos
import org.radarbase.appserver.service.ProjectService
import org.radarbase.auth.token.RadarToken
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import radar.spring.auth.common.AuthAspect
import radar.spring.auth.common.Authorization
import radar.spring.auth.common.Authorized
import radar.spring.auth.common.PermissionOn
import radar.spring.auth.exception.AuthorizationFailedException
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.util.*
import java.util.stream.Collectors

/**
 * Resource Endpoint for getting and adding projects. Each user [ ] needs to be associated to a project. A project may represent
 * a Management Portal project.
 *
 * @see [Management Portal](https://github.com/RADAR-base/ManagementPortal)
 *
 * @author yatharthranjan
 */
@CrossOrigin
@RestController
class RadarProjectController(
    private val projectService: ProjectService,
    private val authorization: Authorization<RadarToken>?
) {
    /**
     * Method for updating a project.
     *
     * @param projectDto The project info to update
     * @return The updated Project DTO. Throws [     ] if project was not found.
     */
    @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.SUBJECT)
    @PostMapping(
        value = ["/${PathsUtil.PROJECT_PATH}"],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun addProject(
        request: HttpServletRequest,
        @Valid @RequestBody projectDto: ProjectDto
    ): ResponseEntity<ProjectDto> {
        authorization?.let {
            val token = request.getAttribute(AuthAspect.TOKEN_KEY) as RadarToken
            if (it.hasPermission(
                    token,
                    "READ",
                    "SUBJECT",
                    PermissionOn.PROJECT,
                    projectDto.projectId,
                    null,
                    null
                )
            ) {
                val projectDtoNew = projectService.addProject(projectDto)
                return ResponseEntity.created(URI("/projects/project?id=${projectDtoNew.id}"))
                    .body(projectDtoNew)
            } else {
                throw AuthorizationFailedException("The token does not have permission for the project ${projectDto.projectId}")
            }
        } ?: run {
            val projectDtoNew = projectService.addProject(projectDto)
            return ResponseEntity.created(URI("/projects/project?id=${projectDtoNew.id}"))
                .body(projectDtoNew)
        }
    }

    /**
     * Method for updating a project.
     *
     * @param projectDto The project info to update
     * @return The updated Project DTO. Throws [     ] if project was not found.
     */
    @Authorized(
        permission = AuthPermissions.UPDATE,
        entity = AuthEntities.SUBJECT,
        permissionOn = PermissionOn.PROJECT
    )
    @PutMapping(
        value = ["/" + PathsUtil.PROJECT_PATH + "/" + PathsUtil.PROJECT_ID_CONSTANT],
        consumes = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun updateProject(
        @PathVariable("projectId") projectId: String,
        @Valid @RequestBody projectDto: ProjectDto
    ): ResponseEntity<ProjectDto> {
        val updatedProject = projectService.updateProject(projectDto)
        return ResponseEntity.ok(updatedProject)
    }

    @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.PROJECT)
    @GetMapping("/" + PathsUtil.PROJECT_PATH)
    fun getAllProjects(request: HttpServletRequest): ResponseEntity<ProjectDtos> {
        val allProjects = projectService.getAllProjects()
        return authorization?.let {
            val filteredProjects = allProjects.projects.filter { project ->
                it.hasPermission(
                    request.getAttribute(AuthAspect.TOKEN_KEY) as RadarToken,
                    AuthPermissions.READ,
                    AuthEntities.PROJECT,
                    PermissionOn.PROJECT,
                    project.projectId,
                    null,
                    null
                )
            }
            ResponseEntity.ok(ProjectDtos().withProjects(filteredProjects))
        } ?: ResponseEntity.ok(allProjects)
    }


    // TODO think about plain authorized
    @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.PROJECT)
    @GetMapping("/" + PathsUtil.PROJECT_PATH + "/project")
    fun getProjectsUsingId(
        request: HttpServletRequest,
        @RequestParam("id") id: Long
    ): ResponseEntity<ProjectDto> {
        val projectDto = projectService.getProjectById(id)
        return authorization?.let {
            val token = request.getAttribute(AuthAspect.TOKEN_KEY) as RadarToken
            if (it.hasPermission(
                    token,
                    AuthPermissions.READ,
                    AuthEntities.PROJECT,
                    PermissionOn.PROJECT,
                    projectDto.projectId,
                    null,
                    null
                )
            ) {
                ResponseEntity.ok(projectDto)
            } else {
                throw AuthorizationFailedException("The token does not have permission for the project ${projectDto.projectId}")
            }
        } ?: ResponseEntity.ok(projectDto)
    }


    @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.SUBJECT, permissionOn = PermissionOn.PROJECT)
    @GetMapping("/" + PathsUtil.PROJECT_PATH + "/" + PathsUtil.PROJECT_ID_CONSTANT)
    fun getProjectsUsingProjectId(@PathVariable projectId: String): ResponseEntity<ProjectDto> {
        return ResponseEntity.ok(projectService.getProjectByProjectId(projectId))
    }

}