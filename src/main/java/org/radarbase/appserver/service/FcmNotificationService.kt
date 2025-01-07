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

import org.radarbase.appserver.dto.fcm.FcmNotificationDto
import org.radarbase.appserver.dto.fcm.FcmNotifications
import org.radarbase.appserver.entity.Notification
import org.radarbase.appserver.entity.Notification.NotificationBuilder
import org.radarbase.appserver.entity.Task
import org.radarbase.appserver.entity.User
import org.radarbase.appserver.event.state.MessageState
import org.radarbase.appserver.event.state.dto.NotificationStateEventDto
import org.radarbase.appserver.exception.InvalidNotificationDetailsException
import org.radarbase.appserver.exception.InvalidUserDetailsException
import org.radarbase.appserver.exception.NotFoundException
import org.radarbase.appserver.exception.NotificationAlreadyExistsException
import org.radarbase.appserver.mapper.NotificationMapper
import org.radarbase.appserver.repository.NotificationRepository
import org.radarbase.appserver.repository.ProjectRepository
import org.radarbase.appserver.repository.UserRepository
import org.radarbase.appserver.service.scheduler.MessageSchedulerService
import org.radarbase.appserver.util.checkInvalidDetails
import org.radarbase.appserver.util.checkPresence
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime

/**
 * [Service] for interacting with the [Notification] [jakarta.persistence.Entity]
 * using the [NotificationRepository].
 *
 * @author yatharthranjan
 */
@Suppress("unused")
@Service
class FcmNotificationService(
    private val notificationRepository: NotificationRepository,
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val schedulerService: MessageSchedulerService<Notification>,
    private val notificationConverter: NotificationMapper,
    private val notificationStateEventPublisher: ApplicationEventPublisher?
) : NotificationService {

    // TODO Add option to specify a scheduling provider (default will be fcm)
    // TODO: Use strategy pattern for handling notifications for scheduling and adding to database

    @Transactional(readOnly = true)
    fun getAllNotifications(): FcmNotifications {
        val notifications: List<Notification> = notificationRepository.findAll()
        return FcmNotifications()
            .withNotifications(notificationConverter.entitiesToDtos(notifications))
    }

    @Transactional(readOnly = true)
    fun getNotificationById(id: Long): FcmNotificationDto {
        val notification: Notification? = notificationRepository.findByIdOrNull(id)
        return notificationConverter.entityToDto(notification ?: Notification())
    }

    @Transactional(readOnly = true)
    fun getNotificationsBySubjectId(subjectId: String?): FcmNotifications {
        val user = this.userRepository.findBySubjectId(subjectId)
        if (user == null) {
            throw NotFoundException(INVALID_SUBJECT_ID_MESSAGE)
        }
        val notifications: List<Notification> = notificationRepository.findByUserId(user.id)
        return FcmNotifications()
            .withNotifications(notificationConverter.entitiesToDtos(notifications))
    }

    @Transactional(readOnly = true)
    fun getNotificationsByProjectIdAndSubjectId(
        projectId: String?, subjectId: String?
    ): FcmNotifications {
        return subjectAndProjectExistElseThrow(subjectId, projectId).let { user ->
            notificationRepository.findByUserId(user.id)
        }.let { notifications ->
            FcmNotifications()
                .withNotifications(notificationConverter.entitiesToDtos(notifications))
        }
    }

    @Transactional(readOnly = true)
    fun getNotificationsByProjectId(projectId: String?): FcmNotifications {
        return checkPresence(projectRepository.findByProjectId(projectId)) {
            "Project not found with projectId $projectId"
        }.let { project ->
            this.userRepository.findByProjectId(project.id)
        }.let { users ->
            hashSetOf<Notification>().also { notifications ->
                users.map { user ->
                    notificationRepository.findByUserId(user.id)
                }.forEach { userNotifications: List<Notification> ->
                    notifications.addAll(userNotifications)
                }
            }
        }.let { notifications ->
            FcmNotifications()
                .withNotifications(notificationConverter.entitiesToDtos(notifications))
        }
    }

    @Transactional(readOnly = true)
    fun checkIfNotificationExists(notificationDto: FcmNotificationDto, subjectId: String?): Boolean {
        checkPresence(this.userRepository.findBySubjectId(subjectId)) {
            INVALID_SUBJECT_ID_MESSAGE
        }.let { user ->
            val notification =
                NotificationBuilder(notificationConverter.dtoToEntity(notificationDto)).user(user).build()
            val notifications: List<Notification> = this.notificationRepository.findByUserId(user.id)
            return notifications.contains(notification)
        }
    }

    // TODO : WIP
    @Transactional(readOnly = true)
    fun getFilteredNotifications(
        type: String?,
        delivered: Boolean,
        ttlSeconds: Int,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int
    ): FcmNotifications? = null

    @Transactional
    fun addNotification(
        notificationDto: FcmNotificationDto, subjectId: String?, projectId: String?, schedule: Boolean
    ): FcmNotificationDto {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)
        val notification: Notification? = notificationRepository
            .findByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                user.id,
                notificationDto.sourceId,
                notificationDto.scheduledTime,
                notificationDto.title,
                notificationDto.body,
                notificationDto.type,
                notificationDto.ttlSeconds
            )

        if (notification == null) {
            val notificationSaved = this.notificationRepository.saveAndFlush<Notification>(
                NotificationBuilder(notificationConverter.dtoToEntity(notificationDto)).user(user).build()
            )
            user.usermetrics!!.lastOpened = Instant.now()
            this.userRepository.save(user)
            addNotificationStateEvent(
                notificationSaved, MessageState.ADDED, notificationSaved.createdAt!!.toInstant()
            )
            if (schedule) {
                this.schedulerService.schedule(notificationSaved)
            }
            return notificationConverter.entityToDto(notificationSaved)
        } else {
            throw NotificationAlreadyExistsException(
                "The Notification Already exists. Please Use update endpoint",
                notificationConverter.entityToDto(notification)
            )
        }
    }

    @Transactional
    fun addNotification(
        notificationDto: FcmNotificationDto, subjectId: String?, projectId: String?
    ): FcmNotificationDto {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)
        val notification: Notification? = notificationRepository
            .findByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                user.id,
                notificationDto.sourceId,
                notificationDto.scheduledTime,
                notificationDto.title,
                notificationDto.body,
                notificationDto.type,
                notificationDto.ttlSeconds
            )

        if (notification == null) {
            val notificationSaved = this.notificationRepository.saveAndFlush<Notification>(
                NotificationBuilder(notificationConverter.dtoToEntity(notificationDto)).user(user).build()
            )
            user.usermetrics!!.lastOpened = Instant.now()
            this.userRepository.save(user)
            addNotificationStateEvent(
                notificationSaved, MessageState.ADDED, notificationSaved.createdAt!!.toInstant()
            )
            this.schedulerService.schedule(notificationSaved)
            return notificationConverter.entityToDto(notificationSaved)
        } else {
            throw NotificationAlreadyExistsException(
                "The Notification Already exists. Please Use update endpoint",
                notificationConverter.entityToDto(notification)
            )
        }
    }

    private fun addNotificationStateEvent(
        notification: Notification, state: MessageState, time: Instant
    ) {
        if (notificationStateEventPublisher != null) {
            val notificationStateEvent =
                NotificationStateEventDto(this, notification, state, null, time)
            notificationStateEventPublisher.publishEvent(notificationStateEvent)
        }
    }


    @Transactional
    fun updateNotification(
        notificationDto: FcmNotificationDto, subjectId: String?, projectId: String?
    ): FcmNotificationDto {
        val notificationId = notificationDto.id

        checkInvalidDetails<InvalidNotificationDetailsException>({ notificationId == null }, {
            "ID must be supplied for updating the notification"
        })

        val user = subjectAndProjectExistElseThrow(subjectId, projectId)

        val notification = checkPresence(this.notificationRepository.findByIdOrNull(notificationId)) {
            throw NotFoundException("Notification does not exist. Please create first")
        }

        val newNotification = NotificationBuilder(notification)
            .body(notificationDto.body)
            .scheduledTime(notificationDto.scheduledTime)
            .sourceId(notificationDto.sourceId)
            .title(notificationDto.title)
            .ttlSeconds(notificationDto.ttlSeconds)
            .type(notificationDto.type)
            .user(user)
            .fcmMessageId(notificationDto.hashCode().toString())
            .build()
        val notificationSaved = this.notificationRepository.saveAndFlush<Notification>(newNotification)

        addNotificationStateEvent(
            notificationSaved, MessageState.UPDATED, notificationSaved.updatedAt!!.toInstant()
        )
        if (!notification.delivered) {
            this.schedulerService.updateScheduled(notificationSaved)
        }
        return notificationConverter.entityToDto(notificationSaved)
    }

    @Transactional
    fun scheduleAllUserNotifications(subjectId: String, projectId: String): FcmNotifications {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)
        val notifications: List<Notification> = notificationRepository.findByUserId(user.id)
        this.schedulerService.scheduleMultiple(notifications)
        return FcmNotifications()
            .withNotifications(notificationConverter.entitiesToDtos(notifications))
    }

    @Transactional
    fun scheduleNotification(subjectId: String?, projectId: String?, notificationId: Long): FcmNotificationDto {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)
        val notification = notificationRepository.findByIdAndUserId(notificationId, user.id!!)
        checkPresence(notification) {
            "The Notification with Id $notificationId does not exist in project $projectId for user $subjectId"
        }
        this.schedulerService.schedule(notification)
        return notificationConverter.entityToDto(notification)
    }

    @Transactional
    fun removeNotificationsForUser(projectId: String?, subjectId: String?) {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)

        val notifications: List<Notification> = this.notificationRepository.findByUserId(user.id)
        this.schedulerService.deleteScheduledMultiple(notifications)

        this.notificationRepository.deleteByUserId(user.id)
    }

    @Transactional
    fun updateDeliveryStatus(fcmMessageId: String?, isDelivered: Boolean) {
        val notification =
            this.notificationRepository.findByFcmMessageId(fcmMessageId)

        checkInvalidDetails<InvalidNotificationDetailsException>(
            { notification == null },
            {
                "Notification with the provided FCM message ID does not exist."
            }
        )
        val newNotif = NotificationBuilder(notification).delivered(isDelivered).build()
        this.notificationRepository.save<Notification?>(newNotif)
    }

    // TODO: Investigate if notifications can be marked in the state CANCELLED when deleted.
    @Transactional
    fun deleteNotificationByProjectIdAndSubjectIdAndNotificationId(projectId: String?, subjectId: String?, id: Long) {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)
        val userId = user.id

        if (this.notificationRepository.existsByIdAndUserId(id, userId)) {
            this.schedulerService.deleteScheduled(
                this.notificationRepository.findByIdAndUserId(id, userId!!)
            )
            this.notificationRepository.deleteByIdAndUserId(id, userId)
        } else throw InvalidNotificationDetailsException(
            "Notification with the provided ID does not exist."
        )
    }

    @Transactional
    fun removeNotificationsForUserUsingTaskId(projectId: String?, subjectId: String?, taskId: Long?) {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)

        val notifications: List<Notification> = this.notificationRepository.findByUserIdAndTaskId(user.id, taskId)
        this.schedulerService.deleteScheduledMultiple(notifications)

        this.notificationRepository.deleteByUserIdAndTaskId(user.id, taskId)
    }

    @Transactional
    fun removeNotificationsForUserUsingFcmToken(fcmToken: String?) {
        val user = this.userRepository.findByFcmToken(fcmToken)

        checkInvalidDetails<InvalidUserDetailsException>(
            { user == null },
            { "The user with the given Fcm Token does not exist" }
        )


        this.schedulerService.deleteScheduledMultiple(
            this.notificationRepository.findByUserId(user!!.id)
        )

        this.notificationRepository.deleteByUserId(user.id)
    }

    @Transactional
    fun deleteNotificationsByTaskId(task: Task?) {
        val taskId = task?.id ?: return
        if (notificationRepository.existsByTaskId(taskId)) {
            val notifications: List<Notification> = notificationRepository.findByTaskId(taskId)
            schedulerService.deleteScheduledMultiple(notifications)
            notificationRepository.deleteByTaskId(taskId)
        }
    }

    @Transactional
    fun addNotifications(
        notificationDtos: FcmNotifications, subjectId: String?, projectId: String?, schedule: Boolean
    ): FcmNotifications {

        val newNotifications: List<Notification> = subjectAndProjectExistElseThrow(subjectId, projectId).let { user ->
            notificationRepository.findByUserId(user.id).let { notifications ->
                notificationDtos.notifications.map { dto: FcmNotificationDto ->
                    notificationConverter.dtoToEntity(dto)
                }.map { notification ->
                    NotificationBuilder(notification).user(user).build()
                }.filter { notification ->
                    !notifications.contains(notification)
                }
            }
        }

        val savedNotifications = this.notificationRepository.saveAll(newNotifications)
        this.notificationRepository.flush()
        savedNotifications.forEach { n: Notification ->
            addNotificationStateEvent(
                n,
                MessageState.ADDED,
                n.createdAt!!.toInstant()
            )
        }

        if (schedule) {
            this.schedulerService.scheduleMultiple(savedNotifications)
        }
        return FcmNotifications()
            .withNotifications(notificationConverter.entitiesToDtos(savedNotifications))
    }

    @Transactional
    fun addNotifications(notifications: List<Notification>?, user: User): List<Notification> {
        notifications ?: return listOf()
        val newNotifications: List<Notification> = notifications.filter { notification: Notification ->
            notificationRepository
                .findByUserIdAndSourceIdAndScheduledTimeAndTitleAndBodyAndTypeAndTtlSeconds(
                    user.id,
                    notification.sourceId,
                    notification.scheduledTime,
                    notification.title,
                    notification.body,
                    notification.type,
                    notification.ttlSeconds
                ) == null
        }

        val savedNotifications: List<Notification> = this.notificationRepository.saveAllAndFlush(newNotifications)
        savedNotifications.forEach { n: Notification? ->
            addNotificationStateEvent(
                n!!,
                MessageState.ADDED,
                n.createdAt!!.toInstant()
            )
        }
        this.schedulerService.scheduleMultiple(savedNotifications)
        return savedNotifications
    }

    @Transactional
    fun addNotifications(
        notificationDtos: FcmNotifications, subjectId: String?, projectId: String?
    ): FcmNotifications {

        val newNotifications: List<Notification> = subjectAndProjectExistElseThrow(subjectId, projectId).let { user ->
            notificationRepository.findByUserId(user.id).let { notifications ->
                notificationDtos.notifications.map { dto: FcmNotificationDto ->
                    notificationConverter.dtoToEntity(dto)
                }.map { notification ->
                    NotificationBuilder(notification).user(user).build()
                }.filter { notification ->
                    !notifications.contains(notification)
                }
            }
        }

        val savedNotifications = this.notificationRepository.saveAll(newNotifications)
        this.notificationRepository.flush()
        savedNotifications.forEach { n: Notification ->
            addNotificationStateEvent(
                n,
                MessageState.ADDED,
                n.createdAt!!.toInstant()
            )
        }

        this.schedulerService.scheduleMultiple(savedNotifications)
        return FcmNotifications()
            .withNotifications(notificationConverter.entitiesToDtos(savedNotifications))
    }

    fun subjectAndProjectExistElseThrow(subjectId: String?, projectId: String?): User {
        return checkPresence(this.projectRepository.findByProjectId(projectId)) {
            "Project Id does not exist. Please create a project with the ID first"
        }.let { project ->
            checkPresence(this.userRepository.findBySubjectIdAndProjectId(subjectId, project.id)) {
                INVALID_SUBJECT_ID_MESSAGE
            }
        }
    }

    @Transactional(readOnly = true)
    fun getNotificationByProjectIdAndSubjectIdAndNotificationId(
        projectId: String?, subjectId: String?, notificationId: Long
    ): Notification {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)

        val notification = notificationRepository.findByIdAndUserId(notificationId, user.id!!)

        checkInvalidDetails<InvalidNotificationDetailsException>({
            notification == null
        }, {
            "The Notification with Id $notificationId does not exist in project $projectId for user $subjectId"
        })

        return notification!!
    }

    @Transactional(readOnly = true)
    fun getNotificationByMessageId(messageId: String?): Notification {
        val notification = this.notificationRepository.findByFcmMessageId(messageId)
        checkInvalidDetails<InvalidNotificationDetailsException>({
            notification == null
        }, {
            "The Notification with FCM Message Id $messageId does not exist."
        })
        return notification!!
    }

    companion object {
        private const val INVALID_SUBJECT_ID_MESSAGE = "The supplied Subject ID is invalid. No user found. Please Create a User First."
    }
}
