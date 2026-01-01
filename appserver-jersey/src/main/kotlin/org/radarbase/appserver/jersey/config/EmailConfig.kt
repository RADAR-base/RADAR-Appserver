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

package org.radarbase.appserver.jersey.config

import org.radarbase.jersey.config.ConfigLoader.copyEnv

data class EmailConfig(
    val enabled: Boolean = false,
    val smtpHost: String = "smtp.gmail.com",
    val smtpPort: Int = 587,
    val smtpUser: String? = null,
    val smtpPassword: String? = null,
    val fromAddress: String = "radar-base@kcl.ac.uk",
    val connectTimeout: Int = 10000,
    val readTimeout: Int = 10000,
    val enableTls: Boolean = true,
) {
    fun withEnv() = this.
        copyEnv("RADAR_APPSERVER_NOTIFICATION_EMAIL_ENABLED") {
            copy(enabled = it.toBoolean())
        }
        .copyEnv("RADAR_APPSERVER_NOTIFICATION_EMAIL_FROM") {
            copy(fromAddress = it)
        }
        .copyEnv("RADAR_APPSERVER_EMAIL_SMTP_HOST") {
            copy(smtpHost = it)
        }
        .copyEnv("RADAR_APPSERVER_EMAIL_SMTP_PORT") {
            copy(smtpPort = it.toInt())
        }
        .copyEnv("RADAR_APPSERVER_EMAIL_SMTP_USERNAME") {
            copy(smtpUser = it)
        }
        .copyEnv("RADAR_APPSERVER_EMAIL_SMTP_PASSWORD") {
            copy(smtpPassword = it)
        }
        .copyEnv("RADAR_APPSERVER_EMAIL_FROM_ADDRESS") {
            copy(fromAddress = it)
        }
        .copyEnv("RADAR_APPSERVER_EMAIL_CONNECT_TIMEOUT") {
            copy(connectTimeout = it.toIntOrNull() ?: 10000)
        }
        .copyEnv("RADAR_APPSERVER_EMAIL_READ_TIMEOUT") {
            copy(readTimeout = it.toIntOrNull() ?: 10000)
        }
        .copyEnv("RADAR_APPSERVER_EMAIL_TLS_ENABLED") {
            copy(enableTls = it.toBoolean())
        }
}
