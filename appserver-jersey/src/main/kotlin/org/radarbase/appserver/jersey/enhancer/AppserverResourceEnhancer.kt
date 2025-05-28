package org.radarbase.appserver.jersey.enhancer

import com.google.common.eventbus.EventBus
import com.google.firebase.FirebaseOptions
import jakarta.inject.Singleton
import org.glassfish.hk2.api.TypeLiteral
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.server.validation.ValidationFeature
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
import org.radarbase.appserver.jersey.factory.event.EventBusFactory
import org.radarbase.appserver.jersey.factory.fcm.FcmSenderFactory
import org.radarbase.appserver.jersey.factory.fcm.FirebaseOptionsFactory
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
import org.radarbase.appserver.jersey.service.DataMessageService
import org.radarbase.appserver.jersey.service.FcmDataMessageService
import org.radarbase.appserver.jersey.service.FcmNotificationService
import org.radarbase.appserver.jersey.service.TaskService
import org.radarbase.appserver.jersey.service.quartz.QuartzNamingStrategy
import org.radarbase.appserver.jersey.service.quartz.SchedulerServiceImpl
import org.radarbase.appserver.jersey.service.quartz.SimpleQuartzNamingStrategy
import org.radarbase.appserver.jersey.service.questionnaire_schedule.MessageSchedulerService
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

        bind(DataMessageRepository::class.java)
            .to(DataMessageRepositoryImpl::class.java)
            .`in`(Singleton::class.java)

        bind(NotificationRepository::class.java)
            .to(NotificationRepositoryImpl::class.java)
            .`in`(Singleton::class.java)

        bind(TaskRepository::class.java)
            .to(TaskRepositoryImpl::class.java)
            .`in`(Singleton::class.java)

        bind(DataMessageStateEventRepository::class.java)
            .to(DataMessageStateEventRepositoryImpl::class.java)
            .`in`(Singleton::class.java)

        bind(NotificationStateEventRepository::class.java)
            .to(NotificationStateEventRepositoryImpl::class.java)
            .`in`(Singleton::class.java)

        bind(TaskStateEventRepository::class.java)
            .to(TaskStateEventRepositoryImpl::class.java)
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

        bind(QuartzMessageSchedulerListener::class.java)
            .to(QuartzMessageSchedulerListener::class.java)
            .`in`(Singleton::class.java)

        bind(QuartzMessageJobListener::class.java)
            .to(QuartzMessageJobListener::class.java)
            .`in`(Singleton::class.java)

        bind(MessageSchedulerService::class.java)
            .to(MessageSchedulerService::class.java)
            .`in`(Singleton::class.java)

        bind(SimpleQuartzNamingStrategy::class.java)
            .to(QuartzNamingStrategy::class.java)
            .`in`(Singleton::class.java)

        bind(SchedulerServiceImpl::class.java)
            .to(SchedulingService::class.java)
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

        bindFactory(EventBusFactory::class.java)
            .to(EventBus::class.java)
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
    }

    override fun ResourceConfig.enhance() {
        register(ValidationFeature::class.java)
        register(UnhandledExceptionMapper::class.java)
    }

    companion object {
        const val PROJECT_MAPPER = "project_mapper"
        const val USER_MAPPER = "user_mapper"
        const val DATA_MESSAGE_MAPPER = "data_message_mapper"
        const val NOTIFICATION_MAPPER = "notification_mapper"
    }
}
