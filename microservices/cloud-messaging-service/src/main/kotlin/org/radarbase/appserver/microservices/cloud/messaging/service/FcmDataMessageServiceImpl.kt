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
import org.radarbase.appserver.microservices.core.dto.fcm.FcmDataMessageDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmDataMessages
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUserDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUsers
import org.radarbase.appserver.microservices.core.entity.DataMessage
import org.radarbase.appserver.microservices.core.entity.Project
import org.radarbase.appserver.microservices.core.entity.User
import org.radarbase.appserver.microservices.core.event.state.MessageState
import org.radarbase.appserver.microservices.core.event.state.dto.DataMessageStateEventDto
import org.radarbase.appserver.microservices.core.exception.AlreadyExistsException
import org.radarbase.appserver.microservices.core.exception.InvalidNotificationDetailsException
import org.radarbase.appserver.microservices.core.mapper.Mapper
import org.radarbase.appserver.microservices.core.repository.DataMessageRepository
import org.radarbase.appserver.microservices.core.service.FcmDataMessageService
import org.radarbase.appserver.microservices.core.utils.Const.DATA_MESSAGE_MAPPER
import org.radarbase.appserver.microservices.core.utils.Const.USER_MAPPER
import org.radarbase.appserver.microservices.core.utils.checkInvalidDetails
import org.radarbase.appserver.microservices.core.utils.checkPresence
import org.radarbase.appserver.microservices.core.utils.nonNullUserId
import org.radarbase.appserver.microservices.core.utils.requireNotNullField
import java.time.Instant
import java.time.LocalDateTime
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Suppress("unused")
class FcmDataMessageServiceImpl @Inject constructor(
    private val dataMessageRepository: DataMessageRepository,
    private val schedulerService: MessageSchedulerService<DataMessage>,
    private val dataMessageStateEventPublisher: EventBus,
    @field:Named(DATA_MESSAGE_MAPPER) private val dataMessageMapper: Mapper<FcmDataMessageDto, DataMessage>,
    @field:Named(USER_MAPPER) private val userMapper: Mapper<FcmUserDto, User>,
    config: CloudMessagingServiceConfig,
) : FcmDataMessageService {
    private val userServiceUrl = config.contract.user
    private val projectServiceUrl = config.contract.project

    // TODO Add option to specify a scheduling provider (default will be fcm)
    // TODO: Use strategy pattern for handling data messages for scheduling and adding to database

    override suspend fun getAllDataMessages(): FcmDataMessages {
        val dataMessages = dataMessageRepository.findAll()
        return FcmDataMessages(dataMessageMapper.entitiesToDtos(dataMessages).toMutableList())
    }

    override suspend fun getDataMessageById(id: Long): FcmDataMessageDto {
        val dataMessage = dataMessageRepository.find(id)
        return dataMessageMapper.entityToDto(dataMessage ?: DataMessage())
    }

    override suspend fun getDataMessagesBySubjectId(subjectId: String): FcmDataMessages {
        val user: FcmUserDto = deserializeDtoFromContract(
            UserServiceContract.getUserUsingSubjectId(subjectId, userServiceUrl),
        ) {
            "User not found ; user with subject id not found"
        }

        val dataMessages = dataMessageRepository.findByUserId(nonNullUserId(user))
        return FcmDataMessages(
            dataMessageMapper.entitiesToDtos(dataMessages).toMutableList(),
        )
    }

    override suspend fun getDataMessagesByProjectIdAndSubjectId(
        projectId: String,
        subjectId: String,
    ): FcmDataMessages {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)

        val dataMessages = dataMessageRepository.findByUserId(nonNullUserId(user))
        return FcmDataMessages(
            dataMessageMapper.entitiesToDtos(dataMessages).toMutableList(),
        )
    }

    override suspend fun getDataMessagesByProjectId(projectId: String): FcmDataMessages {
        val users: List<FcmUserDto> = deserializeDtoFromContract<ProjectDto>(
            ProjectServiceContract.getProjectUsingProjectId(projectId, projectServiceUrl),
        ) {
            "project_not_found ; Project with projectId $projectId not found"
        }.let { project: ProjectDto ->
            deserializeDtoFromContract<FcmUsers>(
                UserServiceContract.getUsersUsingProjectId(
                    org.radarbase.appserver.microservices.core.utils.nonNullProjectId(
                        project,
                    ),
                    userServiceUrl,
                ),
            ) { "user_not_found ; user with projectId $projectId not found" }.users
        }

        val dataMessages: MutableSet<DataMessage> = hashSetOf()
        users.flatMapTo(dataMessages) { user ->
            this.dataMessageRepository.findByUserId(nonNullUserId(user))
        }
        return FcmDataMessages(
            dataMessageMapper.entitiesToDtos(dataMessages).toMutableList(),
        )
    }

    override suspend fun checkIfDataMessageExists(dataMessageDto: FcmDataMessageDto, subjectId: String): Boolean {
        val user = deserializeDtoFromContract<FcmUserDto>(
            UserServiceContract.getUserUsingSubjectId(subjectId, userServiceUrl),
        ) {
            "user_not_found ; user with subjectId $subjectId not found"
        }

        val dataMessage = DataMessage.DataMessageBuilder(
            dataMessageMapper.dtoToEntity(dataMessageDto),
        ).userId(nonNullUserId(user))
            .subjectId(user.subjectId)
            .projectId(user.projectId)
            .build()

        val dataMessages = this.dataMessageRepository.findByUserId(nonNullUserId(user))
        return dataMessages.contains(dataMessage)
    }

    // TODO : WIP
    @Suppress("UNUSED_PARAMETER")
    override fun getFilteredDataMessages(
        type: String?,
        delivered: Boolean?,
        ttlSeconds: Int?,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int?,
    ): FcmDataMessages? = null

    override suspend fun addDataMessage(
        dataMessageDto: FcmDataMessageDto,
        subjectId: String,
        projectId: String,
    ): FcmDataMessageDto {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)

        val dataMessageExists = checkDataMessageExists(
            dataMessageDto,
            user,
            subjectId,
            projectId,
        )

        if (!dataMessageExists) {
            val savedDataMessage = addDataMessageAndItsStateEvent(dataMessageDto, user)
            this.schedulerService.schedule(savedDataMessage)
            return dataMessageMapper.entityToDto(savedDataMessage)
        } else {
            throw AlreadyExistsException(
                "data_message_already_exists",
                "The Data Message Already exists. Please Use update endpoint",
            )
        }
    }

    override suspend fun updateDataMessage(
        dataMessageDto: FcmDataMessageDto,
        subjectId: String,
        projectId: String,
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
            .userId(user.id)
            .subjectId(subjectId)
            .projectId(projectId)
            .scheduledTime(dataMessageDto.scheduledTime)
            .sourceId(dataMessageDto.sourceId)
            .ttlSeconds(dataMessageDto.ttlSeconds)
            .fcmMessageId(dataMessageDto.hashCode().toString())
            .build()

        val savedDataMessage = this.dataMessageRepository.update(newDataMessage) ?: throw IllegalStateException(
            "Data message cannot be updated. Database returned null.",
        )
        addDataMessageStateEvent(
            savedDataMessage,
            MessageState.UPDATED,
            requireNotNullField(
                savedDataMessage.updatedAt,
                "Data message update timestamp",
            ).toInstant(),
        )
        if (!dataMessage.delivered) {
            this.schedulerService.updateScheduled(savedDataMessage)
        }
        return dataMessageMapper.entityToDto(savedDataMessage)
    }

    override suspend fun removeDataMessagesForUser(projectId: String, subjectId: String) {
        val userId = nonNullUserId(subjectAndProjectExistElseThrow(subjectId, projectId))
        val dataMessages = this.dataMessageRepository.findByUserId(
            userId,
        )
        this.schedulerService.deleteScheduledMultiple(dataMessages)
        this.dataMessageRepository.deleteByUserId(
            userId,
        )
    }

    override suspend fun updateDeliveryStatus(fcmMessageId: String, isDelivered: Boolean) {
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
    override suspend fun deleteDataMessageByProjectIdAndSubjectIdAndDataMessageId(
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

    override suspend fun removeDataMessagesForUserUsingFcmToken(fcmToken: String) {
        val user = deserializeDtoFromContract<FcmUserDto>(
            UserServiceContract.getUserUsingFcmToken(fcmToken, userServiceUrl),
        ) {
            "invalid_user_details ; The user with the given Fcm Token does not exist"
        }
        this.dataMessageRepository.deleteByUserId(nonNullUserId(user))
        /*User newUser = user1.setFcmToken("");
      this.userRepository.save(newUser);*/

    }

    override suspend fun addDataMessages(
        dataMessageDtos: FcmDataMessages,
        subjectId: String,
        projectId: String,
    ): FcmDataMessages {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)
        val dataMessages = dataMessageRepository.findByUserId(nonNullUserId(user))

        val newDataMessages = dataMessageDtos.dataMessages.map { dto ->
            dataMessageMapper.dtoToEntity(dto)
        }.map { dm ->
            DataMessage.DataMessageBuilder(dm)
                .userId(user.id)
                .subjectId(subjectId)
                .projectId(projectId)
                .build()
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
        return FcmDataMessages(
            dataMessageMapper.entitiesToDtos(dataMessages).toMutableList(),
        )
    }

    suspend fun subjectAndProjectExistElseThrow(subjectId: String, projectId: String): User {
        return deserializeDtoFromContract<ProjectDto>(
            ProjectServiceContract.getProjectUsingProjectId(
                projectId,
                projectServiceUrl,
            ),
        ) {
            "project_not_found ; Project Id $projectId does not exist. Please create a project with the ID first."
        }.let { project ->
            UserServiceContract.getUserUsingProjectIdAndSubjectId(
                projectId,
                subjectId,
                userServiceUrl,
            ).let {
                deserializeDtoFromContract<FcmUserDto>(
                    it,
                ) {
                    "user_not_found ; $INVALID_SUBJECT_ID_MESSAGE"
                }
            }.let {
                userMapper.dtoToEntity(it)
            }
        }
    }

    override suspend fun getDataMessageByProjectIdAndSubjectIdAndDataMessageId(
        projectId: String,
        subjectId: String,
        dataMessageId: Long,
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

    override suspend fun getDataMessageByMessageId(messageId: String): DataMessage {
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

    private suspend fun addDataMessageAndItsStateEvent(
        dataMessageDto: FcmDataMessageDto,
        user: User,
    ): DataMessage {
        val projectId = requireNotNullField(user.projectId, "User's Project Id")
        val subjectId = requireNotNullField(user.subjectId, "User's Subject Id")

        val savedDataMessage = this.dataMessageRepository.add(
            DataMessage.DataMessageBuilder(dataMessageMapper.dtoToEntity(dataMessageDto))
                .userId(user.id)
                .subjectId(subjectId)
                .projectId(projectId)
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

        addDataMessageStateEvent(
            savedDataMessage,
            MessageState.ADDED,
            requireNotNullField(savedDataMessage.createdAt, "Data message creation timestamp").toInstant(),
        )
        return savedDataMessage
    }

    private fun addDataMessageStateEvent(
        dataMessage: DataMessage,
        state: MessageState,
        time: Instant,
    ) {
        val dataMessageStateEvent = DataMessageStateEventDto(
            dataMessage,
            state,
            null,
            time,
        )
        dataMessageStateEventPublisher.post(dataMessageStateEvent)
    }

    private suspend fun checkDataMessageExists(
        dataMessageDto: FcmDataMessageDto,
        user: User,
        subjectId: String,
        projectId: String,
    ): Boolean {
        return dataMessageRepository
            .existsByUserIdAndSourceIdAndScheduledTimeAndTtlSeconds(
                nonNullUserId(user),
                requireNotNullField(dataMessageDto.sourceId, "Data Message source id"),
                requireNotNullField(dataMessageDto.scheduledTime, "Data Message scheduled time"),
                requireNotNullField(dataMessageDto.ttlSeconds, "Data Message ttl seconds"),
            )
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

        private fun nonNullProjectId(project: Project): Long = checkNotNull(project.id) {
            "User id cannot be null"
        }
    }
}
