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

package org.radarbase.appserver.microservices.cloud.messaging.enhancer.factory

import org.radarbase.appserver.microservices.cloud.messaging.config.CloudMessagingServiceConfig
import org.radarbase.appserver.microservices.cloud.messaging.config.EmailConfig
import org.radarbase.appserver.microservices.cloud.messaging.enhancer.CloudMessagingServiceResourceEnhancer
import org.radarbase.appserver.microservices.cloud.messaging.service.transmitter.EmailTransmitter
import org.radarbase.appserver.microservices.cloud.messaging.utils.mail.MailSessionFactory
import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.jersey.enhancer.Enhancers
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.hibernate.config.DatabaseConfig
import org.radarbase.jersey.hibernate.config.HibernateResourceEnhancer

class CloudMessagingServiceResourceEnhancerFactory(private val config: CloudMessagingServiceConfig) : EnhancerFactory {
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

        val emailConfig: EmailConfig? = if (config.email.enabled) {
            EmailConfig(
                enabled = config.email.enabled,
                smtpHost = config.email.smtpHost,
                smtpPort = config.email.smtpPort,
                smtpUser = config.email.smtpUser,
                smtpPassword = config.email.smtpPassword,
                fromAddress = config.email.fromAddress,
                connectTimeout = config.email.connectTimeout,
                readTimeout = config.email.readTimeout,
                enableTls = config.email.enableTls,
            )
        } else null

        val emailTransmitter = emailConfig?.let {
            MailSessionFactory.createMailSession(
                it.smtpHost,
                it.smtpPort,
                it.smtpUser,
                it.smtpPassword,
                it.enableTls,
                it.connectTimeout,
                it.readTimeout,
            )
        }?.let { session ->
            EmailTransmitter(
                session,
                emailConfig.fromAddress,
                config,
            )
        }

        return listOf(
            CloudMessagingServiceResourceEnhancer(config, emailTransmitter),
            HibernateResourceEnhancer(dbConfig),
            Enhancers.health,
            Enhancers.exception,
        )
    }
}
