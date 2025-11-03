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

package org.radarbase.appserver.microservices.protocol.enhancer

import jakarta.inject.Singleton
import org.glassfish.hk2.api.TypeLiteral
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.server.validation.ValidationFeature
import org.radarbase.appserver.microservices.core.dto.ProjectDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUserDto
import org.radarbase.appserver.microservices.core.dto.fcm.FcmUsers
import org.radarbase.appserver.microservices.core.entity.Project
import org.radarbase.appserver.microservices.core.entity.User
import org.radarbase.appserver.microservices.core.exception.handler.UnhandledExceptionMapper
import org.radarbase.appserver.microservices.core.mapper.Mapper
import org.radarbase.appserver.microservices.core.mapper.ProjectMapper
import org.radarbase.appserver.microservices.core.mapper.UserMapper
import org.radarbase.appserver.microservices.core.service.github.protocol.ProtocolFetcherStrategy
import org.radarbase.appserver.microservices.core.service.github.protocol.ProtocolGenerator
import org.radarbase.appserver.microservices.protocol.config.ProtocolServiceConfig
import org.radarbase.appserver.microservices.protocol.service.DefaultProtocolGenerator
import org.radarbase.appserver.microservices.protocol.service.GithubProtocolFetcherStrategy
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.service.AsyncCoroutineService
import org.radarbase.jersey.service.ScopedAsyncCoroutineService

class ProtocolServiceResourceEnhancer(private val config: ProtocolServiceConfig) : JerseyResourceEnhancer {
    override val packages: Array<String>
        get() = arrayOf(
            "org.radarbase.appserver.microservices.protocol.api",
        )

    override fun AbstractBinder.enhance() {
        bind(config)
            .to(ProtocolServiceConfig::class.java)
            .`in`(Singleton::class.java)

        bind(DefaultProtocolGenerator::class.java)
            .to(ProtocolGenerator::class.java)
            .`in`(Singleton::class.java)

        bind(GithubProtocolFetcherStrategy::class.java)
            .to(ProtocolFetcherStrategy::class.java)
            .`in`(Singleton::class.java)

        bind(UserMapper::class.java)
            .to(object : TypeLiteral<Mapper<FcmUserDto, User>>() {}.type)
            .`in`(Singleton::class.java)

        bind(ProjectMapper::class.java)
            .to(object : TypeLiteral<Mapper<ProjectDto, Project>>() {}.type)
            .`in`(Singleton::class.java)

        bind(ScopedAsyncCoroutineService::class.java)
            .to(AsyncCoroutineService::class.java)
            .`in`(Singleton::class.java)
    }

    override fun ResourceConfig.enhance() {
        register(ValidationFeature::class.java)
        register(UnhandledExceptionMapper::class.java)
    }
}
