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

import com.google.common.eventbus.EventBus
import jakarta.inject.Inject
import jakarta.inject.Named
import org.radarbase.appserver.jersey.dto.fcm.FcmDataMessageDto
import org.radarbase.appserver.jersey.dto.fcm.FcmDataMessages
import org.radarbase.appserver.jersey.enhancer.AppserverResourceEnhancer.Companion.DATA_MESSAGE_MAPPER
import org.radarbase.appserver.jersey.entity.DataMessage
import org.radarbase.appserver.jersey.entity.Project
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.event.state.MessageState
import org.radarbase.appserver.jersey.event.state.dto.DataMessageStateEventDto
import org.radarbase.appserver.jersey.exception.AlreadyExistsException
import org.radarbase.appserver.jersey.exception.InvalidNotificationDetailsException
import org.radarbase.appserver.jersey.exception.InvalidUserDetailsException
import org.radarbase.appserver.jersey.mapper.DataMessageMapper
import org.radarbase.appserver.jersey.mapper.Mapper
import org.radarbase.appserver.jersey.repository.DataMessageRepository
import org.radarbase.appserver.jersey.repository.ProjectRepository
import org.radarbase.appserver.jersey.repository.UserRepository
import org.radarbase.appserver.jersey.service.TaskService.Companion.nonNullUserId
import org.radarbase.appserver.jersey.service.questionnaire_schedule.MessageSchedulerService
import org.radarbase.appserver.jersey.utils.checkInvalidDetails
import org.radarbase.appserver.jersey.utils.checkPresence
import org.radarbase.appserver.jersey.utils.requireNotNullField
import org.radarbase.jersey.exception.HttpNotFoundException
import java.time.Instant
import java.time.LocalDateTime
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Suppress("unused")
class FcmDataMessageService @Inject constructor(
    private val dataMessageRepository: DataMessageRepository,
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val schedulerService: MessageSchedulerService<DataMessage>,
    @field:Named(DATA_MESSAGE_MAPPER) private val dataMessageMapper: Mapper<FcmDataMessageDto, DataMessage>,
    private val dataMessageStateEventPublisher: EventBus,
) : DataMessageService {

    // TODO Add option to specify a scheduling provider (default will be fcm)
    // TODO: Use strategy pattern for handling data messages for scheduling and adding to database

    suspend fun getAllDataMessages(): FcmDataMessages {
        val dataMessages = dataMessageRepository.findAll()
        return FcmDataMessages()
            .withDataMessages(dataMessageMapper.entitiesToDtos(dataMessages))
    }

    suspend fun getDataMessageById(id: Long): FcmDataMessageDto {
        val dataMessage = dataMessageRepository.find(id)
        return dataMessageMapper.entityToDto(dataMessage ?: DataMessage())
    }

    suspend fun getDataMessagesBySubjectId(subjectId: String): FcmDataMessages {
        val user = this.userRepository.findBySubjectId(subjectId)
        checkPresenceOfUser(user)

        val dataMessages = dataMessageRepository.findByUserId(nonNullUserId(user))
        return FcmDataMessages()
            .withDataMessages(dataMessageMapper.entitiesToDtos(dataMessages))
    }

    suspend fun getDataMessagesByProjectIdAndSubjectId(
        projectId: String, subjectId: String,
    ): FcmDataMessages {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)

        val dataMessages = dataMessageRepository.findByUserId(nonNullUserId(user))
        return FcmDataMessages()
            .withDataMessages(dataMessageMapper.entitiesToDtos(dataMessages))
    }

    suspend fun getDataMessagesByProjectId(projectId: String): FcmDataMessages {
        val project = projectRepository.findByProjectId(projectId)

        checkPresence(project, "project_not_found") {
            "Project not found with projectId $projectId"
        }

        val users: List<User> = this.userRepository.findByProjectId(nonNullProjectId(project))
        val dataMessages: MutableSet<DataMessage> = hashSetOf()
        users.flatMapTo(dataMessages) { user ->
            this.dataMessageRepository.findByUserId(nonNullUserId(user))
        }
        return FcmDataMessages()
            .withDataMessages(dataMessageMapper.entitiesToDtos(dataMessages))
    }

    suspend fun checkIfDataMessageExists(dataMessageDto: FcmDataMessageDto, subjectId: String): Boolean {
        val user = this.userRepository.findBySubjectId(subjectId)
        checkPresence(user, "user_not_found") {
            INVALID_SUBJECT_ID_MESSAGE
        }
        val dataMessage = DataMessage.DataMessageBuilder(
            dataMessageMapper.dtoToEntity(dataMessageDto),
        ).user(user).build()

        val dataMessages = this.dataMessageRepository.findByUserId(nonNullUserId(user))
        return dataMessages.contains(dataMessage)
    }

    // TODO : WIP
    @Suppress("UNUSED_PARAMETER")
    fun getFilteredDataMessages(
        type: String?,
        delivered: Boolean?,
        ttlSeconds: Int?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int?,
    ): FcmDataMessages? = null

    suspend fun addDataMessage(
        dataMessageDto: FcmDataMessageDto, subjectId: String, projectId: String,
    ): FcmDataMessageDto {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)

        if (!dataMessageRepository
                .existsByUserIdAndSourceIdAndScheduledTimeAndTtlSeconds(
                    nonNullUserId(user),
                    requireNotNullField(dataMessageDto.sourceId, "Data Message source id"),
                    requireNotNullField(dataMessageDto.scheduledTime, "Data Message scheduled time"),
                    requireNotNullField(dataMessageDto.ttlSeconds, "Data Message ttl seconds"),
                )
        ) {
            val dataMessageSaved = dataMessageRepository.add(
                DataMessage.DataMessageBuilder(dataMessageMapper.dtoToEntity(dataMessageDto)).user(user).build(),
            )
            requireNotNullField(user.usermetrics, "User's user metrics").lastOpened = Instant.now()
            this.userRepository.update(user)
            addDataMessageStateEvent(
                dataMessageSaved,
                MessageState.ADDED,
                requireNotNullField(
                    dataMessageSaved.createdAt, "Data message creation timestamp",
                ).toInstant(),
            )
            this.schedulerService.schedule(dataMessageSaved)
            return dataMessageMapper.entityToDto(dataMessageSaved)
        } else {
            throw AlreadyExistsException(
                "data_message_already_exists",
                "The Data Message Already exists. Please Use update endpoint",
            )
        }
    }

    private fun addDataMessageStateEvent(
        dataMessage: DataMessage, state: MessageState, time: Instant,
    ) {
        val dataMessageStateEvent = DataMessageStateEventDto(
            dataMessage, state, null, time,
        )
        dataMessageStateEventPublisher.post(dataMessageStateEvent)

    }

    suspend fun updateDataMessage(
        dataMessageDto: FcmDataMessageDto, subjectId: String, projectId: String,
    ): FcmDataMessageDto {
        val dmDtoId = dataMessageDto.id ?: throw InvalidNotificationDetailsException(
            "ID must be supplied for updating the data message",
        )
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)
        val dataMessage = this.dataMessageRepository.find(dmDtoId)

        checkPresence(dataMessage, "data_message_not_found") {
            "Data message does not exist. Please create first"
        }

        val newDataMessage = DataMessage.DataMessageBuilder(dataMessage)
            .scheduledTime(dataMessageDto.scheduledTime)
            .sourceId(dataMessageDto.sourceId)
            .ttlSeconds(dataMessageDto.ttlSeconds)
            .user(user)
            .fcmMessageId(dataMessageDto.hashCode().toString())
            .build()

        val dataMessageSaved = this.dataMessageRepository.update(newDataMessage) ?: throw IllegalStateException(
            "Data message cannot be updated",
        )
        addDataMessageStateEvent(
            dataMessageSaved,
            MessageState.UPDATED,
            requireNotNullField(
                dataMessageSaved.updatedAt, "Data message update timestamp",
            ).toInstant(),
        )
        if (!dataMessage.delivered) {
            this.schedulerService.updateScheduled(dataMessageSaved)
        }
        return dataMessageMapper.entityToDto(dataMessageSaved)
    }

    suspend fun removeDataMessagesForUser(projectId: String, subjectId: String) {
        val userId = nonNullUserId(subjectAndProjectExistElseThrow(subjectId, projectId))
        val dataMessages = this.dataMessageRepository.findByUserId(
            userId,
        )
        this.schedulerService.deleteScheduledMultiple(dataMessages)
        this.dataMessageRepository.deleteByUserId(
            userId,
        )
    }

    suspend fun updateDeliveryStatus(fcmMessageId: String, isDelivered: Boolean) {
        val dataMessage = this.dataMessageRepository.findByFcmMessageId(fcmMessageId)
        checkInvalidDetails<InvalidNotificationDetailsException>(
            {
                dataMessage == null
            },
            {
                "Data message with the provided FCM message ID does not exist."
            },
        )

        val newDataMessage = DataMessage.DataMessageBuilder(dataMessage).delivered(isDelivered).build()
        this.dataMessageRepository.update(newDataMessage)
    }

    // TODO: Investigate if data messages/notifications can be marked in the state CANCELLED when deleted.
    suspend fun deleteDataMessageByProjectIdAndSubjectIdAndDataMessageId(
        projectId: String,
        subjectId: String,
        id: Long,
    ) {
        val userId = nonNullUserId(subjectAndProjectExistElseThrow(subjectId, projectId))
        if (dataMessageRepository.existsByIdAndUserId(id, userId)) {
            this.dataMessageRepository.deleteByIdAndUserId(id, userId)
        } else {
            throw InvalidNotificationDetailsException(
                "Data message with the provided ID does not exist.",
            )
        }
    }

    suspend fun removeDataMessagesForUserUsingFcmToken(fcmToken: String) {
        val user = this.userRepository.findByFcmToken(fcmToken)
        if (user == null) {
            throw InvalidUserDetailsException("The user with the given Fcm Token does not exist")
        } else {
            this.dataMessageRepository.deleteByUserId(nonNullUserId(user))
            /*User newUser = user1.setFcmToken("");
          this.userRepository.save(newUser);*/
        }
    }

    suspend fun addDataMessages(
        dataMessageDtos: FcmDataMessages, subjectId: String, projectId: String,
    ): FcmDataMessages {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)
        val dataMessages = dataMessageRepository.findByUserId(nonNullUserId(user))

        val newDataMessages = dataMessageDtos.dataMessages.map { dto ->
            dataMessageMapper.dtoToEntity(dto)
        }.map { dm ->
            DataMessage.DataMessageBuilder(dm).user(user).build()
        }.filter { dataMessage: DataMessage? ->
            !dataMessages.contains(dataMessage)
        }

        val savedDataMessages: List<DataMessage> = newDataMessages.map {
            this.dataMessageRepository.add(it)
        }

        savedDataMessages.forEach { dm ->
            addDataMessageStateEvent(
                dm,
                MessageState.ADDED,
                requireNotNullField(dm.createdAt, "Data message creation timestamp").toInstant(),
            )
        }

        this.schedulerService.scheduleMultiple(savedDataMessages)
        return FcmDataMessages()
            .withDataMessages(dataMessageMapper.entitiesToDtos(savedDataMessages))
    }

    suspend fun subjectAndProjectExistElseThrow(subjectId: String, projectId: String): User {
        val project = this.projectRepository.findByProjectId(projectId)
        if (project == null || project.id == null) {
            throw HttpNotFoundException(
                "project_not_found",
                "Project Id does not exist. Please create a project with the ID first",
            )
        }
        val user: User = this.userRepository.findBySubjectIdAndProjectId(subjectId, project.id!!)!!
        checkPresence(user, "user_not_found") {
            INVALID_SUBJECT_ID_MESSAGE
        }
        return user
    }

    suspend fun getDataMessageByProjectIdAndSubjectIdAndDataMessageId(
        projectId: String, subjectId: String, dataMessageId: Long,
    ): DataMessage {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)
        val dataMessage = dataMessageRepository.findByIdAndUserId(dataMessageId, nonNullUserId(user))
        checkInvalidDetails<InvalidNotificationDetailsException>(
            {
                dataMessage == null
            },
            {
                "The Data message with Id $dataMessageId does not exist in project $projectId for user $subjectId"
            },
        )
        return dataMessage!!
    }

    suspend fun getDataMessageByMessageId(messageId: String): DataMessage {
        val dataMessage = this.dataMessageRepository.findByFcmMessageId(messageId)
        checkInvalidDetails<InvalidNotificationDetailsException>(
            {
                dataMessage == null
            },
            {
                "The Data message with FCM Message Id $messageId does not exist."
            },
        )
        return dataMessage!!
    }

    companion object {
        private const val INVALID_SUBJECT_ID_MESSAGE =
            "The supplied Subject ID is invalid. No user found. Please Create a User First."

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

    }
}
