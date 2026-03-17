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

package org.radarbase.appserver.microservices.protocol.enhancer.factory

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.radarbase.appserver.microservices.protocol.config.ProtocolServiceConfig
import org.radarbase.appserver.microservices.protocol.enhancer.ProtocolServiceResourceEnhancer
import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.jersey.enhancer.Enhancers
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer

class ProtocolServiceResourceEnhancerFactory(private val config: ProtocolServiceConfig) : EnhancerFactory {
    override fun createEnhancers(): List<JerseyResourceEnhancer> {
        val openApi = OpenAPI().apply {
            info = Info().apply {
                title = "RADAR-Appserver Protocol Service"
                description = "Manages questionnaire protocols and scheduling in the RADAR platform"
                version = "2.4.3"
            }
        }

        return listOf(
            ProtocolServiceResourceEnhancer(config),
            Enhancers.swagger(openApi),
            Enhancers.health,
            Enhancers.exception,
        )
    }
}
