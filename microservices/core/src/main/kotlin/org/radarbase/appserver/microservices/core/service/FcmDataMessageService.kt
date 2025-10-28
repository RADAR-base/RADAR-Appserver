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

import org.radarbase.appserver.microservices.core.dto.fcm.FcmDataMessageDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmDataMessages
import org.radarbase.appserver.microservices.core.entity.DataMessage
import org.radarbase.appserver.microservices.core.entity.User
import java.time.LocalDateTime

/**
 * Contract for operations related to Data Messages (FCM).
 *
 * Implementations are expected to perform validation, persistence and scheduling
 * as appropriate for the concrete environment (e.g. FCM-backed scheduler).
 */
interface DataMessageService {

    /** Get all data messages as DTO wrapper. */
    suspend fun getAllDataMessages(): FcmDataMessages

    /** Get a single data message DTO by DB id. Returns an "empty" DTO when not found in current implementation. */
    suspend fun getDataMessageById(id: Long): FcmDataMessageDto

    /** Get all data messages for the user identified by subjectId. */
    suspend fun getDataMessagesBySubjectId(subjectId: String): FcmDataMessages

    /** Get all data messages for a (projectId, subjectId) pair. */
    suspend fun getDataMessagesByProjectIdAndSubjectId(projectId: String, subjectId: String): FcmDataMessages

    /** Get all data messages for the given project. */
    suspend fun getDataMessagesByProjectId(projectId: String): FcmDataMessages

    /**
     * Check whether a data message (dto) already exists for the given subject.
     * Returns true if an equivalent DataMessage exists for the user.
     */
    suspend fun checkIfDataMessageExists(dataMessageDto: FcmDataMessageDto, subjectId: String): Boolean

    /**
     * Filter data messages by a variety of optional criteria.
     * Current service has this as TODO/WIP and returns null in some implementations.
     */
    fun getFilteredDataMessages(
        type: String? = null,
        delivered: Boolean? = null,
        ttlSeconds: Int? = null,
        startTime: LocalDateTime? = null,
        endTime: LocalDateTime? = null,
        limit: Int? = null,
    ): FcmDataMessages?

    /**
     * Add a single data message for the user identified by (subjectId, projectId).
     * Returns the saved DTO.
     */
    suspend fun addDataMessage(
        dataMessageDto: FcmDataMessageDto,
        subjectId: String,
        projectId: String,
    ): FcmDataMessageDto

    /**
     * Update an existing data message. The DTO must contain the id.
     * Returns the updated DTO.
     */
    suspend fun updateDataMessage(
        dataMessageDto: FcmDataMessageDto,
        subjectId: String,
        projectId: String,
    ): FcmDataMessageDto

    /** Remove all data messages for the given (projectId, subjectId) user. */
    suspend fun removeDataMessagesForUser(projectId: String, subjectId: String)

    /** Update the delivery status (delivered/not) identified by the FCM message id. */
    suspend fun updateDeliveryStatus(fcmMessageId: String, isDelivered: Boolean)

    /**
     * Delete a specific data message by id for a (projectId, subjectId) pair.
     * Should validate the message belongs to the given user/project.
     */
    suspend fun deleteDataMessageByProjectIdAndSubjectIdAndDataMessageId(
        projectId: String,
        subjectId: String,
        id: Long,
    )

    /** Remove all data messages for a user identified by their FCM token. */
    suspend fun removeDataMessagesForUserUsingFcmToken(fcmToken: String)

    /** Add multiple data messages for a user; returns the DTO wrapper after saving. */
    suspend fun addDataMessages(
        dataMessageDtos: FcmDataMessages,
        subjectId: String,
        projectId: String,
    ): FcmDataMessages

    /**
     * Ensure subject and project exist; returns the resolved User entity or throws.
     * Exposed here because callers (or other implementations) may need the same behaviour.
     */
    suspend fun subjectAndProjectExistElseThrow(subjectId: String, projectId: String): User

    /**
     * Get a DataMessage entity by (projectId, subjectId, dataMessageId).
     * Throws if not found.
     */
    suspend fun getDataMessageByProjectIdAndSubjectIdAndDataMessageId(
        projectId: String,
        subjectId: String,
        dataMessageId: Long,
    ): DataMessage

    /** Get a DataMessage entity by its FCM message id. */
    suspend fun getDataMessageByMessageId(messageId: String): DataMessage
}
