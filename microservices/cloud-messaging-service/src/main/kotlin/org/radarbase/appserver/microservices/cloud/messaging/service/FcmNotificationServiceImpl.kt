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

package org.radarbase.appserver.microservices.cloud.messaging.service

import com.google.common.eventbus.EventBus
import jakarta.inject.Inject
import jakarta.inject.Named
import org.radarbase.appserver.microservices.cloud.messaging.config.CloudMessagingServiceConfig
import org.radarbase.appserver.microservices.cloud.messaging.service.schedule.MessageSchedulerService
import org.radarbase.appserver.microservices.contract.calls.ProjectServiceContract
import org.radarbase.appserver.microservices.contract.calls.UserServiceContract
import org.radarbase.appserver.microservices.contract.utils.Utils.deserializeDtoFromContract
import org.radarbase.appserver.microservices.core.dto.ProjectDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmNotificationDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmNotifications
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUserDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUsers
import org.radarbase.appserver.microservices.core.entity.Notification
import org.radarbase.appserver.microservices.core.entity.Project
import org.radarbase.appserver.microservices.core.entity.Task
import org.radarbase.appserver.microservices.core.entity.User
import org.radarbase.appserver.microservices.core.event.state.MessageState
import org.radarbase.appserver.microservices.core.event.state.dto.NotificationStateEventDto
import org.radarbase.appserver.microservices.core.exception.AlreadyExistsException
import org.radarbase.appserver.microservices.core.exception.InvalidNotificationDetailsException
import org.radarbase.appserver.microservices.core.mapper.Mapper
import org.radarbase.appserver.microservices.core.repository.NotificationRepository
import org.radarbase.appserver.microservices.core.service.FcmNotificationService
import org.radarbase.appserver.microservices.core.utils.Const.NOTIFICATION_MAPPER
import org.radarbase.appserver.microservices.core.utils.Const.USER_MAPPER
import org.radarbase.appserver.microservices.core.utils.checkInvalidDetails
import org.radarbase.appserver.microservices.core.utils.checkPresence
import org.radarbase.appserver.microservices.core.utils.requireNotNullField
import java.time.Instant
import java.time.LocalDateTime
import kotlin.collections.forEach
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Suppress("unused")
class FcmNotificationServiceImpl @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val schedulerService: MessageSchedulerService<Notification>,
    @param:Named(NOTIFICATION_MAPPER) private val notificationMapper: Mapper<FcmNotificationDto, Notification>,
    @param:Named(USER_MAPPER) val userMapper: Mapper<FcmUserDto, User>,
    private val notificationStateEventPublisher: EventBus,
    config: CloudMessagingServiceConfig,
) : FcmNotificationService {

    private val userServiceUrl = config.contract.user
    private val projectServiceUrl = config.contract.project

    // TODO Add option to specify a scheduling provider (default will be fcm)
    // TODO: Use strategy pattern for handling notifications for scheduling and adding to database

    override suspend fun getAllNotifications(): FcmNotifications {
        val notifications: List<Notification> = notificationRepository.findAll()
        return FcmNotifications(
            notificationMapper.entitiesToDtos(notifications).toMutableList(),
        )
    }

    override suspend fun getNotificationById(id: Long): FcmNotificationDto {
        val notification: Notification? = notificationRepository.find(id)
        return notificationMapper.entityToDto(notification ?: Notification())
    }

    override suspend fun getNotificationsBySubjectId(subjectId: String): FcmNotifications {
        val user = deserializeDtoFromContract<FcmUserDto>(
            UserServiceContract.getUserUsingSubjectId(subjectId, userServiceUrl),
        ) {
            "user_not_found ; user with subjectId $subjectId not found"
        }

        val notifications: List<Notification> = notificationRepository.findByUserId(nonNullUserId(user))
        return FcmNotifications(
            notificationMapper.entitiesToDtos(notifications).toMutableList(),
        )
    }

    override suspend fun getNotificationsByProjectIdAndSubjectId(
        projectId: String,
        subjectId: String,
    ): FcmNotifications {
        return subjectAndProjectExistElseThrow(subjectId, projectId).let { user ->
            notificationRepository.findByUserId(nonNullUserId(user))
        }.let { notifications ->
            FcmNotifications(
                notificationMapper.entitiesToDtos(notifications).toMutableList(),
            )
        }
    }

    override suspend fun getNotificationsByProjectId(projectId: String): FcmNotifications {
        return deserializeDtoFromContract<ProjectDto>(
            ProjectServiceContract.getProjectUsingProjectId(projectId, projectServiceUrl),
        ) { "project_not_found ; Project not found with projectId $projectId" }.let { project ->
            deserializeDtoFromContract<FcmUsers>(
                UserServiceContract.getUsersUsingProjectId(nonNullProjectId(project), userServiceUrl),
            ) { "user_not_found ; user with projectId $projectId not found" }.users
        }.let { users ->
            hashSetOf<Notification>().also { notifications ->
                users.map { user ->
                    notificationRepository.findByUserId(nonNullUserId(user))
                }.forEach { userNotifications: List<Notification> ->
                    notifications.addAll(userNotifications)
                }
            }
        }.let { notifications ->
            FcmNotifications(
                notificationMapper.entitiesToDtos(notifications).toMutableList(),
            )
        }
    }

    override suspend fun checkIfNotificationExists(notificationDto: FcmNotificationDto, subjectId: String): Boolean {
        deserializeDtoFromContract<FcmUserDto>(
            UserServiceContract.getUserUsingSubjectId(subjectId, userServiceUrl),
        ) {
            "user_not_found ; user with subjectId $subjectId not found"
        }.let { user ->
            val notification = Notification.NotificationBuilder(
                notificationMapper.dtoToEntity(notificationDto),
            ).userId(nonNullUserId(user))
                .subjectId(user.subjectId)
                .projectId(user.projectId)
                .build()
            val notifications: List<Notification> = this.notificationRepository.findByUserId(nonNullUserId(user))
            return notifications.contains(notification)
        }
    }

    // TODO : WIP
    @Suppress("UNUSED_PARAMETER")
    override fun getFilteredNotifications(
        type: String?,
        delivered: Boolean?,
        ttlSeconds: Int?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int?,
    ): FcmNotifications? = null

    override suspend fun addNotification(
        notificationDto: FcmNotificationDto,
        subjectId: String,
        projectId: String,
        schedule: Boolean,
    ): FcmNotificationDto {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)
        val notificationExists: Boolean = checkNotificationExists(notificationDto, subjectId, projectId)

        if (!notificationExists) {
            val notificationSaved = addNotificationAndItsStateEvent(notificationDto, user)
            if (schedule) {
                this.schedulerService.schedule(notificationSaved)
            }
            return notificationMapper.entityToDto(notificationSaved)
        } else {
            throw AlreadyExistsException(
                "notifications.already_exists",
                "The Notification Already exists. Please Use update endpoint",
            )
        }
    }

    override suspend fun addNotification(
        notificationDto: FcmNotificationDto,
        subjectId: String,
        projectId: String,
    ): FcmNotificationDto {
        val notificationExists = checkNotificationExists(notificationDto, subjectId, projectId)
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)

        if (!notificationExists) {
            val savedNotification = addNotificationAndItsStateEvent(notificationDto, user)
            this.schedulerService.schedule(savedNotification)
            return notificationMapper.entityToDto(savedNotification)
        } else {
            throw AlreadyExistsException(
                "notifications.already_exists",
                "The Notification Already exists. Please Use update endpoint",
            )
        }
    }

    override suspend fun addNotificationAndItsStateEvent(
        notificationDto: FcmNotificationDto,
        user: User,
    ): Notification {
        val projectId = requireNotNullField(user.projectId, "User's Project Id")
        val subjectId = requireNotNullField(user.subjectId, "User's SubjectId")

        val savedNotification = this.notificationRepository.add(
            Notification.NotificationBuilder(notificationMapper.dtoToEntity(notificationDto))
                .userId(user.id)
                .projectId(projectId)
                .subjectId(subjectId)
                .build(),
        )
        requireNotNullField(user.usermetrics, "User's user metrics").lastOpened = Instant.now()
        UserServiceContract.updateUser(
            userMapper.entityToDto(user),
            projectId,
            subjectId,
            false,
            userServiceUrl,
        ).also {
            deserializeDtoFromContract<FcmUserDto>(it) {
                "user_not_found ; user with subjectId $subjectId not found"
            }
        }

        addNotificationStateEvent(
            savedNotification,
            MessageState.ADDED,
            requireNotNullField(
                savedNotification.createdAt,
                "Notification creation timestamp",
            ).toInstant(),
        )

        return savedNotification
    }

    override suspend fun checkNotificationExists(
        notificationDto: FcmNotificationDto,
        subjectId: String,
        projectId: String,
    ): Boolean {
        return subjectAndProjectExistElseThrow(subjectId, projectId).let { user ->
            notificationRepository.existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                nonNullUserId(user),
                requireNotNullField(notificationDto.sourceId, "Notification Source Id"),
                requireNotNullField(notificationDto.scheduledTime, "Notification Scheduled time"),
                requireNotNullField(notificationDto.title, "Notification Title"),
                requireNotNullField(notificationDto.body, "Notification Body"),
                requireNotNullField(notificationDto.type, "Notification Type"),
                requireNotNullField(notificationDto.ttlSeconds, "Notification TTL seconds"),
            )
        }
    }

    private fun addNotificationStateEvent(
        notification: Notification,
        state: MessageState,
        time: Instant,
    ) {
        val notificationStateEvent = NotificationStateEventDto(notification, state, null, time)
        notificationStateEventPublisher.post(notificationStateEvent)
    }

    override suspend fun updateNotification(
        notificationDto: FcmNotificationDto,
        subjectId: String,
        projectId: String,
    ): FcmNotificationDto {
        val notificationId = notificationDto.id
            ?: throw InvalidNotificationDetailsException("ID must be supplied for updating the notification")

        val user = subjectAndProjectExistElseThrow(subjectId, projectId)

        val notification = checkPresence(this.notificationRepository.find(notificationId), "notification_not_found") {
            "Notification does not exist. Please create one first"
        }

        val newNotification = Notification.NotificationBuilder(notification).body(notificationDto.body)
            .scheduledTime(notificationDto.scheduledTime).sourceId(notificationDto.sourceId)
            .title(notificationDto.title).ttlSeconds(notificationDto.ttlSeconds).type(notificationDto.type)
            .userId(user.id).subjectId(subjectId).projectId(projectId)
            .fcmMessageId(notificationDto.hashCode().toString()).build()
        val notificationSaved = this.notificationRepository.update(newNotification) ?: throw IllegalStateException(
            "Returned notification is null. Notification didn't updated successfully in the database.",
        )

        addNotificationStateEvent(
            notificationSaved,
            MessageState.UPDATED,
            requireNotNullField(
                notificationSaved.updatedAt,
                "Notification update timestamp",
            ).toInstant(),
        )
        if (!notification.delivered) {
            this.schedulerService.updateScheduled(notificationSaved)
        }
        return notificationMapper.entityToDto(notificationSaved)
    }

    override suspend fun scheduleAllUserNotifications(subjectId: String, projectId: String): FcmNotifications {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)
        val notifications: List<Notification> = notificationRepository.findByUserId(nonNullUserId(user))
        this.schedulerService.scheduleMultiple(notifications)
        return FcmNotifications(
            notificationMapper.entitiesToDtos(notifications).toMutableList(),
        )
    }

    override suspend fun scheduleNotification(subjectId: String, projectId: String, notificationId: Long): FcmNotificationDto {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)
        val notification = notificationRepository.findByIdAndUserId(notificationId, nonNullUserId(user))
        checkPresence(notification, "notification_not_found") {
            "The Notification with Id $notificationId does not exist in project $projectId for user $subjectId"
        }
        this.schedulerService.schedule(notification)
        return notificationMapper.entityToDto(notification)
    }

    override suspend fun removeNotificationsForUser(projectId: String, subjectId: String) {
        val userId = nonNullUserId(subjectAndProjectExistElseThrow(subjectId, projectId))
        val notifications: List<Notification> = this.notificationRepository.findByUserId(userId)
        this.schedulerService.deleteScheduledMultiple(notifications)

        this.notificationRepository.deleteByUserId(userId)
    }

    override suspend fun updateDeliveryStatus(fcmMessageId: String, isDelivered: Boolean) {
        val notification = this.notificationRepository.findByFcmMessageId(fcmMessageId)

        checkInvalidDetails<InvalidNotificationDetailsException>(
            { notification == null },
            {
                "Notification with the provided FCM message ID does not exist."
            },
        )
        val newNotification = Notification.NotificationBuilder(notification).delivered(isDelivered).build()
        this.notificationRepository.update(newNotification)
    }

    // TODO: Investigate if notifications can be marked in the state CANCELLED when deleted.
    override suspend fun deleteNotificationByProjectIdAndSubjectIdAndNotificationId(
        projectId: String,
        subjectId: String,
        id: Long,
    ) {
        val userId = nonNullUserId(subjectAndProjectExistElseThrow(subjectId, projectId))

        if (this.notificationRepository.existsByIdAndUserId(id, userId)) {
            this.schedulerService.deleteScheduled(
                this.notificationRepository.findByIdAndUserId(id, userId)!!,
            )
            this.notificationRepository.deleteByIdAndUserId(id, userId)
        } else {
            throw InvalidNotificationDetailsException(
                "Notification with the provided ID does not exist.",
            )
        }
    }

    override suspend fun removeNotificationsForUserUsingTaskId(projectId: String, subjectId: String, taskId: Long) {
        val userId = nonNullUserId(subjectAndProjectExistElseThrow(subjectId, projectId))

        val notifications: List<Notification> = this.notificationRepository.findByUserIdAndTaskId(userId, taskId)
        this.schedulerService.deleteScheduledMultiple(notifications)

        this.notificationRepository.deleteByUserIdAndTaskId(userId, taskId)
    }

    override suspend fun removeNotificationsForUserUsingFcmToken(fcmToken: String) {
        val user =
            deserializeDtoFromContract<FcmUserDto>(UserServiceContract.getUserUsingFcmToken(fcmToken, userServiceUrl)) {
                "invalid_user_details ; The user with the given Fcm Token does not exist"
            }
        val userId = nonNullUserId(user)
        this.schedulerService.deleteScheduledMultiple(
            this.notificationRepository.findByUserId(userId),
        )
        this.notificationRepository.deleteByUserId(userId)
    }

    override suspend fun deleteNotificationsByTaskId(task: Task) {
        val taskId = task.id ?: return
        if (notificationRepository.existsByTaskId(taskId)) {
            val notifications: List<Notification> = notificationRepository.findByTaskId(taskId)
            schedulerService.deleteScheduledMultiple(notifications)
            notificationRepository.deleteByTaskId(taskId)
        }
    }

    override suspend fun addNotifications(
        notificationDtos: FcmNotifications,
        subjectId: String,
        projectId: String,
        schedule: Boolean,
    ): FcmNotifications {
        val savedNotifications = addNewNotifications(notificationDtos, subjectId, projectId)
        savedNotifications.forEach { n: Notification ->
            addNotificationStateEvent(
                n,
                MessageState.ADDED,
                requireNotNullField(n.createdAt, "Notification creation timestamp").toInstant(),
            )
        }

        if (schedule) {
            this.schedulerService.scheduleMultiple(savedNotifications)
        }
        return FcmNotifications(
            notificationMapper.entitiesToDtos(savedNotifications).toMutableList(),
        )
    }

    override suspend fun addNotifications(notifications: List<Notification>?, user: User): List<Notification> {
        notifications ?: return listOf()
        val newNotifications: List<Notification> = notifications.filter { notification: Notification ->
            !notificationRepository.existsByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                requireNotNullField(user.id, "User id"),
                requireNotNullField(notification.sourceId, "Notification Source Id"),
                requireNotNullField(notification.scheduledTime, "Notification Scheduled time"),
                requireNotNullField(notification.title, "Notification Title"),
                requireNotNullField(notification.body, "Notification Body"),
                requireNotNullField(notification.type, "Notification Type"),
                requireNotNullField(notification.ttlSeconds, "Notification TTL seconds"),
            )
        }

        val savedNotifications: List<Notification> = newNotifications.map {
            this.notificationRepository.add(it)
        }
        savedNotifications.forEach { n: Notification? ->
            addNotificationStateEvent(
                n!!,
                MessageState.ADDED,
                requireNotNullField(n.createdAt, "Notification creation timestamp").toInstant(),
            )
        }
        this.schedulerService.scheduleMultiple(savedNotifications)
        return savedNotifications
    }

    override suspend fun addNotifications(
        notificationDtos: FcmNotifications,
        subjectId: String,
        projectId: String,
    ): FcmNotifications {
        val savedNotifications = addNewNotifications(notificationDtos, subjectId, projectId)
        savedNotifications.forEach { n: Notification ->
            addNotificationStateEvent(
                n,
                MessageState.ADDED,
                requireNotNullField(n.createdAt, "Notification creation timestamp").toInstant(),
            )
        }

        this.schedulerService.scheduleMultiple(savedNotifications)
        return FcmNotifications(
            notificationMapper.entitiesToDtos(savedNotifications).toMutableList(),
        )
    }

    override suspend fun addNewNotifications(
        notificationDtos: FcmNotifications,
        subjectId: String,
        projectId: String,
    ): List<Notification> {
        val newNotifications: List<Notification> = subjectAndProjectExistElseThrow(subjectId, projectId).let { user ->
            notificationRepository.findByUserId(nonNullUserId(user)).let { notifications ->
                notificationDtos.notifications.map { dto: FcmNotificationDto ->
                    notificationMapper.dtoToEntity(dto)
                }.map { notification ->
                    Notification.NotificationBuilder(notification)
                        .userId(user.id)
                        .subjectId(subjectId)
                        .projectId(projectId)
                        .build()
                }.filter { notification ->
                    !notifications.contains(notification)
                }
            }
        }

        return newNotifications.map {
            this.notificationRepository.add(it)
        }
    }

    suspend fun subjectAndProjectExistElseThrow(subjectId: String, projectId: String): User {
        return deserializeDtoFromContract<ProjectDto>(ProjectServiceContract.getProjectUsingProjectId(projectId, projectServiceUrl)) {
            "project_not_found ; Project Id $projectId does not exist. Please create a project with the ID first"
        }.let { project ->
            UserServiceContract.getUserUsingProjectIdAndSubjectId(
                projectId,
                subjectId,
                userServiceUrl
            ).let {
                deserializeDtoFromContract<FcmUserDto>(
                    it
                ) {
                    "user_not_found ; $INVALID_SUBJECT_ID_MESSAGE"
                }
            }.let {
                userMapper.dtoToEntity(it)
            }
        }
    }

    override suspend fun getNotificationByProjectIdAndSubjectIdAndNotificationId(
        projectId: String,
        subjectId: String,
        notificationId: Long,
    ): Notification {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)
        val notification = notificationRepository.findByIdAndUserId(notificationId, nonNullUserId(user))
            ?: throw InvalidNotificationDetailsException(
                "The Notification with Id $notificationId does not exist in project $projectId for user $subjectId",
            )

        return notification
    }

    override suspend fun getNotificationByMessageId(messageId: String): Notification {
        val notification = this.notificationRepository.findByFcmMessageId(messageId)
        checkInvalidDetails<InvalidNotificationDetailsException>(
            {
                notification == null
            },
            {
                "The Notification with FCM Message Id $messageId does not exist."
            },
        )
        return notification!!
    }

    companion object {
        private const val INVALID_SUBJECT_ID_MESSAGE =
            "The supplied Subject ID is invalid. No user found. Please Create a User First."
    }

    @OptIn(ExperimentalContracts::class)
    private fun checkPresenceOfUser(user: User?) {
        contract {
            returns() implies (user != null)
        }
        checkPresence(user, "user_not_found") {
            INVALID_SUBJECT_ID_MESSAGE
        }
    }

    fun nonNullProjectId(project: Project): Long = checkNotNull(project.id) {
        "User id cannot be null"
    }

    fun nonNullProjectId(project: ProjectDto): String = checkNotNull(project.projectId) {
        "User id cannot be null"
    }

    fun nonNullUserId(user: FcmUserDto): Long = checkNotNull(user.id) {
        "User id cannot be null"
    }

    fun nonNullUserId(user: User): Long = checkNotNull(user.id) {
        "User id cannot be null"
    }
}
