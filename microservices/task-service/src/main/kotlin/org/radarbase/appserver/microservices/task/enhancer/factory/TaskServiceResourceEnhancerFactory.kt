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

package org.radarbase.appserver.microservices.task.enhancer.factory

import org.radarbase.appserver.microservices.task.config.TaskServiceConfig
import org.radarbase.appserver.microservices.task.enhancer.TaskStateEventServiceResourceEnhancer
import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.jersey.enhancer.Enhancers
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.hibernate.config.DatabaseConfig
import org.radarbase.jersey.hibernate.config.HibernateResourceEnhancer

class TaskServiceResourceEnhancerFactory(private val config: TaskServiceConfig) : EnhancerFactory {
    override fun createEnhancers(): List<JerseyResourceEnhancer> {
        val dbConfig = DatabaseConfig(
            managedClasses = config.db.classes,
            url = config.db.jdbcUrl,
            driver = config.db.jdbcDriver,
            user = config.db.username,
            password = config.db.password,
            dialect = config.db.hibernateDialect,
            properties = config.db.additionalProperties,
            liquibase = org.radarbase.jersey.hibernate.config.LiquibaseConfig(
                enable = config.db.liquibase.enabled,
            ),
        )

        return listOf(
            TaskStateEventServiceResourceEnhancer(config),
            HibernateResourceEnhancer(dbConfig),
            Enhancers.health,
            Enhancers.exception,
        )
    }
}
