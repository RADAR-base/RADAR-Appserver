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
import org.radarbase.appserver.config.AuthConfig.AuthEntities
import org.radarbase.appserver.config.AuthConfig.AuthPermissions
import org.radarbase.appserver.dto.fcm.FcmUserDto
import org.radarbase.appserver.dto.fcm.FcmUsers
import org.radarbase.appserver.exception.InvalidUserDetailsException
import org.radarbase.appserver.service.UserService
import org.radarbase.auth.token.RadarToken
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import radar.spring.auth.common.AuthAspect
import radar.spring.auth.common.Authorization
import radar.spring.auth.common.Authorized
import radar.spring.auth.common.PermissionOn
import radar.spring.auth.exception.AuthorizationFailedException
import java.net.URI
import java.net.URISyntaxException

/**
 * Resource Endpoint for getting and adding users. Each notification [ ] needs to be associated to a user. A user may
 * represent a Management Portal subject.
 *
 * @see [Management Portal](https://github.com/RADAR-base/ManagementPortal)
 *
 * @author yatharthranjan
 */
@CrossOrigin
@RestController
class RadarUserController(
    private val userService: UserService,
    private val authorization: Authorization<RadarToken>?,
) {

    @Authorized(permission = AuthPermissions.UPDATE, entity = AuthEntities.SUBJECT)
    @PostMapping(
        (
            "/" +
                PathsUtil.PROJECT_PATH +
                "/" +
                PathsUtil.PROJECT_ID_CONSTANT +
                "/" +
                PathsUtil.USER_PATH
            ),
    )
    @Throws(URISyntaxException::class)
    fun addUserToProject(
        request: HttpServletRequest,
        @Valid @RequestBody userDto: FcmUserDto,
        @PathVariable projectId: String,
        @RequestParam(required = false, defaultValue = "false") forceFcmToken: Boolean,
    ): ResponseEntity<FcmUserDto> {
        userDto.projectId = projectId
        authorization?.let {
            val token = request.getAttribute(AuthAspect.TOKEN_KEY) as RadarToken
            if (it.hasPermission(
                    token,
                    AuthPermissions.UPDATE,
                    AuthEntities.SUBJECT,
                    PermissionOn.SUBJECT,
                    projectId,
                    userDto.subjectId,
                    null,
                )
            ) {
                if (forceFcmToken) userService.checkFcmTokenExistsAndReplace(userDto)
                val user = userService.saveUserInProject(userDto)
                return ResponseEntity.created(URI("${PathsUtil.USER_PATH}/user?id=${user.id}")).body(user)
            } else {
                throw AuthorizationFailedException("The provided token does not have enough privileges.")
            }
        } ?: run {
            if (forceFcmToken) userService.checkFcmTokenExistsAndReplace(userDto)
            val user = userService.saveUserInProject(userDto)
            return ResponseEntity.created(URI("${PathsUtil.USER_PATH}/user?id=${user.id}")).body(user)
        }
    }

    @Authorized(permission = AuthPermissions.UPDATE, entity = AuthEntities.SUBJECT, permissionOn = PermissionOn.SUBJECT)
    @PutMapping(
        (
            "/" +
                PathsUtil.PROJECT_PATH +
                "/" +
                PathsUtil.PROJECT_ID_CONSTANT +
                "/" +
                PathsUtil.USER_PATH +
                "/" +
                PathsUtil.SUBJECT_ID_CONSTANT
            ),
    )
    fun updateUserInProject(
        @Valid @RequestBody userDto: FcmUserDto,
        @PathVariable subjectId: String,
        @PathVariable projectId: String,
        @RequestParam(required = false, defaultValue = "false") forceFcmToken: Boolean,
    ): ResponseEntity<FcmUserDto> {
        userDto.subjectId = subjectId
        userDto.projectId = projectId
        if (forceFcmToken) userService.checkFcmTokenExistsAndReplace(userDto)
        val user = userService.updateUser(userDto)
        return ResponseEntity.ok(user)
    }

    @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.SUBJECT)
    @GetMapping("/" + PathsUtil.USER_PATH)
    fun getAllRadarUsers(request: HttpServletRequest): ResponseEntity<FcmUsers> {
        val users = userService.getAllRadarUsers()
        return authorization?.let {
            val token = request.getAttribute(AuthAspect.TOKEN_KEY) as RadarToken
            val filteredUsers = users.users.filter { user ->
                it.hasPermission(
                    token,
                    AuthPermissions.READ,
                    AuthEntities.SUBJECT,
                    PermissionOn.SUBJECT,
                    user.projectId,
                    user.subjectId,
                    null,
                )
            }
            ResponseEntity.ok(FcmUsers(filteredUsers))
        } ?: ResponseEntity.ok(users)
    }

    @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.SUBJECT)
    @GetMapping("/" + PathsUtil.USER_PATH + "/user")
    fun getRadarUserUsingId(
        request: HttpServletRequest,
        @RequestParam("id") id: Long?,
    ): ResponseEntity<FcmUserDto> {
        id ?: throw InvalidUserDetailsException("The given id must not be null!")
        val userDto = userService.getUserById(id)
        return getFcmUserDtoResponseEntity(request, userDto)
    }

    @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.SUBJECT)
    @GetMapping("/" + PathsUtil.USER_PATH + "/" + PathsUtil.SUBJECT_ID_CONSTANT)
    fun getRadarUserUsingSubjectId(
        request: HttpServletRequest,
        @PathVariable subjectId: String,
    ): ResponseEntity<FcmUserDto> {
        val userDto = userService.getUserBySubjectId(subjectId)
        return getFcmUserDtoResponseEntity(request, userDto)
    }

    private fun getFcmUserDtoResponseEntity(request: HttpServletRequest, userDto: FcmUserDto): ResponseEntity<FcmUserDto> {
        return authorization?.let {
            val token = request.getAttribute(AuthAspect.TOKEN_KEY) as RadarToken
            if (it.hasPermission(
                    token,
                    AuthPermissions.READ,
                    AuthEntities.SUBJECT,
                    PermissionOn.SUBJECT,
                    userDto.projectId,
                    userDto.subjectId,
                    null,
                )
            ) {
                ResponseEntity.ok(userDto)
            } else {
                throw AuthorizationFailedException("The provided token does not have enough privileges.")
            }
        } ?: ResponseEntity.ok(userDto)
    }

    @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.SUBJECT, permissionOn = PermissionOn.PROJECT)
    @GetMapping(
        (
            "/" +
                PathsUtil.PROJECT_PATH +
                "/" +
                PathsUtil.PROJECT_ID_CONSTANT +
                "/" +
                PathsUtil.USER_PATH
            ),
    )
    fun getUsersUsingProjectId(@PathVariable projectId: String): ResponseEntity<FcmUsers> {
        return ResponseEntity.ok(userService.getUsersByProjectId(projectId))
    }

    @Authorized(permission = AuthPermissions.READ, entity = AuthEntities.SUBJECT, permissionOn = PermissionOn.SUBJECT)
    @GetMapping(
        (
            "/" +
                PathsUtil.PROJECT_PATH +
                "/" +
                PathsUtil.PROJECT_ID_CONSTANT +
                "/" +
                PathsUtil.USER_PATH +
                "/" +
                PathsUtil.SUBJECT_ID_CONSTANT
            ),
    )
    fun getUsersUsingProjectIdAndSubjectId(
        @PathVariable projectId: String,
        @PathVariable subjectId: String,
    ): ResponseEntity<FcmUserDto> {
        return ResponseEntity.ok(userService.getUserByProjectIdAndSubjectId(projectId, subjectId))
    }

    @Authorized(permission = AuthPermissions.UPDATE, entity = AuthEntities.SUBJECT, permissionOn = PermissionOn.SUBJECT)
    @DeleteMapping(
        (
            "/" +
                PathsUtil.PROJECT_PATH +
                "/" +
                PathsUtil.PROJECT_ID_CONSTANT +
                "/" +
                PathsUtil.USER_PATH +
                "/" +
                PathsUtil.SUBJECT_ID_CONSTANT
            ),
    )
    fun deleteUserUsingProjectIdAndSubjectId(
        @PathVariable projectId: String,
        @PathVariable subjectId: String,
    ): ResponseEntity<Any> {
        userService.deleteUserByProjectIdAndSubjectId(projectId, subjectId)
        return ResponseEntity.ok().build()
    }
}
