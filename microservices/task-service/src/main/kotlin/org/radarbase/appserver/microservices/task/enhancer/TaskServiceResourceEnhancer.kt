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

package org.radarbase.appserver.microservices.task.enhancer

import com.google.common.eventbus.EventBus
import jakarta.inject.Singleton
import org.glassfish.hk2.api.TypeLiteral
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.server.validation.ValidationFeature
import org.radarbase.appserver.jersey.event.listener.TaskStateEventListener
import org.radarbase.appserver.jersey.service.quartz.QuartzNamingStrategy
import org.radarbase.appserver.jersey.service.quartz.SimpleQuartzNamingStrategy
import org.radarbase.appserver.jersey.service.questionnaire.schedule.QuestionnaireScheduleGeneratorService
import org.radarbase.appserver.jersey.service.questionnaire.schedule.ScheduleGeneratorService
import org.radarbase.appserver.microservices.core.config.CoreEventBusConfig
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUserDto
import org.radarbase.appserver.microservices.core.entity.User
import org.radarbase.appserver.microservices.core.exception.handler.UnhandledExceptionMapper
import org.radarbase.appserver.microservices.core.factory.eventBus.EventBusFactory
import org.radarbase.appserver.microservices.core.mapper.Mapper
import org.radarbase.appserver.microservices.core.mapper.UserMapper
import org.radarbase.appserver.microservices.core.repository.TaskRepository
import org.radarbase.appserver.microservices.core.repository.TaskStateEventRepository
import org.radarbase.appserver.microservices.core.service.TaskService
import org.radarbase.appserver.microservices.core.service.TaskStateEventService
import org.radarbase.appserver.microservices.core.utils.Const.USER_MAPPER
import org.radarbase.appserver.microservices.task.application.event.EventBusStartupListener
import org.radarbase.appserver.microservices.task.config.TaskServiceConfig
import org.radarbase.appserver.microservices.task.factory.scheduling.SchedulingServiceFactory
import org.radarbase.appserver.microservices.task.repository.TaskRepositoryImpl
import org.radarbase.appserver.microservices.task.repository.TaskStateEventRepositoryImpl
import org.radarbase.appserver.microservices.task.service.TaskServiceImpl
import org.radarbase.appserver.microservices.task.service.TaskStateEventServiceImpl
import org.radarbase.appserver.microservices.task.service.questionnaire.schedule.QuestionnaireScheduleService
import org.radarbase.appserver.microservices.task.service.scheduling.SchedulingService
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.service.AsyncCoroutineService
import org.radarbase.jersey.service.ScopedAsyncCoroutineService

class TaskServiceResourceEnhancer(private val config: TaskServiceConfig) : JerseyResourceEnhancer {
    override val packages: Array<String>
        get() = arrayOf(
            "org.radarbase.appserver.microservices.task.api",
        )

    override fun AbstractBinder.enhance() {
        bind(config)
            .to(TaskServiceConfig::class.java)
            .`in`(Singleton::class.java)

        bind(config.eventBus)
            .to(CoreEventBusConfig::class.java)
            .`in`(Singleton::class.java)

        bindFactory(EventBusFactory::class.java)
            .to(EventBus::class.java)
            .`in`(Singleton::class.java)

        bind(TaskStateEventRepositoryImpl::class.java)
            .to(TaskStateEventRepository::class.java)
            .`in`(Singleton::class.java)

        bind(TaskStateEventListener::class.java)
            .to(TaskStateEventListener::class.java)
            .`in`(Singleton::class.java)

        bind(TaskStateEventServiceImpl::class.java)
            .to(TaskStateEventService::class.java)
            .`in`(Singleton::class.java)

        bind(TaskRepositoryImpl::class.java)
            .to(TaskRepository::class.java)
            .`in`(Singleton::class.java)

        bind(UserMapper::class.java)
            .to(object : TypeLiteral<Mapper<FcmUserDto, User>>() {}.type)
            .named(USER_MAPPER)
            .`in`(Singleton::class.java)

        bind(TaskServiceImpl::class.java)
            .to(TaskService::class.java)
            .`in`(Singleton::class.java)

        bind(ScopedAsyncCoroutineService::class.java)
            .to(AsyncCoroutineService::class.java)
            .`in`(Singleton::class.java)

        bindFactory(SchedulingServiceFactory::class.java)
            .to(SchedulingService::class.java)
            .`in`(Singleton::class.java)

        bind(QuestionnaireScheduleService::class.java)
            .to(QuestionnaireScheduleService::class.java)
            .`in`(Singleton::class.java)

        bind(QuestionnaireScheduleGeneratorService::class.java)
            .to(ScheduleGeneratorService::class.java)
            .`in`(Singleton::class.java)

        bind(SimpleQuartzNamingStrategy::class.java)
            .to(QuartzNamingStrategy::class.java)
            .`in`(Singleton::class.java)

    }

    override fun ResourceConfig.enhance() {
        register(ValidationFeature::class.java)
        register(UnhandledExceptionMapper::class.java)
        register(EventBusStartupListener::class.java)
    }
}
