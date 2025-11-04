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

package org.radarbase.appserver.microservices.task.config

import org.radarbase.appserver.microservices.contract.utils.Env.TASK_SERVICE_BASE_URL
import org.radarbase.appserver.microservices.core.config.Validation
import org.radarbase.jersey.config.ConfigLoader.copyEnv
import org.slf4j.LoggerFactory
import java.net.URI

data class TaskServerConfig(
    /** Base URL to serve data with. This will determine the base path and the port. */
    val baseUri: URI = URI.create("http://task-service:9015"),
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
    fun withEnv(): TaskServerConfig = this
        .copyEnv(TASK_SERVICE_BASE_URL) {
            try {
                copy(baseUri = URI.create(it))
            } catch (e: Exception) {
                logger.error("Not a valid url from env APPSERVER_TASK_SERVICE_BASE_URL", e)
                this@TaskServerConfig
            }
        }

    override fun validate() {
        check(baseUri.toString().isNotBlank()) { "Base URL must not be blank." }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(TaskServerConfig::class.java)
    }
}
