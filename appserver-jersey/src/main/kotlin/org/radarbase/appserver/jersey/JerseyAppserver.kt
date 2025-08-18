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

package org.radarbase.appserver.jersey

import org.radarbase.appserver.jersey.config.AppserverConfig
import org.radarbase.appserver.jersey.config.Validation
import org.radarbase.jersey.GrizzlyServer
import org.radarbase.jersey.config.ConfigLoader
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("org.radarbase.appserver.JerseyAppServerKt")

    logger.info("Starting Jersey Appserver")
    val config = try {
        ConfigLoader.loadConfig<AppserverConfig>(
            listOf(
                "appserver-jersey/src/main/resources/appserver.yml",
                "/etc/appserver-jersey/appserver.yml",
            ),
            args,
        )
    } catch (ex: IllegalArgumentException) {
        logger.error("No configuration file (appserver.yml) was found.")
        exitProcess(1)
    }

    try {
        (config as Validation).validate()
    } catch (ex: IllegalStateException) {
        logger.error("Incomplete configuration: {}", ex.message)
        exitProcess(1)
    }

    ConfigLoader.loadResources(config.resourceConfig, config).run {
        GrizzlyServer(config.server.baseUri, this)
    }.let { server: GrizzlyServer ->
        server.start()
    }
}
