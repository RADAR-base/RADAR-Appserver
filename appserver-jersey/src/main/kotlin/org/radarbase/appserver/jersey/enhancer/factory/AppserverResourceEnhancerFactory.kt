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

package org.radarbase.appserver.jersey.enhancer.factory

import org.radarbase.appserver.jersey.config.AppserverConfig
import org.radarbase.appserver.jersey.config.EmailConfig
import org.radarbase.appserver.jersey.enhancer.AppserverResourceEnhancer
import org.radarbase.appserver.jersey.service.transmitter.EmailTransmitter
import org.radarbase.appserver.jersey.utils.mail.MailSessionFactory
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.auth.MPConfig
import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.jersey.enhancer.Enhancers
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.hibernate.config.DatabaseConfig
import org.radarbase.jersey.hibernate.config.HibernateResourceEnhancer

class AppserverResourceEnhancerFactory(private val config: AppserverConfig) : EnhancerFactory {
    override fun createEnhancers(): List<JerseyResourceEnhancer> {
        val authConfig = AuthConfig(
            managementPortal = MPConfig(
                url = config.auth.managementPortalUrl,
            ),
            jwtResourceName = config.auth.resourceName,
            jwtIssuer = config.auth.issuer,
            jwksUrls = config.auth.publicKeyUrls ?: emptyList(),
        )

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
                changelogs = config.db.liquibase.changelogs,
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

        val openApi = OpenAPI().apply {
            info = Info().apply {
                title = "RADAR-Appserver"
                description = "General purpose application server for the RADAR platform"
                version = "2.4.3"
            }
        }

        return listOf(
            AppserverResourceEnhancer(config, emailTransmitter),
            Enhancers.radar(authConfig),
            Enhancers.managementPortal(authConfig),
            HibernateResourceEnhancer(dbConfig),
            Enhancers.swagger(openApi),
            Enhancers.health,
            Enhancers.exception,
        )
    }
}
