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

package org.radarbase.appserver.jersey.service

import jakarta.inject.Inject
import jakarta.inject.Named
import org.radarbase.appserver.jersey.config.AppserverConfig
import org.radarbase.appserver.jersey.dto.fcm.FcmUserDto
import org.radarbase.appserver.jersey.dto.fcm.FcmUsers
import org.radarbase.appserver.jersey.enhancer.AppserverResourceEnhancer.Companion.USER_MAPPER
import org.radarbase.appserver.jersey.entity.Project
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.exception.InvalidUserDetailsException
import org.radarbase.appserver.jersey.mapper.Mapper
import org.radarbase.appserver.jersey.mapper.UserMapper
import org.radarbase.appserver.jersey.repository.ProjectRepository
import org.radarbase.appserver.jersey.repository.UserRepository
import org.radarbase.appserver.jersey.service.questionnaire_schedule.QuestionnaireScheduleService
import org.radarbase.appserver.jersey.utils.checkInvalidDetails
import org.radarbase.appserver.jersey.utils.checkPresence
import org.radarbase.jersey.exception.HttpNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant

@Suppress("unused")
class UserService @Inject constructor(
    @Named(USER_MAPPER) val userMapper: Mapper<FcmUserDto, User>,
    val userRepository: UserRepository,
    val projectRepository: ProjectRepository,
    val scheduleService: QuestionnaireScheduleService,
    config: AppserverConfig,
) {
    private val sendEmailNotifications: Boolean = config.email.enabled ?: false

    /**
     * Retrieves all users associated with the projects.
     *
     * @return a list of [FcmUsers].
     */
    suspend fun getAllRadarUsers(): FcmUsers {
        return FcmUsers(userMapper.entitiesToDtos(userRepository.findAll()))
    }

    /**
     * Retrieves a user by their id.
     *
     * @param id the unique identifier of the user to be retrieved
     * @return the user details as [FcmUserDto] if the user is found
     * @throws [HttpNotFoundException] if no user with the given id exists
     */
    suspend fun getUserById(id: Long): FcmUserDto {
        val user: User = checkPresence(userRepository.find(id), "user_not_found") {
            "User with id $id not found"
        }
        return userMapper.entityToDto(user)
    }

    /**
     * Retrieves a user by subject ID.
     * If a user with the specified subject ID cannot be found, a [HttpNotFoundException] is thrown.
     *
     * @param subjectId subject id of user.
     * @return A data transfer object ([FcmUserDto]) representing the user information.
     */
    suspend fun getUserBySubjectId(subjectId: String): FcmUserDto {
        val user =
            checkPresence(userRepository.findBySubjectId(subjectId), "user_not_found") {
                "User with subjectId $subjectId not found"
            }
        return userMapper.entityToDto(user)
    }

    /**
     * Retrieves all users associated with the specified project ID.
     *
     * @param projectId project id of a project whose users should be retrieved
     * @return [FcmUsers] that belong to the specified project
     * @throws [HttpNotFoundException] if the project with the given ID does not exist
     */
    suspend fun getUsersByProjectId(projectId: String): FcmUsers {
        val project: Project =
            checkPresence(projectRepository.findByProjectId(projectId), "project_not_found") {
                "Project with id $projectId not found"
            }

        val users: List<User> = userRepository.findByProjectId(
            requireNotNull(project.id) { "Project id for project ${project.projectId} is null when fetching users by projectId" },
        )

        return FcmUsers(userMapper.entitiesToDtos(users))
    }

    /**
     * Retrieves a user associated with a specific project and subject ID.
     *
     * This method first verifies the existence of the project identified by the given project ID.
     * It then retrieves the user associated with the provided subject ID and project ID. If either
     * the project or user is not found, an exception will be thrown.
     *
     * @param projectId The unique identifier of the project with which the user is associated.
     * @param subjectId The unique identifier of the subject (user) to be retrieved.
     * @return An instance of [FcmUserDto] representing the details of the user.
     * @throws HttpNotFoundException If the specified project or user is not found in the database.
     */
    suspend fun getUserByProjectIdAndSubjectId(projectId: String, subjectId: String): FcmUserDto {
        val project: Project =
            checkPresence(
                projectRepository.findByProjectId(projectId),
                "project_not_found",
            ) { "Project with id $projectId not found" }

        return checkPresence(
            userRepository.findBySubjectIdAndProjectId(
                subjectId,
                requireNotNull(project.id) { "Project id for project ${project.projectId} is null when fetching users by projectId" },
            ),
            "user_not_found",
        ) { "User with subjectId $subjectId not found" }.let { user ->
            userMapper.entityToDto(user)
        }
    }

    /**
     * Checks if a given FCM token exists in the database. If a user with the token exists but is associated
     * with a different subject ID, the token is replaced with a new value. The updated user is then saved.
     *
     * @param userDto The user data transfer object containing the FCM token and subject ID to be verified and updated.
     */
    suspend fun checkFcmTokenExistsAndReplace(userDto: FcmUserDto) {
        userDto.fcmToken?.also { fcmToken ->
            val user: User? = userRepository.findByFcmToken(fcmToken)
            user?.apply {
                if (!subjectId.equals(userDto.subjectId)) {
                    user.fcmToken = FCM_TOKEN_PREFIX + Instant.now().toString()
                }
            }?.also {
                userRepository.update(it)
            }
        }
    }

    /**
     * Saves a user to an existing project in the system. If the user already exists in the project,
     * an exception will be thrown. Additionally, generates a schedule for the newly created user.
     *
     * @param userDto The Data Transfer Object containing the details of the user to be saved,
     * including the associated project ID and other user-specific data.
     * @return Returns the saved user's Data Transfer Object with updated or assigned values after persistence.
     * @throws HttpNotFoundException If the specified project is not found in the system.
     * @throws InvalidUserDetailsException If a user with the same subject ID already exists in the specified project.
     */
    suspend fun saveUserInProject(userDto: FcmUserDto): FcmUserDto {
        // TODO: Future -- If any value is null get them using the MP api using others. (eg only subject
        // id, then get project id and source ids from MP)
        // TODO: Make the above pluggable so can use others or none.
        logger.debug("Saving user: {}", userDto)

        checkInvalidDetails<InvalidUserDetailsException>(
            { userDto.id != null },
            {
                "'id' must not be supplied when creating a project, it is autogenerated"
            },
        )

        val project: Project = checkPresence(
            projectRepository.findByProjectId(
                checkNotNull(userDto.projectId) { "Project ID must not be null" },
            ),
            "project_not_found",
        ) {
            "Project with id ${userDto.projectId} not found. Please create the project first."
        }

        val user: User? = userRepository.findBySubjectIdAndProjectId(
            requireNotNull(userDto.subjectId) { "Subject id must not be null" },
            requireNotNull(project.id) { "Project id must not be null" },
        )

        checkInvalidDetails<InvalidUserDetailsException>(
            { user != null },
            {
                "User with subjectId ${userDto.subjectId} already exists with projectId ${userDto.projectId}. " +
                    "Please use update endpoint if you need to update user"
            },
        )

        val email: String? = userDto.email
        if (sendEmailNotifications && email.isNullOrEmpty()) {
            logger.warn(
                "No email address was provided for new subject '{}'. The option to send notifications via email " +
                    "('email.enabled') will not work for this subject. Consider to provide a valid email " +
                    "address for subject",
                userDto.subjectId,
            )
        }

        val savedUser: User = userMapper.dtoToEntity(userDto).also { newUser ->
            newUser.usermetrics?.let {
                // maintain a bidirectional relationship
                it.user = newUser
            }
            newUser.project = project
        }.run {
            userRepository.add(this)
        }

        this.scheduleService.generateScheduleForUser(savedUser)

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
     * @throws HttpNotFoundException If the project associated with the given projectID does not exist.
     * @throws InvalidUserDetailsException If the user with the specified subject ID does not exist within the project.
     */
    suspend fun updateUser(userDto: FcmUserDto): FcmUserDto {
        val project: Project = checkPresence(
            projectRepository.findByProjectId(
                checkNotNull(userDto.projectId) { "Project ID must not be null" },
            ),
            "project_not_found",
        ) {
            "Project with id ${userDto.projectId} not found. Please create the project first."
        }

        val user: User? = userRepository.findBySubjectIdAndProjectId(
            requireNotNull(userDto.subjectId) { "Subject id must be non-null" },
            requireNotNull(project.id) { "Project `id` must be non-null" },
        )

        checkInvalidDetails<InvalidUserDetailsException>(
            user == null,
        ) {
            "The user with specified subject ID ${userDto.subjectId} does not exist in project ID "
            "${userDto.projectId} Please use post endpoint to create the user."
        }

        user.apply {
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

        val savedUser: User = userRepository.update(user) ?: throw HttpNotFoundException(
            "user_not_found",
            "User with id ${user.id} not found.",
        )
        // Generate schedule for user
        if (user.attributes != userDto.attributes || user.timezone != userDto.timezone || user.enrolmentDate != userDto.enrolmentDate || user.language != userDto.language) {
            this.scheduleService.generateScheduleForUser(savedUser)
        }

        return userMapper.entityToDto(savedUser)
    }

    suspend fun updateLastDelivered(fcmToken: String, lastDelivered: Instant?) {
        val user: User = checkPresence(
            userRepository.findByFcmToken(fcmToken),
            "user_not_found",
        ) {
            "User with the fcm-token $fcmToken doesn't exists"
        }
        user.usermetrics?.let {
            it.lastDelivered = lastDelivered
        }
        userRepository.update(user)
    }

    /**
     * Deletes a user associated with a specific project and subject ID.
     * This method verifies the existence of the project and the user in the
     * specified project before deletion.
     *
     * @param projectId The unique identifier of the project.
     * @param subjectId The unique identifier of the user (subject) within the project.
     * @throws HttpNotFoundException If the project with the specified projectId doesn't exist.
     * @throws InvalidUserDetailsException If the user with the specified subjectId does not exist in the project.
     */
    suspend fun deleteUserByProjectIdAndSubjectId(projectId: String, subjectId: String) {
        val project: Project = checkPresence(projectRepository.findByProjectId(projectId), "project_not_found") {
            "Project with id $projectId not found"
        }

        val user = userRepository.findBySubjectIdAndProjectId(
            subjectId,
            requireNotNull(project.id) { "Project id for project ${project.projectId} is null when fetching users by projectId" },
        )

        checkInvalidDetails<InvalidUserDetailsException>(
            user == null,
        ) {
            "The user with specified subject ID $subjectId does not exist in project ID $projectId. Please specify a valid user for deleting."
        }

        this.userRepository.delete(user)
    }

    companion object {
        private const val FCM_TOKEN_PREFIX = "unregistered_"

        private val logger: Logger = LoggerFactory.getLogger(UserService::class.java)
    }
}
