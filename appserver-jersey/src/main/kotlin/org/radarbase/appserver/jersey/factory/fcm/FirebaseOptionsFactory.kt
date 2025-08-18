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

package org.radarbase.appserver.jersey.factory.fcm

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseOptions
import jakarta.inject.Inject
import org.radarbase.appserver.jersey.config.FcmServerConfig
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.util.Base64
import java.util.function.Supplier

class FirebaseOptionsFactory @Inject constructor(
    private val serverConfig: FcmServerConfig,
) : Supplier<FirebaseOptions> {
    override fun get(): FirebaseOptions {
        var googleCredentials: GoogleCredentials? = null

        if (serverConfig.credentials != null) {
            try {
                // read base64 encoded value directly
                val decodedCredentials = Base64.getDecoder().decode(serverConfig.credentials)
                ByteArrayInputStream(decodedCredentials).use { input ->
                    googleCredentials = GoogleCredentials.fromStream(input)
                }
            } catch (ex: IllegalArgumentException) {
                logger.error("Cannot load credentials from fcmserver.credentials", ex)
            }
        }

        if (googleCredentials == null) {
            // read from a path specified with environment variable GOOGLE_APPLICATION_CREDENTIALS
            googleCredentials = GoogleCredentials.getApplicationDefault()
        }
        return FirebaseOptions.builder()
            .setCredentials(googleCredentials)
            .build()
    }

    companion object {
        private val logger = LoggerFactory.getLogger(FirebaseOptionsFactory::class.java)
    }
}
