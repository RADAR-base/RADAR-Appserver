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

package org.radarbase.appserver.microservices.cloud.messaging.utils.mail

import jakarta.mail.Authenticator
import jakarta.mail.PasswordAuthentication
import jakarta.mail.Session
import java.util.Properties

object MailSessionFactory {
    fun createMailSession(
        smtpHost: String,
        smtpPort: Int,
        username: String? = null,
        password: String? = null,
        enableStartTls: Boolean = true,
        connectionTimeout: Int = 10000,
        readTimeout: Int = 10000,
        additionalProps: Map<String, String> = emptyMap(),
    ): Session {
        val props = Properties().apply {
            put("mail.smtp.host", smtpHost)
            put("mail.smtp.port", smtpPort.toString())
            put("mail.smtp.auth", (username != null && password != null).toString())
            put("mail.smtp.starttls.enable", enableStartTls.toString())
            put("mail.smtp.connectiontimeout", connectionTimeout.toString())
            put("mail.smtp.timeout", readTimeout.toString())
            additionalProps.forEach { (k, v) -> put(k, v) }
        }

        val auth = if (username != null && password != null) {
            object : Authenticator() {
                override fun getPasswordAuthentication() = PasswordAuthentication(username, password)
            }
        } else null

        return if (auth != null) Session.getInstance(props, auth) else Session.getInstance(props)
    }
}
