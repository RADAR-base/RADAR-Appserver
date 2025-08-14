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

package org.radarbase.appserver.jersey.enhancer

import com.google.common.eventbus.EventBus
import com.google.firebase.FirebaseOptions
import jakarta.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import org.glassfish.hk2.api.TypeLiteral
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.server.validation.ValidationFeature
import org.quartz.JobListener
import org.quartz.Scheduler
import org.quartz.SchedulerListener
import org.radarbase.appserver.jersey.config.AppserverConfig
import org.radarbase.appserver.jersey.config.FcmServerConfig
import org.radarbase.appserver.jersey.dto.ProjectDto
import org.radarbase.appserver.jersey.dto.fcm.FcmDataMessageDto
import org.radarbase.appserver.jersey.dto.fcm.FcmNotificationDto
import org.radarbase.appserver.jersey.dto.fcm.FcmUserDto
import org.radarbase.appserver.jersey.entity.DataMessage
import org.radarbase.appserver.jersey.entity.Notification
import org.radarbase.appserver.jersey.entity.Project
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.event.listener.MessageStateEventListener
import org.radarbase.appserver.jersey.event.listener.TaskStateEventListener
import org.radarbase.appserver.jersey.event.listener.quartz.QuartzMessageJobListener
import org.radarbase.appserver.jersey.event.listener.quartz.QuartzMessageSchedulerListener
import org.radarbase.appserver.jersey.exception.handler.UnhandledExceptionMapper
import org.radarbase.appserver.jersey.factory.coroutines.SchedulerScopedCoroutine
import org.radarbase.appserver.jersey.factory.event.EventBusFactory
import org.radarbase.appserver.jersey.factory.fcm.FcmSenderFactory
import org.radarbase.appserver.jersey.factory.fcm.FirebaseOptionsFactory
import org.radarbase.appserver.jersey.factory.quartz.QuartzSchedulerFactory
import org.radarbase.appserver.jersey.fcm.downstream.FcmSender
import org.radarbase.appserver.jersey.mapper.Mapper
import org.radarbase.appserver.jersey.mapper.ProjectMapper
import org.radarbase.appserver.jersey.mapper.UserMapper
import org.radarbase.appserver.jersey.repository.ProjectRepository
import org.radarbase.appserver.jersey.repository.UserRepository
import org.radarbase.appserver.jersey.repository.impl.ProjectRepositoryImpl
import org.radarbase.appserver.jersey.repository.impl.UserRepositoryImpl
import org.radarbase.appserver.jersey.service.ProjectService
import org.radarbase.appserver.jersey.service.UserService
import org.radarbase.appserver.jersey.service.github.GithubClient
import org.radarbase.appserver.jersey.service.github.GithubService
import org.radarbase.appserver.jersey.service.github.protocol.ProtocolFetcherStrategy
import org.radarbase.appserver.jersey.service.github.protocol.ProtocolGenerator
import org.radarbase.appserver.jersey.service.github.protocol.impl.DefaultProtocolGenerator
import org.radarbase.appserver.jersey.service.github.protocol.impl.GithubProtocolFetcherStrategy
import org.radarbase.appserver.jersey.service.questionnaire_schedule.QuestionnaireScheduleGeneratorService
import org.radarbase.appserver.jersey.service.questionnaire_schedule.ScheduleGeneratorService
import org.radarbase.appserver.jersey.service.scheduling.SchedulingService
import org.radarbase.appserver.jersey.factory.scheduling.SchedulingServiceFactory
import org.radarbase.appserver.jersey.mapper.DataMessageMapper
import org.radarbase.appserver.jersey.mapper.NotificationMapper
import org.radarbase.appserver.jersey.repository.DataMessageRepository
import org.radarbase.appserver.jersey.repository.DataMessageStateEventRepository
import org.radarbase.appserver.jersey.repository.NotificationRepository
import org.radarbase.appserver.jersey.repository.NotificationStateEventRepository
import org.radarbase.appserver.jersey.repository.TaskRepository
import org.radarbase.appserver.jersey.repository.TaskStateEventRepository
import org.radarbase.appserver.jersey.repository.impl.DataMessageRepositoryImpl
import org.radarbase.appserver.jersey.repository.impl.DataMessageStateEventRepositoryImpl
import org.radarbase.appserver.jersey.repository.impl.NotificationRepositoryImpl
import org.radarbase.appserver.jersey.repository.impl.NotificationStateEventRepositoryImpl
import org.radarbase.appserver.jersey.repository.impl.TaskRepositoryImpl
import org.radarbase.appserver.jersey.repository.impl.TaskStateEventRepositoryImpl
import org.radarbase.appserver.jersey.service.DataMessageStateEventService
import org.radarbase.appserver.jersey.service.FcmDataMessageService
import org.radarbase.appserver.jersey.service.FcmNotificationService
import org.radarbase.appserver.jersey.service.NotificationStateEventService
import org.radarbase.appserver.jersey.service.TaskService
import org.radarbase.appserver.jersey.service.TaskStateEventService
import org.radarbase.appserver.jersey.service.quartz.QuartzNamingStrategy
import org.radarbase.appserver.jersey.service.quartz.SchedulerService
import org.radarbase.appserver.jersey.service.quartz.SchedulerServiceImpl
import org.radarbase.appserver.jersey.service.quartz.SimpleQuartzNamingStrategy
import org.radarbase.appserver.jersey.service.questionnaire_schedule.MessageSchedulerService
import org.radarbase.appserver.jersey.service.questionnaire_schedule.QuestionnaireScheduleService
import org.radarbase.appserver.jersey.service.transmitter.DataMessageTransmitter
import org.radarbase.appserver.jersey.service.transmitter.FcmTransmitter
import org.radarbase.appserver.jersey.service.transmitter.NotificationTransmitter
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer

class AppserverResourceEnhancer(private val config: AppserverConfig) : JerseyResourceEnhancer {

    override val packages: Array<String>
        get() = arrayOf(
            "org.radarbase.appserver.jersey.resource",
        )

    override val classes: Array<Class<*>>
        get() = super.classes

    override fun AbstractBinder.enhance() {
        bind(config)
            .to(AppserverConfig::class.java)
            .`in`(Singleton::class.java)

        bind(config.fcm)
            .to(FcmServerConfig::class.java)
            .`in`(Singleton::class.java)

        bind(ProjectRepositoryImpl::class.java)
            .to(ProjectRepository::class.java)
            .`in`(Singleton::class.java)

        bind(UserRepositoryImpl::class.java)
            .to(UserRepository::class.java)
            .`in`(Singleton::class.java)

        bind(DataMessageRepositoryImpl::class.java)
            .to(DataMessageRepository::class.java)
            .`in`(Singleton::class.java)

        bind(NotificationRepositoryImpl::class.java)
            .to(NotificationRepository::class.java)
            .`in`(Singleton::class.java)

        bind(TaskRepositoryImpl::class.java)
            .to(TaskRepository::class.java)
            .`in`(Singleton::class.java)

        bind(DataMessageStateEventRepositoryImpl::class.java)
            .to(DataMessageStateEventRepository::class.java)
            .`in`(Singleton::class.java)

        bind(NotificationStateEventRepositoryImpl::class.java)
            .to(NotificationStateEventRepository::class.java)
            .`in`(Singleton::class.java)

        bind(TaskStateEventRepositoryImpl::class.java)
            .to(TaskStateEventRepository::class.java)
            .`in`(Singleton::class.java)

        bind(TaskStateEventService::class.java)
            .to(TaskStateEventService::class.java)
            .`in`(Singleton::class.java)

        bind(ProjectMapper::class.java)
            .to(object : TypeLiteral<Mapper<ProjectDto, Project>>() {}.type)
            .named(PROJECT_MAPPER)
            .`in`(Singleton::class.java)

        bind(UserMapper::class.java)
            .to(object : TypeLiteral<Mapper<FcmUserDto, User>>() {}.type)
            .named(USER_MAPPER)
            .`in`(Singleton::class.java)

        bind(DataMessageMapper::class.java)
            .to(object : TypeLiteral<Mapper<FcmDataMessageDto, DataMessage>>() {}.type)
            .named(DATA_MESSAGE_MAPPER)
            .`in`(Singleton::class.java)

        bind(NotificationMapper::class.java)
            .to(object : TypeLiteral<Mapper<FcmNotificationDto, Notification>>() {}.type)
            .named(NOTIFICATION_MAPPER)
            .`in`(Singleton::class.java)

        bind(ProjectService::class.java)
            .to(ProjectService::class.java)
            .`in`(Singleton::class.java)

        bind(UserService::class.java)
            .to(UserService::class.java)
            .`in`(Singleton::class.java)

        bind(TaskService::class.java)
            .to(TaskService::class.java)
            .`in`(Singleton::class.java)

        bind(FcmDataMessageService::class.java)
            .to(FcmDataMessageService::class.java)
            .`in`(Singleton::class.java)

        bind(FcmNotificationService::class.java)
            .to(FcmNotificationService::class.java)
            .`in`(Singleton::class.java)

        bind(GithubClient::class.java)
            .to(GithubClient::class.java)
            .`in`(Singleton::class.java)

        bind(GithubService::class.java)
            .to(GithubService::class.java)
            .`in`(Singleton::class.java)

        bind(GithubProtocolFetcherStrategy::class.java)
            .to(ProtocolFetcherStrategy::class.java)
            .`in`(Singleton::class.java)

        bind(DefaultProtocolGenerator::class.java)
            .to(ProtocolGenerator::class.java)
            .`in`(Singleton::class.java)

        bind(QuestionnaireScheduleGeneratorService::class.java)
            .to(ScheduleGeneratorService::class.java)
            .`in`(Singleton::class.java)

        bind(QuestionnaireScheduleService::class.java)
            .to(QuestionnaireScheduleService::class.java)
            .`in`(Singleton::class.java)

        bind(QuartzMessageSchedulerListener::class.java)
            .to(SchedulerListener::class.java)
            .`in`(Singleton::class.java)

        bind(QuartzMessageJobListener::class.java)
            .to(JobListener::class.java)
            .`in`(Singleton::class.java)

        bind(SchedulerServiceImpl::class.java)
            .to(SchedulerService::class.java)
            .`in`(Singleton::class.java)

        bind(MessageSchedulerService::class.java)
            .to(object : TypeLiteral<MessageSchedulerService<Notification>>() {}.type)
            .`in`(Singleton::class.java)

        bind(MessageSchedulerService::class.java)
            .to(object : TypeLiteral<MessageSchedulerService<DataMessage>>() {}.type)
            .`in`(Singleton::class.java)

        bind(SimpleQuartzNamingStrategy::class.java)
            .to(QuartzNamingStrategy::class.java)
            .`in`(Singleton::class.java)

        bind(FcmTransmitter::class.java)
            .to(DataMessageTransmitter::class.java)
            .to(NotificationTransmitter::class.java)
            .to(FcmTransmitter::class.java)
            .`in`(Singleton::class.java)

        bind(TaskStateEventListener::class.java)
            .to(TaskStateEventListener::class.java)
            .`in`(Singleton::class.java)

        bind(MessageStateEventListener::class.java)
            .to(MessageStateEventListener::class.java)
            .`in`(Singleton::class.java)

        bind(DataMessageStateEventService::class.java)
            .to(DataMessageStateEventService::class.java)
            .`in`(Singleton::class.java)

        bind(NotificationStateEventService::class.java)
            .to(NotificationStateEventService::class.java)
            .`in`(Singleton::class.java)

        bindFactory(SchedulerScopedCoroutine::class.java)
            .to(CoroutineScope::class.java)
            .`in`(Singleton::class.java)

        bindFactory(EventBusFactory::class.java)
            .to(EventBus::class.java)
            .`in`(Singleton::class.java)

        bindFactory(QuartzSchedulerFactory::class.java)
            .to(Scheduler::class.java)
            .`in`(Singleton::class.java)

        bindFactory(SchedulingServiceFactory::class.java)
            .to(SchedulingService::class.java)
            .`in`(Singleton::class.java)

        bindFactory(FirebaseOptionsFactory::class.java)
            .to(FirebaseOptions::class.java)
            .`in`(Singleton::class.java)

        bindFactory(FcmSenderFactory::class.java)
            .to(FcmSender::class.java)
            .`in`(Singleton::class.java)

        bind(UnverifiedProjectService::class.java)
            .to(org.radarbase.jersey.service.ProjectService::class.java)
            .`in`(Singleton::class.java)
    }

    override fun ResourceConfig.enhance() {
        register(ValidationFeature::class.java)
        register(UnhandledExceptionMapper::class.java)
    }

    /** Project service without validation of the project's existence. */
    class UnverifiedProjectService : org.radarbase.jersey.service.ProjectService {
        override suspend fun ensureOrganization(organizationId: String) = Unit

        override suspend fun ensureProject(projectId: String) = Unit

        override suspend fun ensureSubject(projectId: String, userId: String) = Unit

        override suspend fun listProjects(organizationId: String): List<String> = emptyList()

        override suspend fun projectOrganization(projectId: String): String = "main"
    }

    companion object {
        const val PROJECT_MAPPER = "project_mapper"
        const val USER_MAPPER = "user_mapper"
        const val DATA_MESSAGE_MAPPER = "data_message_mapper"
        const val NOTIFICATION_MAPPER = "notification_mapper"
    }
}
