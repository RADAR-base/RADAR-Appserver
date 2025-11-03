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

package org.radarbase.appserver.microservices.gateway.enhancer

import jakarta.inject.Singleton
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.server.validation.ValidationFeature
import org.radarbase.appserver.microservices.core.exception.handler.UnhandledExceptionMapper
import org.radarbase.appserver.microservices.gateway.config.GatewayConfig
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer

class GatewayServiceResourceEnhancer(
    private val config: GatewayConfig,
): JerseyResourceEnhancer {
    override val packages: Array<String>
        get() = arrayOf(
            "org.radarbase.appserver.microservices.gateway",
        )

    override fun AbstractBinder.enhance() {
        bind(config)
            .to(GatewayConfig::class.java)
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
}
