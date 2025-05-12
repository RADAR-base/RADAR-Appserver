package org.radarbase.appserver.enhancer.factory

import io.ktor.http.content.EntityTagVersion
import org.radarbase.appserver.config.AppserverConfig
import org.radarbase.appserver.enhancer.AppserverResourceEnhancer
import org.radarbase.jersey.auth.AuthConfig
import org.radarbase.jersey.auth.MPConfig
import org.radarbase.jersey.enhancer.EnhancerFactory
import org.radarbase.jersey.enhancer.Enhancers
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer

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

        return listOf(
            AppserverResourceEnhancer(config),
            Enhancers.radar(authConfig),
            Enhancers.managementPortal(authConfig),
            Enhancers.health,
            Enhancers.exception,
        )
    }
}
