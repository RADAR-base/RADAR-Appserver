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

package org.radarbase.appserver.microservices.user.service

import jakarta.inject.Inject
import jakarta.inject.Named
import jakarta.ws.rs.core.Response
import kotlinx.serialization.json.Json
import org.radarbase.appserver.microservices.contract.calls.ProjectServiceContract
import org.radarbase.appserver.microservices.contract.calls.QuestionnaireScheduleContract
import org.radarbase.appserver.microservices.contract.exception.ProxyResponseException
import org.radarbase.appserver.microservices.contract.utils.Utils.deserializeDtoFromContract
import org.radarbase.appserver.microservices.core.dto.ProjectDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUserDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUsers
import org.radarbase.appserver.microservices.core.entity.Project
import org.radarbase.appserver.microservices.core.entity.User
import org.radarbase.appserver.microservices.core.exception.InvalidUserDetailsException
import org.radarbase.appserver.microservices.core.mapper.Mapper
import org.radarbase.appserver.microservices.core.mapper.UserMapper
import org.radarbase.appserver.microservices.core.repository.UserRepository
import org.radarbase.appserver.microservices.core.service.UserService
import org.radarbase.appserver.microservices.core.utils.Const.PROJECT_MAPPER
import org.radarbase.appserver.microservices.core.utils.Const.USER_MAPPER
import org.radarbase.appserver.microservices.core.utils.checkInvalidDetails
import org.radarbase.appserver.microservices.core.utils.checkPresence
import org.radarbase.appserver.microservices.user.config.UserServiceConfig
import org.radarbase.jersey.exception.HttpNotFoundException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant

@Suppress("unused")
class UserServiceImpl @Inject constructor(
    @param:Named(USER_MAPPER) val userMapper: Mapper<FcmUserDto, User>,
    @param:Named(PROJECT_MAPPER) val projectMapper: Mapper<ProjectDto, Project>,
    val userRepository: UserRepository,
    config: UserServiceConfig,
) : UserService {
    private val sendEmailNotifications: Boolean = config.email.enabled
    private val projectServiceUrl = config.contract.project
    private val taskServiceUrl = config.contract.task

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

    /**
     * Retrieves all users associated with the projects.
     *
     * @return a list of [FcmUsers].
     */
    override suspend fun getAllRadarUsers(): FcmUsers {
        return FcmUsers(userMapper.entitiesToDtos(userRepository.findAll()))
    }

    override suspend fun findByFcmToken(fcmToken: String): FcmUserDto {
        return userMapper.entityToDto(
            userRepository.findByFcmToken(fcmToken) ?: throw InvalidUserDetailsException("The user with the given Fcm Token does not exist")
        )
    }

    /**
     * Retrieves a user by their id.
     *
     * @param id the unique identifier of the user to be retrieved
     * @return the user details as [FcmUserDto] if the user is found
     * @throws [HttpNotFoundException] if no user with the given id exists
     */
    override suspend fun getUserById(id: Long): FcmUserDto {
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
    override suspend fun getUserBySubjectId(subjectId: String): FcmUserDto {
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
    override suspend fun getUsersByProjectId(projectId: String): FcmUsers {
        val project: ProjectDto = ProjectServiceContract.getProjectUsingProjectId(projectId, projectServiceUrl).let {
            deserializeDtoFromContract<ProjectDto>(
                it,
            ) {
                "project_not_found ; Project with id $projectId not found"
            }
        }

        val users: List<User> = userRepository.findByProjectId(
            requireNotNull(project.projectId) { "Project id for project is null when fetching users by projectId" },
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
    override suspend fun getUserByProjectIdAndSubjectId(projectId: String, subjectId: String): FcmUserDto {
        val project: ProjectDto = ProjectServiceContract.getProjectUsingProjectId(projectId, projectServiceUrl).let {
            deserializeDtoFromContract<ProjectDto>(
                it,
            ) {
                "project_not_found ; Project with id $projectId not found"
            }
        }

        return checkPresence(
            userRepository.findBySubjectIdAndProjectId(
                subjectId,
                requireNotNull(project.projectId) { "Project id for project is null when fetching users by projectId" },
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
    override suspend fun checkFcmTokenExistsAndReplace(userDto: FcmUserDto) {
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
    override suspend fun saveUserInProject(userDto: FcmUserDto): FcmUserDto {
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

        val project: Project = ProjectServiceContract.getProjectUsingProjectId(
            checkNotNull(userDto.projectId) { "Project id must be not null" },
            projectServiceUrl,
        ).let {
            deserializeDtoFromContract<ProjectDto>(
                it,
            ) {
                "project_not_found ; Project with id ${userDto.projectId} not found. Please create a project first"
            }
        }.let {
            projectMapper.dtoToEntity(it)
        }


        val user: User? = userRepository.findBySubjectIdAndProjectId(
            requireNotNull(userDto.subjectId) { "Subject id must not be null" },
            requireNotNull(project.projectId) { "Project id must not be null" },
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
            newUser.projectId = project.projectId
        }.run {
            userRepository.add(this)
        }
        generateScheduleUsingProjectIdAndSubjectId(savedUser.projectId, savedUser.subjectId)

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
    override suspend fun updateUser(userDto: FcmUserDto): FcmUserDto {
        val project: ProjectDto = ProjectServiceContract.getProjectUsingProjectId(
            requireNotNull(userDto.projectId) { "Project ID must be not null" },
            projectServiceUrl,
        ).let {
            deserializeDtoFromContract<ProjectDto>(
                it,
            ) {
                "project_not_found ; Project with id ${userDto.projectId} not found. Please create a project first"
            }
        }

        val user: User? = userRepository.findBySubjectIdAndProjectId(
            requireNotNull(userDto.subjectId) { "Subject id must be non-null" },
            requireNotNull(project.projectId) { "Project `id` must be non-null" },
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

        val updatedUser: User = userRepository.update(user) ?: throw HttpNotFoundException(
            "user_not_found",
            "User with id ${user.id} not found.",
        )
        // Generate schedule for user
        if (user.attributes != userDto.attributes ||
            user.timezone !=
            userDto.timezone ||
            user.enrolmentDate?.equals(userDto.enrolmentDate) != true  ||
            user.language != userDto.language) {
            generateScheduleUsingProjectIdAndSubjectId(updatedUser.projectId, updatedUser.subjectId)
        }

        return userMapper.entityToDto(updatedUser)
    }

    override suspend fun updateLastDelivered(fcmToken: String, lastDelivered: Instant?) {
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
    override suspend fun deleteUserByProjectIdAndSubjectId(projectId: String, subjectId: String) {
        val project: ProjectDto = ProjectServiceContract.getProjectUsingProjectId(
            projectId,
            baseUrl = projectServiceUrl,
        ).let {
            deserializeDtoFromContract<ProjectDto>(
                it,
            ) { "project_not_found ; Project with id $projectId not found." }
        }

        val user = userRepository.findBySubjectIdAndProjectId(
            subjectId,
            requireNotNull(project.projectId) { "Project id for project is null when fetching users by projectId" },
        )

        checkInvalidDetails<InvalidUserDetailsException>(
            user == null,
        ) {
            "The user with specified subject ID $subjectId does not exist in project ID $projectId. Please specify a valid user for deleting."
        }

        this.userRepository.delete(user)
    }

    private suspend fun generateScheduleUsingProjectIdAndSubjectId(projectId: String?, subjectId: String?) {
        return QuestionnaireScheduleContract.generateScheduleUsingProjectIdAndSubjectId(
            requireNotNull(projectId) { "User's Project id must not be null" },
            requireNotNull(subjectId) { "Subject id must not be null" },
            taskServiceUrl
        ).let {
            if (it.status !in 200 .. 299) {
                throw ProxyResponseException(
                    Response.Status.fromStatusCode(it.status),
                    it.body?.decodeToString() ?: "Upstream sent an incorrect response",
                )
            }
        }
    }

    companion object {
        private const val FCM_TOKEN_PREFIX = "unregistered_"

        private val logger: Logger = LoggerFactory.getLogger(UserServiceImpl::class.java)
    }
}
