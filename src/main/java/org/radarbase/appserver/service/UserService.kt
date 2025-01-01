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

package org.radarbase.appserver.service

import org.radarbase.appserver.dto.fcm.FcmUserDto
import org.radarbase.appserver.dto.fcm.FcmUsers
import org.radarbase.appserver.entity.Project
import org.radarbase.appserver.entity.User
import org.radarbase.appserver.exception.InvalidUserDetailsException
import org.radarbase.appserver.exception.NotFoundException
import org.radarbase.appserver.mapper.UserMapper
import org.radarbase.appserver.repository.ProjectRepository
import org.radarbase.appserver.repository.UserRepository
import org.radarbase.appserver.util.checkInvalidDetails
import org.radarbase.appserver.util.checkPresence
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * [Service] for interacting with the  [User] [jakarta.persistence.Entity] using the
 * [UserRepository].
 *
 * @property userMapper Mapper instance used to convert between User entities and FCM User DTOs.
 * @property userRepository Repository interface for user persistence and retrieval operations.
 * @property projectRepository Repository interface for project persistence and lookup.
 * @property scheduleService Service to manage and schedule questionnaires for users.
 */
@Service
@Transactional
@Suppress("unused")
class UserService(
    val userMapper: UserMapper,
    val userRepository: UserRepository,
    val projectRepository: ProjectRepository,
    val scheduleService: QuestionnaireScheduleService,
) {

    /**
     * Indicates whether email notifications are enabled for the application.
     * This variable is configured via the `radar.notification.email.enabled`
     * property and defaults to `false` if no value is explicitly set.
     */
    @Transient
    @Value("\${radar.notification.email.enabled:false}")
    var sendEmailNotifications: Boolean = false

    /**
     * Retrieves all users associated with the projects.
     *
     * @return a list of [FcmUsers].
     */
    @Transactional(readOnly = true)
    fun getAllRadarUsers(): FcmUsers {
        return FcmUsers(userMapper.entitiesToDtos(userRepository.findAll()))
    }

    /**
     * Retrieves a user by their id.
     *
     * @param id the unique identifier of the user to be retrieved
     * @return the user details as [FcmUserDto] if the user is found
     * @throws NotFoundException if no user with the given id exists
     */
    @Transactional(readOnly = true)
    fun getUserById(id: Long): FcmUserDto {
        val user: User = checkPresence(userRepository.findByIdOrNull(id)) { "User with id $id not found" }
        return userMapper.entityToDto(user)
    }

    /**
     * Retrieves a user by subject ID.
     * If a user with the specified subject ID cannot be found, a [NotFoundException] is thrown.
     *
     * @param subjectId subject id of user.
     * @return A data transfer object ([FcmUserDto]) representing the user information.
     */
    @Transactional(readOnly = true)
    fun getUserBySubjectId(subjectId: String): FcmUserDto {
        val user =
            checkPresence(userRepository.findBySubjectId(subjectId)) { "User with subjectId $subjectId not found" }
        return userMapper.entityToDto(user)
    }

    /**
     * Retrieves all users associated with the specified project ID.
     *
     * @param projectId project id of a project whose users should be retrieved
     * @return [FcmUsers] that belong to the specified project
     * @throws NotFoundException if the project with the given ID does not exist
     */
    @Transactional(readOnly = true)
    fun getUsersByProjectId(projectId: String): FcmUsers {
        val project: Project =
            checkPresence(projectRepository.findByProjectId(projectId)) { "Project with id $projectId not found" }

        val pId: Long = requireNotNull(project.id) { "Project id must not be null" }

        val users: List<User> = userRepository.findByProjectId(pId)

        return FcmUsers(userMapper.entitiesToDtos(users))
    }

    /**
     * Retrieves a user associated with a specific project and subject ID.
     *
     * This method first verifies the existence of the project identified by the given project ID.
     * It then retrieves the user associated with the provided subject ID and project ID. If either
     * the project or user is not found, an exception will be thrown.
     *
     * @param projectId The unique identifier of the project to which the user is associated.
     * @param subjectId The unique identifier of the subject (user) to be retrieved.
     * @return An instance of [FcmUserDto] representing the details of the user.
     * @throws NotFoundException If the specified project or user is not found in the database.
     */
    @Transactional(readOnly = true)
    fun getUserByProjectIdAndSubjectId(projectId: String, subjectId: String): FcmUserDto {
        val project: Project =
            checkPresence(projectRepository.findByProjectId(projectId)) { "Project with id $projectId not found" }

        val pId: Long = requireNotNull(project.id) { "Project id must not be null" }

        val user: User = checkPresence(
            userRepository.findBySubjectIdAndProjectId(
                subjectId, pId
            )
        ) { "User with subjectId $subjectId not found" }

        return userMapper.entityToDto(user)
    }

    /**
     * Checks if a given FCM token exists in the database. If a user with the token exists but is associated
     * with a different subject ID, the token is replaced with a new value. The updated user is then saved.
     *
     * @param userDto The user data transfer object containing the FCM token and subject ID to be verified and updated.
     */
    fun checkFcmTokenExistsAndReplace(userDto: FcmUserDto) {
        val user: User? = userRepository.findByFcmToken(userDto.fcmToken)
        user?.apply {
            if (!subjectId.equals(userDto.subjectId)) {
                user.fcmToken = FCM_TOKEN_PREFIX + Instant.now().toString()
            }
        }?.also {
            userRepository.save<User>(it)
        }
    }

    /**
     * Saves a user to an existing project in the system. If the user already exists in the project,
     * an exception will be thrown. Additionally, generates a schedule for the newly created user.
     *
     * @param userDto The Data Transfer Object containing the details of the user to be saved,
     * including the associated project ID and other user-specific data.
     * @return Returns the saved user's Data Transfer Object with updated or assigned values after persistence.
     * @throws NotFoundException If the specified project is not found in the system.
     * @throws InvalidUserDetailsException If a user with the same subject ID already exists in the specified project.
     */
    fun saveUserInProject(userDto: FcmUserDto): FcmUserDto {
        // TODO: Future -- If any value is null get them using the MP api using others. (eg only subject
        // id, then get project id and source ids from MP)
        // TODO: Make the above pluggable so can use others or none.
        logger.debug("Saving user: {}", userDto)

        val project: Project = checkPresence(projectRepository.findByProjectId(userDto.projectId)) {
            "Project with id ${userDto.projectId} not found. Please create the project first."
        }

        val user: User? = userRepository.findBySubjectIdAndProjectId(
            userDto.subjectId, requireNotNull(project.id) { "Project id must not be null" })

        checkInvalidDetails<InvalidUserDetailsException>(
            { user != null },
            {
                "User with subjectId ${userDto.subjectId} already exists with projectId ${userDto.projectId}. " +
                        "Please use update endpoint if you need to update user"
            })

        val email: String? = userDto.email
        if (sendEmailNotifications && (email == null || email.isEmpty())) {
            logger.warn(
                "No email address was provided for new subject '{}'. The option to send notifications via email " +
                        "('radar.notification.email.enabled') will not work for this subject. Consider to provide a valid email " +
                        "address for subject", userDto.subjectId
            )
        }

        val savedUser: User = userMapper.dtoToEntity(userDto).also { newUser ->
            newUser.usermetrics?.let {
                // maintain a bidirectional relationship
                it.user = newUser
            }
        }.run {
            userRepository.save<User>(this)
        }

        this.scheduleService.generateScheduleForUser(user)

        return userMapper.entityToDto(savedUser)
    }

    /**
     * Updates an existing user within a specified project. The method ensures the project exists
     * and validates that the user is already associated with the project, updating their details
     * and regenerating their schedule if relevant changes are detected.
     *
     * @param userDto The data transfer object containing updated user information
     *                (e.g., FCM token, metrics, enrolment date, timezone, language, attributes).
     * @return The updated user data transfer object reflecting the saved changes.
     * @throws NotFoundException If the project associated with the given project ID does not exist.
     * @throws InvalidUserDetailsException If the user with the specified subject ID does not exist within the project.
     */
    fun updateUser(userDto: FcmUserDto): FcmUserDto {
//        TODO update to use Id instead of subjectId
        val project: Project = checkPresence(projectRepository.findByProjectId(userDto.projectId)) {
            "Project with id ${userDto.projectId} not found. Please create the project first."
        }

        val user: User? = userRepository.findBySubjectIdAndProjectId(
            userDto.subjectId,
            requireNotNull(project.id) { "Project id must not be null" })

        checkInvalidDetails<InvalidUserDetailsException>(
            { user == null },
            {
                "The user with specified subject ID ${userDto.subjectId} does not exist in project ID "
                "${userDto.projectId} Please use CreateUser endpoint to create the user."
            }
        )

        user!!.apply {
            this.fcmToken = userDto.fcmToken
            this.usermetrics = UserMapper.getValidUserMetrics(userDto)
            this.enrolmentDate = userDto.enrolmentDate
            this.timezone = userDto.timezone
            this.language = userDto.language
            this.attributes = userDto.attributes
            // maintain a bidirectional relationship
            this.usermetrics?.let {
                it.user = this@apply
            }
        }

        val savedUser: User = userRepository.save<User>(user)
        // Generate schedule for user
        if (user.attributes != userDto.attributes ||
            user.timezone != userDto.timezone ||
            user.enrolmentDate != userDto.enrolmentDate ||
            user.language != userDto.language
        ) {
            this.scheduleService.generateScheduleForUser(savedUser)
        }

        return userMapper.entityToDto(user)
    }

    /**
     * Deletes a user associated with a specific project and subject ID.
     * This method verifies the existence of the project and the user in the
     * specified project before deletion.
     *
     * @param projectId The unique identifier of the project.
     * @param subjectId The unique identifier of the user (subject) within the project.
     * @throws NotFoundException If the project with the specified projectId doesn't exist.
     * @throws InvalidUserDetailsException If the user with the specified subjectId does not exist in the project.
     */
    fun deleteUserByProjectIdAndSubjectId(projectId: String, subjectId: String) {
        val project: Project = checkPresence(projectRepository.findByProjectId(projectId)) {
            "Project with id $projectId not found"
        }

        val user = userRepository.findBySubjectIdAndProjectId(
            subjectId,
            requireNotNull(project.id) { "Project id must not be null" })

        checkInvalidDetails<InvalidUserDetailsException>(
            { user == null },
            {
                "The user with specified subject ID $subjectId does not exist in project ID "
                "$projectId. Please specify a valid user for deleting."
            }
        )

        this.userRepository.deleteById(user!!.id!!)
    }

    companion object {
        private const val FCM_TOKEN_PREFIX = "unregistered_"

        private val logger: Logger = LoggerFactory.getLogger(UserService::class.java)
    }
}