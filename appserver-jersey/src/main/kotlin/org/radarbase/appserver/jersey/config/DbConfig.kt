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

import org.radarbase.appserver.jersey.entity.DataMessage
import org.radarbase.appserver.jersey.entity.DataMessageStateEvent
import org.radarbase.appserver.jersey.entity.Notification
import org.radarbase.appserver.jersey.entity.NotificationStateEvent
import org.radarbase.appserver.jersey.entity.Project
import org.radarbase.appserver.jersey.entity.Task
import org.radarbase.appserver.jersey.entity.TaskStateEvent
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.entity.UserMetrics
import org.radarbase.appserver.jersey.utils.checkInvalidDetails
import org.radarbase.jersey.config.ConfigLoader.copyEnv

data class DbConfig(
    val classes: List<String> = listOf(
        Project::class.qualifiedName!!,
        User::class.qualifiedName!!,
        UserMetrics::class.qualifiedName!!,
        Task::class.qualifiedName!!,
        Notification::class.qualifiedName!!,
        DataMessage::class.qualifiedName!!,
        TaskStateEvent::class.qualifiedName!!,
        NotificationStateEvent::class.qualifiedName!!,
        DataMessageStateEvent::class.qualifiedName!!,
    ),
    val jdbcDriver: String = "org.postgresql.Driver",
    val jdbcUrl: String = "jdbc:postgresql://localhost:5432/appserver",
    val username: String = "radar",
    val password: String = "radar",
    val hibernateDialect: String = "org.hibernate.dialect.PostgreSQLDialect",
    val additionalProperties: Map<String, String> = emptyMap(),
    val liquibase: LiquibaseConfig = LiquibaseConfig(),
) : Validation {
    fun withEnv(): DbConfig = this
        .copyEnv("APPSERVER_JDBC_URL") {
            copy(jdbcUrl = it)
        }
        .copyEnv("APPSERVER_JDBC_USERNAME") {
            copy(username = it)
        }
        .copyEnv("APPSERVER_JDBC_PASSWORD") {
            copy(password = it)
        }
        .copyEnv("APPSERVER_HIBERNATE_DIALECT") {
            copy(hibernateDialect = it)
        }
        .copyEnv("APPSERVER_JDBC_DRIVER") {
            copy(jdbcDriver = it)
        }

    override fun validate() {
        checkInvalidDetails<IllegalStateException>(
            {
                jdbcDriver.isBlank() || jdbcUrl.isBlank()
            },
            {
                "JDBC driver and URL must not be null or empty"
            },
        )
    }
}
