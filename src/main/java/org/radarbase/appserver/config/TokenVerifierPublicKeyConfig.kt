/*
 *
 *  *
 *  *  * Copyright 2018 King's College London
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *  *
 *  *
 *
 */

package org.radarbase.appserver.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.slf4j.LoggerFactory
import java.io.File
import java.net.URI
import java.net.URL
import java.util.*

class TokenVerifierPublicKeyConfig {
    var publicKeyEndpoints: MutableList<URI> = LinkedList()
    var resourceName: String? = null

    companion object {
        private val log = LoggerFactory.getLogger(TokenVerifierPublicKeyConfig::class.java)
        const val LOCATION_ENV = "RADAR_IS_CONFIG_LOCATION"
        private const val CONFIG_FILE_NAME = "radar-is.yml"

        /**
         * Read the configuration from file. This method will first check if the environment variable
         * `RADAR_IS_CONFIG_LOCATION` is set. If not set, it will look for a file called
         * `radar_is.yml` on the classpath. The configuration will be kept in a static field,
         * so subsequent calls to this method will return the same object.
         *
         * @return The initialized configuration object based on the contents of the configuration file
         * @throws RuntimeException If there is any problem loading the configuration
         */
        fun readFromFileOrClasspath(): TokenVerifierPublicKeyConfig {
            val customLocation = System.getenv(LOCATION_ENV)
            val configFile: URL = if (customLocation != null) {
                log.info("$LOCATION_ENV environment variable set, loading config from {}", customLocation)
                try {
                    File(customLocation).toURI().toURL()
                } catch (ex: Exception) {
                    throw RuntimeException(ex)
                }
            } else {
                log.info("$LOCATION_ENV environment variable not set, looking for it on the classpath")
                val resource = Thread.currentThread().contextClassLoader.getResource(CONFIG_FILE_NAME)
                    ?: throw RuntimeException("Cannot find $CONFIG_FILE_NAME file in classpath.")
                resource
            }

            log.info("Config file found at {}", configFile.path)

            val mapper = ObjectMapper(YAMLFactory())
            return try {
                configFile.openStream().use { stream ->
                    mapper.readValue(stream, TokenVerifierPublicKeyConfig::class.java)
                }
            } catch (ex: Exception) {
                throw RuntimeException(ex)
            }
        }
    }
}
