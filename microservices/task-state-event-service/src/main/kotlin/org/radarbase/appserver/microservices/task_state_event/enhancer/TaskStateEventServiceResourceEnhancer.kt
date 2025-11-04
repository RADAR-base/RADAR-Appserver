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

package org.radarbase.appserver.microservices.task_state_event.enhancer

import com.google.common.eventbus.EventBus
import jakarta.inject.Singleton
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.server.validation.ValidationFeature
import org.radarbase.appserver.microservices.core.config.CoreEventBusConfig
import org.radarbase.appserver.microservices.core.exception.handler.UnhandledExceptionMapper
import org.radarbase.appserver.microservices.core.factory.eventBus.EventBusFactory
import org.radarbase.appserver.microservices.core.repository.TaskStateEventRepository
import org.radarbase.appserver.microservices.core.service.TaskStateEventService
import org.radarbase.appserver.microservices.task_state_event.application.event.EventBusStartupListener
import org.radarbase.appserver.microservices.task_state_event.config.TaskStateEventServiceConfig
import org.radarbase.appserver.microservices.task_state_event.repository.TaskStateEventRepositoryImpl
import org.radarbase.appserver.microservices.task_state_event.service.TaskStateEventServiceImpl
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.service.AsyncCoroutineService
import org.radarbase.jersey.service.ScopedAsyncCoroutineService

class TaskStateEventServiceResourceEnhancer(private val config: TaskStateEventServiceConfig) : JerseyResourceEnhancer {
    override val packages: Array<String>
        get() = arrayOf(
            "org.radarbase.appserver.microservices.task_state_event.api",
        )

    override fun AbstractBinder.enhance() {
        bind(config)
            .to(TaskStateEventServiceConfig::class.java)
            .`in`(Singleton::class.java)

        bindFactory(EventBusFactory::class.java)
            .to(EventBus::class.java)
            .`in`(Singleton::class.java)

        bind(config.eventBus)
            .to(CoreEventBusConfig::class.java)
            .`in`(Singleton::class.java)

        bind(TaskStateEventServiceImpl::class.java)
            .to(TaskStateEventService::class.java)
            .`in`(Singleton::class.java)

        bind(TaskStateEventRepositoryImpl::class.java)
            .to(TaskStateEventRepository::class.java)
            .`in`(Singleton::class.java)

        bind(ScopedAsyncCoroutineService::class.java)
            .to(AsyncCoroutineService::class.java)
            .`in`(Singleton::class.java)

    }

    override fun ResourceConfig.enhance() {
        register(ValidationFeature::class.java)
        register(UnhandledExceptionMapper::class.java)
        register(EventBusStartupListener::class.java)
    }
}
