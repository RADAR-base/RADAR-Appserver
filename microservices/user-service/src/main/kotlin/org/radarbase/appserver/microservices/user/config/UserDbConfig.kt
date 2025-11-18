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

package org.radarbase.appserver.microservices.user.config

import org.radarbase.appserver.microservices.contract.utils.Env.USER_HIBERNATE_DIALECT
import org.radarbase.appserver.microservices.contract.utils.Env.USER_JDBC_DRIVER
import org.radarbase.appserver.microservices.contract.utils.Env.USER_JDBC_PASSWORD
import org.radarbase.appserver.microservices.contract.utils.Env.USER_JDBC_URL
import org.radarbase.appserver.microservices.contract.utils.Env.USER_JDBC_USERNAME
import org.radarbase.appserver.microservices.core.config.CoreLiquibaseConfig
import org.radarbase.appserver.microservices.core.config.Validation
import org.radarbase.appserver.microservices.core.entity.Project
import org.radarbase.appserver.microservices.core.entity.User
import org.radarbase.appserver.microservices.core.entity.UserMetrics
import org.radarbase.appserver.microservices.core.utils.checkInvalidDetails
import org.radarbase.jersey.config.ConfigLoader.copyEnv

data class UserDbConfig(
    val classes: List<String> = listOf(
        User::class.qualifiedName!!,
        Project::class.qualifiedName!!,
        UserMetrics::class.qualifiedName!!,
    ),
    val jdbcDriver: String = "org.postgresql.Driver",
    val jdbcUrl: String = "jdbc:postgresql://localhost:5432/appserver_user",
    val username: String = "radar",
    val password: String = "radar",
    val hibernateDialect: String = "org.hibernate.dialect.PostgreSQLDialect",
    val additionalProperties: Map<String, String> = emptyMap(),
    val liquibase: CoreLiquibaseConfig = CoreLiquibaseConfig(),
) : Validation {
    fun withEnv(): UserDbConfig = this
        .copyEnv(USER_JDBC_URL) {
            copy(jdbcUrl = it)
        }
        .copyEnv(USER_JDBC_USERNAME) {
            copy(username = it)
        }
        .copyEnv(USER_JDBC_PASSWORD) {
            copy(password = it)
        }
        .copyEnv(USER_HIBERNATE_DIALECT) {
            copy(hibernateDialect = it)
        }
        .copyEnv(USER_JDBC_DRIVER) {
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
