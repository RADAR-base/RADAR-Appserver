package org.radarbase.appserver.config

import java.net.URI

data class ServerConfig(
    /** Base URL to serve data with. This will determine the base path and the port. */
    val baseUri: URI = URI.create("http://0.0.0.0:8090/kafka/"),
) : Validation {
    override fun validate() {
        check(baseUri.toString().isNotBlank()) { "Base URL must not be blank." }
    }
}
