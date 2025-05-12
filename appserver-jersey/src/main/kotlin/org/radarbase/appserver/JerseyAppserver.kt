package org.radarbase.appserver

import org.radarbase.appserver.config.AppserverConfig
import org.radarbase.jersey.config.ConfigLoader
import org.slf4j.LoggerFactory
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val logger = LoggerFactory.getLogger("org.radarbase.appserver.JerseyAppServerKt")

    val config = try {
        ConfigLoader.loadConfig<AppserverConfig>(
            listOf(
                "appserver-jersey/src/main/resources/appserver.yml",
                "/etc/appserver-jersey/appserver.yml",
            ),
            args,
        )
    } catch (ex: IllegalArgumentException) {
        logger.error("No configuration file appserver.yml was found.")
        exitProcess(1)
    }
}
