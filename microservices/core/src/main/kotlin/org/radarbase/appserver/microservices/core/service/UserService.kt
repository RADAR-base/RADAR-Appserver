/*
 * Copyright 2025 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.appserver.microservices.core.service

import org.radarbase.appserver.microservices.core.dto.fcm.FcmUserDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUsers
import java.time.Instant

/**
 * Contract for user related operations (FCM users).
 */
interface UserService {

    /** Retrieve all users across projects as DTO wrapper. */
    suspend fun getAllRadarUsers(): FcmUsers

    suspend fun findByFcmToken(fcmToken: String): FcmUserDto?

    /**
     * Retrieve a user by internal DB id.
     * @throws org.radarbase.jersey.exception.HttpNotFoundException if not found
     */
    suspend fun getUserById(id: Long): FcmUserDto

    /**
     * Retrieve a user by their subject id.
     * @throws org.radarbase.jersey.exception.HttpNotFoundException if not found
     */
    suspend fun getUserBySubjectId(subjectId: String): FcmUserDto

    /**
     * Retrieve all users for the given project id.
     * @throws org.radarbase.jersey.exception.HttpNotFoundException if project not found
     */
    suspend fun getUsersByProjectId(projectId: String): FcmUsers

    /**
     * Retrieve a user for the given (projectId, subjectId) pair.
     * @throws org.radarbase.jersey.exception.HttpNotFoundException if project or user not found
     */
    suspend fun getUserByProjectIdAndSubjectId(projectId: String, subjectId: String): FcmUserDto

    suspend fun checkFcmTokenExistsAndReplace(userDto: FcmUserDto)

    /**
     * Create (save) a new user within an existing project.
     * Generates any required schedules for the user.
     *
     * @throws org.radarbase.jersey.exception.HttpNotFoundException if the project is missing
     * @throws org.radarbase.appserver.microservices.core.exception.InvalidUserDetailsException if the user already exists
     */
    suspend fun saveUserInProject(userDto: FcmUserDto): FcmUserDto

    /**
     * Update an existing user. Regenerates the schedule if relevant fields change.
     *
     * @throws org.radarbase.jersey.exception.HttpNotFoundException if the project is missing
     * @throws org.radarbase.appserver.microservices.core.exception.InvalidUserDetailsException if the user does not exist
     */
    suspend fun updateUser(userDto: FcmUserDto): FcmUserDto

    /**
     * Update the lastDelivered timestamp for the user identified by the FCM token.
     *
     * @throws org.radarbase.jersey.exception.HttpNotFoundException if the user is not found
     */
    suspend fun updateLastDelivered(fcmToken: String, lastDelivered: Instant?)

    /**
     * Delete a user in the specified project by subjectId.
     *
     * @throws org.radarbase.jersey.exception.HttpNotFoundException if the project is missing
     * @throws org.radarbase.appserver.microservices.core.exception.InvalidUserDetailsException if the user does not exist
     */
    suspend fun deleteUserByProjectIdAndSubjectId(projectId: String, subjectId: String)
}
