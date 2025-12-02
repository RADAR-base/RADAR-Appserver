package org.radarbase.appserver.microservices.cloud.messaging.config

import org.radarbase.appserver.microservices.contract.utils.Env.CLOUD_MESSAGING_SERVICE_BASE_URL
import org.radarbase.appserver.microservices.core.config.Validation
import org.radarbase.jersey.config.ConfigLoader.copyEnv
import org.slf4j.LoggerFactory
import java.net.URI

data class CloudMessagingServerConfig(
    /** Base URL to serve data with. This will determine the base path and the port. */
    val baseUri: URI = URI.create("http://cloud-messaging-service:9015"),
    /**
     * Maximum time in seconds to wait for a request to complete.
     * This timeout is applied to the co-routine context, not to the Grizzly server.
     */
    val requestTimeout: Int = 30,
    /**
     * Whether JMX should be enabled. Disable if not needed, for higher performance.
     */
    val isJmxEnabled: Boolean = false,
) : Validation {
    fun withEnv(): CloudMessagingServerConfig = this
        .copyEnv(CLOUD_MESSAGING_SERVICE_BASE_URL) {
            try {
                copy(baseUri = URI.create(it))
            } catch (e: Exception) {
                logger.error("Not a valid url from env APPSERVER_CLOUD_MESSAGING_SERVICE_BASE_URL", e)
                this@CloudMessagingServerConfig
            }
        }

    override fun validate() {
        check(baseUri.toString().isNotBlank()) { "Base URL must not be blank." }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(CloudMessagingServerConfig::class.java)
    }
}
