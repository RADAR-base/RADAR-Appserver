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

package org.radarbase.appserver.microservices.github.enhancer

import jakarta.inject.Singleton
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.server.validation.ValidationFeature
import org.radarbase.appserver.microservices.core.exception.handler.UnhandledExceptionMapper
import org.radarbase.appserver.microservices.github.config.GithubServiceConfig
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer

class GithubServiceResourceEnhancer(private val config: GithubServiceConfig) : JerseyResourceEnhancer {
    override val packages: Array<String>
        get() = arrayOf(
            "org.radarbase.appserver.microservices.github.api",
        )

    override fun AbstractBinder.enhance() {
        bind(config)
            .to(GithubServiceConfig::class.java)
            .`in`(Singleton::class.java)
    }

    override fun ResourceConfig.enhance() {
        register(ValidationFeature::class.java)
        register(UnhandledExceptionMapper::class.java)
    }

}
