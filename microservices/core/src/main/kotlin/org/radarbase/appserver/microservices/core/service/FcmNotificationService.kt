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

import org.radarbase.appserver.microservices.core.dto.fcm.FcmNotificationDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmNotifications
import org.radarbase.appserver.microservices.core.entity.Notification
import org.radarbase.appserver.microservices.core.entity.Task
import org.radarbase.appserver.microservices.core.entity.User
import java.time.LocalDateTime

/**
 * Contract for operations related to Notifications (FCM).
 *
 * Implementations are expected to perform validation, persistence and scheduling
 * as appropriate for the concrete environment (e.g. FCM-backed scheduler).
 */
interface FcmNotificationService {

    /** Get all notifications wrapped as DTO. */
    suspend fun getAllNotifications(): FcmNotifications

    /** Get a single notification DTO by id. */
    suspend fun getNotificationById(id: Long): FcmNotificationDto

    /** Get all notifications for the user identified by subjectId. */
    suspend fun getNotificationsBySubjectId(subjectId: String): FcmNotifications

    /** Get all notifications for a (projectId, subjectId) pair. */
    suspend fun getNotificationsByProjectIdAndSubjectId(projectId: String, subjectId: String): FcmNotifications

    /** Get all notifications for the given project. */
    suspend fun getNotificationsByProjectId(projectId: String): FcmNotifications

    /**
     * Check whether a notification (dto) already exists for the given subject.
     * Returns true if an equivalent Notification exists for the user.
     */
    suspend fun checkIfNotificationExists(notificationDto: FcmNotificationDto, subjectId: String): Boolean

    /**
     * Filter notifications by optional criteria.
     */
    fun getFilteredNotifications(
        type: String? = null,
        delivered: Boolean? = null,
        ttlSeconds: Int? = null,
        startTime: LocalDateTime? = null,
        endTime: LocalDateTime? = null,
        limit: Int? = null,
    ): FcmNotifications?

    /**
     * Add a notification for the user identified by (subjectId, projectId).
     * Returns the saved DTO.
     */
    suspend fun addNotification(
        notificationDto: FcmNotificationDto,
        subjectId: String,
        projectId: String,
        schedule: Boolean,
    ): FcmNotificationDto

    /**
     * Add a notification for the user identified by (subjectId, projectId) and always schedule it.
     * Returns the saved DTO.
     */
    suspend fun addNotification(
        notificationDto: FcmNotificationDto,
        subjectId: String,
        projectId: String,
    ): FcmNotificationDto

    /**
     * Add a notification entity and publish its state event; returns the saved Notification entity.
     */
    suspend fun addNotificationAndItsStateEvent(notificationDto: FcmNotificationDto, user: User): Notification

    /**
     * Check if a notification for given (subjectId, projectId) already exists using all relevant fields.
     */
    suspend fun checkNotificationExists(notificationDto: FcmNotificationDto, subjectId: String, projectId: String): Boolean

    /** Update an existing notification. Returns the updated DTO. */
    suspend fun updateNotification(notificationDto: FcmNotificationDto, subjectId: String, projectId: String): FcmNotificationDto

    /** Schedule all notifications for a user (subjectId, projectId) and return them as DTO wrapper. */
    suspend fun scheduleAllUserNotifications(subjectId: String, projectId: String): FcmNotifications

    /** Schedule one notification and return it as DTO. */
    suspend fun scheduleNotification(subjectId: String, projectId: String, notificationId: Long): FcmNotificationDto

    /** Remove all notifications for the given (projectId, subjectId) user. */
    suspend fun removeNotificationsForUser(projectId: String, subjectId: String)

    /** Update the delivery status (delivered/not) identified by the FCM message id. */
    suspend fun updateDeliveryStatus(fcmMessageId: String, isDelivered: Boolean)

    /**
     * Delete a specific notification by id for a (projectId, subjectId) pair.
     */
    suspend fun deleteNotificationByProjectIdAndSubjectIdAndNotificationId(projectId: String, subjectId: String, id: Long)

    /** Remove notifications for a user filtered by taskId. */
    suspend fun removeNotificationsForUserUsingTaskId(projectId: String, subjectId: String, taskId: Long)

    /** Remove all notifications for a user identified by their FCM token. */
    suspend fun removeNotificationsForUserUsingFcmToken(fcmToken: String)

    /** Delete all notifications associated with a Task entity. */
    suspend fun deleteNotificationsByTaskId(task: Task)

    /**
     * Add multiple notifications (DTO wrapper) for a user and optionally schedule them.
     * Returns the saved notifications as DTO wrapper.
     */
    suspend fun addNotifications(notificationDtos: FcmNotifications, subjectId: String, projectId: String, schedule: Boolean): FcmNotifications

    /**
     * Add notifications from entity list for a resolved user. Returns saved entity list.
     */
    suspend fun addNotifications(notifications: List<Notification>?, user: User): List<Notification>

    /**
     * Add multiple notifications (DTO wrapper) for a user and schedule them. Returns saved DTO wrapper.
     */
    suspend fun addNotifications(notificationDtos: FcmNotifications, subjectId: String, projectId: String): FcmNotifications

    /**
     * Create and persist new notifications (entities) without scheduling; returns saved entities.
     */
    suspend fun addNewNotifications(notificationDtos: FcmNotifications, subjectId: String, projectId: String): List<Notification>

    /**
     * Get a Notification entity by (projectId, subjectId, notificationId). Throws if not found.
     */
    suspend fun getNotificationByProjectIdAndSubjectIdAndNotificationId(projectId: String, subjectId: String, notificationId: Long): Notification

    /** Get a Notification entity by its FCM message id. */
    suspend fun getNotificationByMessageId(messageId: String): Notification
}
