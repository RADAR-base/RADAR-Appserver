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

package org.radarbase.appserver.microservices.notification.enhancer

import com.google.common.eventbus.EventBus
import com.google.firebase.FirebaseOptions
import jakarta.inject.Singleton
import org.glassfish.hk2.api.TypeLiteral
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.server.validation.ValidationFeature
import org.radarbase.appserver.microservices.core.config.CoreEventBusConfig
import org.radarbase.appserver.microservices.core.config.CoreFcmServerConfig
import org.radarbase.appserver.microservices.core.dto.fcm.FcmNotificationDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUserDto
import org.radarbase.appserver.microservices.core.entity.Notification
import org.radarbase.appserver.microservices.core.entity.User
import org.radarbase.appserver.microservices.core.exception.handler.UnhandledExceptionMapper
import org.radarbase.appserver.microservices.core.factory.eventBus.EventBusFactory
import org.radarbase.appserver.microservices.core.factory.fcm.FcmSenderFactory
import org.radarbase.appserver.microservices.core.factory.fcm.FirebaseOptionsFactory
import org.radarbase.appserver.microservices.core.fcm.downstream.FcmSender
import org.radarbase.appserver.microservices.core.mapper.Mapper
import org.radarbase.appserver.microservices.core.mapper.NotificationMapper
import org.radarbase.appserver.microservices.core.mapper.UserMapper
import org.radarbase.appserver.microservices.core.repository.NotificationRepository
import org.radarbase.appserver.microservices.core.repository.NotificationStateEventRepository
import org.radarbase.appserver.microservices.core.service.FcmNotificationService
import org.radarbase.appserver.microservices.core.service.NotificationStateEventService
import org.radarbase.appserver.microservices.core.service.questionnaire.schedule.MessageSchedulerService
import org.radarbase.appserver.microservices.core.utils.Const.NOTIFICATION_MAPPER
import org.radarbase.appserver.microservices.core.utils.Const.USER_MAPPER
import org.radarbase.appserver.microservices.notification.application.event.EventBusStartupListener
import org.radarbase.appserver.microservices.notification.config.NotificationServiceConfig
import org.radarbase.appserver.microservices.notification.event.listener.NotificationStateEventListener
import org.radarbase.appserver.microservices.notification.repository.NotificationRepositoryImpl
import org.radarbase.appserver.microservices.notification.repository.NotificationStateEventRepositoryImpl
import org.radarbase.appserver.microservices.notification.service.FcmNotificationServiceImpl
import org.radarbase.appserver.microservices.notification.service.NotificationStateEventServiceImpl
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.service.AsyncCoroutineService
import org.radarbase.jersey.service.ScopedAsyncCoroutineService

class NotificationServiceResourceEnhancer(private val config: NotificationServiceConfig) : JerseyResourceEnhancer {
    override val packages: Array<String>
        get() = arrayOf(
            "org.radarbase.appserver.microservices.notification.api",
        )

    override fun AbstractBinder.enhance() {
        bind(config)
            .to(NotificationServiceConfig::class.java)
            .`in`(Singleton::class.java)

        bind(config.fcm)
            .to(CoreFcmServerConfig::class.java)
            .`in`(Singleton::class.java)

        bind(config.eventBus)
            .to(CoreEventBusConfig::class.java)
            .`in`(Singleton::class.java)

        bindFactory(EventBusFactory::class.java)
            .to(EventBus::class.java)
            .`in`(Singleton::class.java)

        bind(UserMapper::class.java)
            .to(object : TypeLiteral<Mapper<FcmUserDto, User>>() {}.type)
            .named(USER_MAPPER)
            .`in`(Singleton::class.java)

        bind(MessageSchedulerService::class.java)
            .to(object : TypeLiteral<MessageSchedulerService<Notification>>() {}.type)
            .`in`(Singleton::class.java)

        bind(ScopedAsyncCoroutineService::class.java)
            .to(AsyncCoroutineService::class.java)
            .`in`(Singleton::class.java)

        bind(NotificationStateEventRepositoryImpl::class.java)
            .to(NotificationStateEventRepository::class.java)
            .`in`(Singleton::class.java)

        bind(NotificationStateEventServiceImpl::class.java)
            .to(NotificationStateEventService::class.java)
            .`in`(Singleton::class.java)

        bind(NotificationRepositoryImpl::class.java)
            .to(NotificationRepository::class.java)
            .`in`(Singleton::class.java)

        bind(FcmNotificationServiceImpl::class.java)
            .to(FcmNotificationService::class.java)
            .`in`(Singleton::class.java)

        bind(NotificationStateEventListener::class.java)
            .to(NotificationStateEventListener::class.java)
            .`in`(Singleton::class.java)

        bind(NotificationMapper::class.java)
            .to(object : TypeLiteral<Mapper<FcmNotificationDto, Notification>>() {}.type)
            .named(NOTIFICATION_MAPPER)
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
        register(EventBusStartupListener::class.java)
    }
}
