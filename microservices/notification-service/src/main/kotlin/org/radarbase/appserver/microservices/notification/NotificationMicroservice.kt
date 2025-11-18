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

package org.radarbase.appserver.microservices.notification

import org.radarbase.appserver.microservices.core.config.Validation
import org.radarbase.appserver.microservices.notification.config.NotificationServiceConfig
import org.radarbase.jersey.GrizzlyServer
import org.radarbase.jersey.config.ConfigLoader
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("org.radarbase.appserver.microservices.notification.NotificationMicroservice")

    logger.info("Starting Notification Service")

    val config = try {
        ConfigLoader.loadConfig<NotificationServiceConfig>(
            listOf(
                "microservices/notification-service/src/main/resources/notification-service.yml",
                "/etc/notification-service/notification-service.yml",
            ),
            args,
        ).withEnv()
    } catch (_: IllegalArgumentException) {
        logger.error("No configuration file (notification-service.yml) found. Exiting...")
        exitProcess(1)
    }

    try {
        (config as Validation).validate()
    } catch (ex: IllegalStateException) {
        logger.error("Invalid configuration: {}", ex.message)
        exitProcess(1)
    }

    ConfigLoader.loadResources(config.resourceConfig, config).run {
        GrizzlyServer(config.server.baseUri, this)
    }.let { server: GrizzlyServer ->
        server.start()
    }
}
