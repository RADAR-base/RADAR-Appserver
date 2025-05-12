package org.radarbase.appserver.enhancer.factory

import org.radarbase.appserver.config.AppserverConfig
import org.radarbase.appserver.enhancer.AppserverResourceEnhancer
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.auth.MPConfig
import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.jersey.enhancer.Enhancers
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer
import org.radarbase.jersey.hibernate.config.DatabaseConfig
import org.radarbase.jersey.hibernate.config.HibernateResourceEnhancer

class ManagementPortalEnhancerFactory(private val config: AppserverConfig) : EnhancerFactory {
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
            managedClasses = listOf(),
            url = config.db.jdbcUrl,
            driver = config.db.jdbcDriver,
            user = config.db.username,
            password = config.db.password,
            dialect = config.db.hibernateDialect
        )

        return listOf(
            AppserverResourceEnhancer(config),
            Enhancers.radar(authConfig),
            Enhancers.managementPortal(authConfig),
            HibernateResourceEnhancer(dbConfig),
            Enhancers.health,
            Enhancers.exception,
        )
    }
}
