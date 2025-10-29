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

package org.radarbase.appserver.microservices.gateway

import org.radarbase.appserver.microservices.gateway.config.GatewayConfig
import org.radarbase.jersey.GrizzlyServer
import org.radarbase.jersey.config.ConfigLoader
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("org.radarbase.appserver.microservices.project.GatewayService")

    logger.info("Starting Gateway Service")

    val config = try {
        ConfigLoader.loadConfig<GatewayConfig>(
            listOf(
                "microservices/gateway-service/src/main/resources/gateway-service.yml",
                "/etc/project-service/gateway-service.yml",
            ),
            args,
        )
    } catch (_: IllegalArgumentException) {
        logger.info("No configuration file (gateway-service.yml) found. Exiting...")
        exitProcess(1)
    }

    ConfigLoader.loadResources(config.resourceConfig, config).run {
        GrizzlyServer(config.server.baseUri, this)
    }.let { server: GrizzlyServer ->
        server.start()
    }
}
