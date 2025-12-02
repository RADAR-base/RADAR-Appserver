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

package org.radarbase.appserver.microservices.cloud.messaging.enhancer

import com.google.common.eventbus.EventBus
import jakarta.inject.Singleton
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.quartz.JobListener
import org.radarbase.appserver.microservices.cloud.messaging.config.CloudMessagingServiceConfig
import org.radarbase.appserver.microservices.cloud.messaging.event.listener.MessageStateEventListener
import org.radarbase.appserver.microservices.cloud.messaging.event.listener.quartz.QuartzMessageJobListener
import org.radarbase.appserver.microservices.core.config.CoreEventBusConfig
import org.radarbase.appserver.microservices.core.config.CoreFcmServerConfig
import org.radarbase.appserver.microservices.core.config.CoreSchedulerConfig
import org.radarbase.appserver.microservices.core.factory.eventBus.EventBusFactory
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.service.AsyncCoroutineService
import org.radarbase.jersey.service.ScopedAsyncCoroutineService

class CloudMessagingServiceResourceEnhancer(private val config: CloudMessagingServiceConfig) : JerseyResourceEnhancer {
    override val packages: Array<String>
        get() = arrayOf(
            "org.radarbase.appserver.microservices.cloud.messaging.api",
        )

    override fun AbstractBinder.enhance() {
        bind(config)
            .to(CloudMessagingServiceConfig::class.java)
            .`in`(Singleton::class.java)

        bind(config.fcm)
            .to(CoreFcmServerConfig::class.java)
            .`in`(Singleton::class.java)

        bind(config.eventBus)
            .to(CoreEventBusConfig::class.java)
            .`in`(Singleton::class.java)

        bind(config.quartz)
            .to(CoreSchedulerConfig::class.java)
            .`in`(Singleton::class.java)

        bindFactory(EventBusFactory::class.java)
            .to(EventBus::class.java)
            .`in`(Singleton::class.java)

        bind(MessageStateEventListener::class.java)
            .to(MessageStateEventListener::class.java)
            .`in`(Singleton::class.java)

        bind(ScopedAsyncCoroutineService::class.java)
            .to(AsyncCoroutineService::class.java)
            .`in`(Singleton::class.java)

        bind(QuartzMessageJobListener::class.java)
            .to(JobListener::class.java)
            .`in`(Singleton::class.java)
    }
}
