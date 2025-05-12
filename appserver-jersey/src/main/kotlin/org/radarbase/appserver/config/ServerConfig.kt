package org.radarbase.appserver.config

import java.net.URI

data class ServerConfig(
    /** Base URL to serve data with. This will determine the base path and the port. */
    val baseUri: URI = URI.create("http://0.0.0.0:8090/kafka/"),
)
