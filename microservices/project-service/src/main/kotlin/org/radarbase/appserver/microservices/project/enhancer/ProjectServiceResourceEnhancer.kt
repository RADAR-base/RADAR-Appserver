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

package org.radarbase.appserver.microservices.project.enhancer

import jakarta.inject.Singleton
import org.glassfish.hk2.api.TypeLiteral
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.server.validation.ValidationFeature
import org.radarbase.appserver.microservices.core.dto.ProjectDto
import org.radarbase.appserver.microservices.core.entity.Project
import org.radarbase.appserver.microservices.core.exception.handler.UnhandledExceptionMapper
import org.radarbase.appserver.microservices.core.mapper.Mapper
import org.radarbase.appserver.microservices.core.mapper.ProjectMapper
import org.radarbase.appserver.microservices.core.repository.ProjectRepository
import org.radarbase.appserver.microservices.core.utils.Const.PROJECT_MAPPER
import org.radarbase.appserver.microservices.project.config.ProjectServiceConfig
import org.radarbase.appserver.microservices.project.repository.ProjectRepositoryImpl
import org.radarbase.appserver.microservices.project.service.ProjectService
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.service.AsyncCoroutineService
import org.radarbase.jersey.service.ScopedAsyncCoroutineService

class ProjectServiceResourceEnhancer(private val config: ProjectServiceConfig) : JerseyResourceEnhancer {
    override val packages: Array<String>
        get() = arrayOf(
            "org.radarbase.appserver.microservices.project.api",
        )

    override fun AbstractBinder.enhance() {
        bind(config)
            .to(ProjectServiceConfig::class.java)
            .`in`(Singleton::class.java)

        bind(ProjectService::class.java)
            .to(ProjectService::class.java)
            .`in`(Singleton::class.java)

        bind(ProjectRepositoryImpl::class.java)
            .to(ProjectRepository::class.java)
            .`in`(Singleton::class.java)

        bind(ProjectMapper::class.java)
            .to(object : TypeLiteral<Mapper<ProjectDto, Project>>() {}.type)
            .named(PROJECT_MAPPER)
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
