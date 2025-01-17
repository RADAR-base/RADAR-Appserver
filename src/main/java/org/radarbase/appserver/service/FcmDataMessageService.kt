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

import org.radarbase.appserver.dto.fcm.FcmDataMessageDto
import org.radarbase.appserver.dto.fcm.FcmDataMessages
import org.radarbase.appserver.entity.DataMessage
import org.radarbase.appserver.entity.DataMessage.DataMessageBuilder
import org.radarbase.appserver.entity.User
import org.radarbase.appserver.event.state.MessageState
import org.radarbase.appserver.event.state.dto.DataMessageStateEventDto
import org.radarbase.appserver.exception.AlreadyExistsException
import org.radarbase.appserver.exception.InvalidNotificationDetailsException
import org.radarbase.appserver.exception.InvalidUserDetailsException
import org.radarbase.appserver.exception.NotFoundException
import org.radarbase.appserver.mapper.DataMessageMapper
import org.radarbase.appserver.repository.DataMessageRepository
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
 * [Service] for interacting with the [DataMessage] [jakarta.persistence.Entity]
 * using the [DataMessageRepository].
 *
 * @author yatharthranjan
 */
@Service
@Suppress("unused")
class FcmDataMessageService(
    private val dataMessageRepository: DataMessageRepository,
    private val userRepository: UserRepository,
    private val projectRepository: ProjectRepository,
    private val schedulerService: MessageSchedulerService<DataMessage>,
    private val dataMessageConverter: DataMessageMapper,
    private val dataMessageStateEventPublisher: ApplicationEventPublisher?
) : DataMessageService {

    // TODO Add option to specify a scheduling provider (default will be fcm)
    // TODO: Use strategy pattern for handling data messages for scheduling and adding to database

    @Transactional(readOnly = true)
    fun getAllDataMessages(): FcmDataMessages {
        val dataMessages = dataMessageRepository.findAll()
        return FcmDataMessages()
            .withDataMessages(dataMessageConverter.entitiesToDtos(dataMessages))
    }

    @Transactional(readOnly = true)
    fun getDataMessageById(id: Long): FcmDataMessageDto {
        val dataMessage = dataMessageRepository.findByIdOrNull(id)
        return dataMessageConverter.entityToDto(dataMessage ?: DataMessage())
    }

    @Transactional(readOnly = true)
    fun getDataMessagesBySubjectId(subjectId: String): FcmDataMessages {
        val user = this.userRepository.findBySubjectId(subjectId)
        checkPresence(user) {
            INVALID_SUBJECT_ID_MESSAGE
        }

        val dataMessages = dataMessageRepository.findByUserId(user.id)
        return FcmDataMessages()
            .withDataMessages(dataMessageConverter.entitiesToDtos(dataMessages))
    }

    @Transactional(readOnly = true)
    fun getDataMessagesByProjectIdAndSubjectId(
        projectId: String, subjectId: String
    ): FcmDataMessages {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)

        val dataMessages = dataMessageRepository.findByUserId(user.id)
        return FcmDataMessages()
            .withDataMessages(dataMessageConverter.entitiesToDtos(dataMessages))
    }

    @Transactional(readOnly = true)
    fun getDataMessagesByProjectId(projectId: String): FcmDataMessages {
        val project = projectRepository.findByProjectId(projectId)

        checkPresence(project) {
            "Project not found with projectId $projectId"
        }
        val users: List<User> = this.userRepository.findByProjectId(project.id)
        val dataMessages: MutableSet<DataMessage> = hashSetOf()
        users.flatMapTo(dataMessages) { user -> this.dataMessageRepository.findByUserId(user.id) }
        return FcmDataMessages()
            .withDataMessages(dataMessageConverter.entitiesToDtos(dataMessages))
    }

    @Transactional(readOnly = true)
    fun checkIfDataMessageExists(dataMessageDto: FcmDataMessageDto, subjectId: String?): Boolean {
        val user = this.userRepository.findBySubjectId(subjectId)

        checkPresence(user) {
            INVALID_SUBJECT_ID_MESSAGE
        }

        val dataMessage = DataMessageBuilder(dataMessageConverter.dtoToEntity(dataMessageDto)).user(user).build()

        val dataMessages = this.dataMessageRepository.findByUserId(user.id)
        return dataMessages.contains(dataMessage)
    }

    // TODO : WIP
    @Transactional(readOnly = true)
    fun getFilteredDataMessages(
        type: String?,
        delivered: Boolean,
        ttlSeconds: Int,
        startTime: LocalDateTime?,
        endTime: LocalDateTime?,
        limit: Int
    ): FcmDataMessages? = null

    @Transactional
    fun addDataMessage(
        dataMessageDto: FcmDataMessageDto, subjectId: String?, projectId: String?
    ): FcmDataMessageDto {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)
        if (!dataMessageRepository
                .existsByUserIdAndSourceIdAndScheduledTimeAndTtlSeconds(
                    user.id,
                    dataMessageDto.sourceId,
                    dataMessageDto.scheduledTime,
                    dataMessageDto.ttlSeconds
                )
        ) {
            val dataMessageSaved =
                this.dataMessageRepository.saveAndFlush(
                    DataMessageBuilder(dataMessageConverter.dtoToEntity(dataMessageDto)).user(user).build()
                )
            user.usermetrics!!.lastOpened = Instant.now()
            this.userRepository.save(user)
            addDataMessageStateEvent(
                dataMessageSaved, MessageState.ADDED, dataMessageSaved.createdAt!!.toInstant()
            )
            this.schedulerService.schedule(dataMessageSaved)
            return dataMessageConverter.entityToDto(dataMessageSaved)
        } else {
            throw AlreadyExistsException(
                "The Data Message Already exists. Please Use update endpoint", dataMessageDto
            )
        }
    }

    private fun addDataMessageStateEvent(
        dataMessage: DataMessage, state: MessageState, time: Instant
    ) {
        if (dataMessageStateEventPublisher != null) {
            val dataMessageStateEvent =
                DataMessageStateEventDto(this, dataMessage, state, null, time)
            dataMessageStateEventPublisher.publishEvent(dataMessageStateEvent)
        }
    }

    @Transactional
    fun updateDataMessage(
        dataMessageDto: FcmDataMessageDto, subjectId: String, projectId: String
    ): FcmDataMessageDto {
        checkInvalidDetails<InvalidNotificationDetailsException> ({
            dataMessageDto.id == null
        }, {
            "ID must be supplied for updating the data message"
        })

        val user = subjectAndProjectExistElseThrow(subjectId, projectId)

        val dataMessage =
            this.dataMessageRepository.findByIdOrNull(dataMessageDto.id)

        checkPresence(dataMessage) {
            "Data message does not exist. Please create first"
        }

        val newDataMessage = DataMessageBuilder(dataMessage)
            .scheduledTime(dataMessageDto.scheduledTime)
            .sourceId(dataMessageDto.sourceId)
            .ttlSeconds(dataMessageDto.ttlSeconds)
            .user(user)
            .fcmMessageId(dataMessageDto.hashCode().toString())
            .build()

        val dataMessageSaved = this.dataMessageRepository.saveAndFlush(newDataMessage)
        addDataMessageStateEvent(
            dataMessageSaved, MessageState.UPDATED, dataMessageSaved.updatedAt!!.toInstant()
        )
        if (!dataMessage.delivered) {
            this.schedulerService.updateScheduled(dataMessageSaved)
        }
        return dataMessageConverter.entityToDto(dataMessageSaved)
    }

    @Transactional
    fun removeDataMessagesForUser(projectId: String?, subjectId: String?) {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)

        val dataMessages = this.dataMessageRepository.findByUserId(user.id)
        this.schedulerService.deleteScheduledMultiple(dataMessages)

        this.dataMessageRepository.deleteByUserId(user.id)
    }

    @Transactional
    fun updateDeliveryStatus(fcmMessageId: String?, isDelivered: Boolean) {
        val dataMessage = this.dataMessageRepository.findByFcmMessageId(fcmMessageId)

        checkInvalidDetails<InvalidNotificationDetailsException> ({
            dataMessage == null
        }, {
            "Data message with the provided FCM message ID does not exist."
        })

        val newDataMessage = DataMessageBuilder(dataMessage).delivered(isDelivered).build()
        this.dataMessageRepository.save<DataMessage?>(newDataMessage)

    }

    // TODO: Investigate if data messages/notifications can be marked in the state CANCELLED when deleted.
    @Transactional
    fun deleteDataMessageByProjectIdAndSubjectIdAndDataMessageId(projectId: String?, subjectId: String?, id: Long) {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)

        if (this.dataMessageRepository.existsByIdAndUserId(id, user.id)) {
            this.dataMessageRepository.deleteByIdAndUserId(
                id,
                user.id
            )
        } else {
            throw InvalidNotificationDetailsException(
                "Data message with the provided ID does not exist."
            )
        }
    }

    @Transactional
    fun removeDataMessagesForUserUsingFcmToken(fcmToken: String?) {
        val user = this.userRepository.findByFcmToken(fcmToken)

        if (user == null) {
            throw InvalidUserDetailsException("The user with the given Fcm Token does not exist")
        } else {
            this.dataMessageRepository.deleteByUserId(user.id)
            /*User newUser = user1.setFcmToken("");
          this.userRepository.save(newUser);*/
        }
    }

    @Transactional
    fun addDataMessages(
        dataMessageDtos: FcmDataMessages, subjectId: String, projectId: String
    ): FcmDataMessages {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)
        val dataMessages = dataMessageRepository.findByUserId(user.id)

        val newDataMessages =
            dataMessageDtos.dataMessages.map { dto -> dataMessageConverter.dtoToEntity(dto) }
                .map { dm -> DataMessageBuilder(dm).user(user).build() }
                .filter { dataMessage: DataMessage? -> !dataMessages.contains(dataMessage) }

        val savedDataMessages: List<DataMessage> = this.dataMessageRepository.saveAll(newDataMessages)
        this.dataMessageRepository.flush()

        savedDataMessages.forEach{ dm ->
                addDataMessageStateEvent(
                    dm,
                    MessageState.ADDED,
                    dm.createdAt!!.toInstant()
                )
            }

        this.schedulerService.scheduleMultiple(savedDataMessages)
        return FcmDataMessages()
            .withDataMessages(dataMessageConverter.entitiesToDtos(savedDataMessages))
    }

    fun subjectAndProjectExistElseThrow(subjectId: String?, projectId: String?): User {
        val project = this.projectRepository.findByProjectId(projectId)
        if (project == null || project.id == null) {
            throw NotFoundException(
                "Project Id does not exist. Please create a project with the ID first"
            )
        }

        val user: User = this.userRepository.findBySubjectIdAndProjectId(subjectId, project.id)!!

        checkPresence(user) {
            INVALID_SUBJECT_ID_MESSAGE
        }
        return user
    }

    @Transactional(readOnly = true)
    fun getDataMessageByProjectIdAndSubjectIdAndDataMessageId(
        projectId: String?, subjectId: String?, dataMessageId: Long
    ): DataMessage {
        val user = subjectAndProjectExistElseThrow(subjectId, projectId)

        val dataMessage = dataMessageRepository.findByIdAndUserId(dataMessageId, user.id!!)

        checkInvalidDetails<InvalidNotificationDetailsException> ({
            dataMessage == null
        }, {
            "The Data message with Id $dataMessageId does not exist in project $projectId for user $subjectId"
        })
        return dataMessage!!
    }

    @Transactional(readOnly = true)
    fun getDataMessageByMessageId(messageId: String?): DataMessage {
        val dataMessage = this.dataMessageRepository.findByFcmMessageId(messageId)

        checkInvalidDetails<InvalidNotificationDetailsException> ({
            dataMessage == null
        }, {
            "The Data message with FCM Message Id $messageId does not exist."
        })

        return dataMessage!!
    }

    companion object {
        private const val INVALID_SUBJECT_ID_MESSAGE =
            "The supplied Subject ID is invalid. No user found. Please Create a User First."
    }
}
