package org.radarbase.appserver.jersey.enhancer

import jakarta.inject.Singleton
import org.glassfish.hk2.api.TypeLiteral
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.glassfish.jersey.server.ResourceConfig
import org.glassfish.jersey.server.validation.ValidationFeature
import org.radarbase.appserver.jersey.config.AppserverConfig
import org.radarbase.appserver.jersey.dto.ProjectDto
import org.radarbase.appserver.jersey.entity.Project
import org.radarbase.appserver.jersey.exception.handler.UnhandledExceptionMapper
import org.radarbase.appserver.jersey.mapper.Mapper
import org.radarbase.appserver.jersey.mapper.ProjectMapper
import org.radarbase.appserver.jersey.repository.ProjectRepository
import org.radarbase.appserver.jersey.repository.impl.ProjectRepositoryImpl
import org.radarbase.appserver.jersey.service.ProjectService
import org.radarbase.appserver.jersey.service.github.GithubClient
import org.radarbase.appserver.jersey.service.github.GithubService
import org.radarbase.appserver.jersey.service.github.protocol.ProtocolFetcherStrategy
import org.radarbase.appserver.jersey.service.github.protocol.ProtocolGenerator
import org.radarbase.appserver.jersey.service.github.protocol.impl.DefaultProtocolGenerator
import org.radarbase.appserver.jersey.service.github.protocol.impl.GithubProtocolFetcherStrategy
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer

class AppserverResourceEnhancer(private val config: AppserverConfig) : JerseyResourceEnhancer {

    override val packages: Array<String>
        get() = arrayOf(
            "org.radarbase.appserver.jersey.resource",
        )

    override val classes: Array<Class<*>>
        get() = super.classes

    override fun AbstractBinder.enhance() {
        bind(config)
            .to(AppserverConfig::class.java)
            .`in`(Singleton::class.java)

        bind(ProjectRepositoryImpl::class.java)
            .to(ProjectRepository::class.java)
            .`in`(Singleton::class.java)

        bind(ProjectMapper::class.java)
            .to(object : TypeLiteral<Mapper<ProjectDto, Project>>() {}.type)
            .named(PROJECT_MAPPER)
            .`in`(Singleton::class.java)

        bind(ProjectService::class.java)
            .to(ProjectService::class.java)
            .`in`(Singleton::class.java)

        bind(GithubClient::class.java)
            .to(GithubClient::class.java)
            .`in`(Singleton::class.java)

        bind(GithubService::class.java)
            .to(GithubService::class.java)
            .`in`(Singleton::class.java)

        bind(GithubProtocolFetcherStrategy::class.java)
            .to(ProtocolFetcherStrategy::class.java)
            .`in`(Singleton::class.java)

        bind(DefaultProtocolGenerator::class.java)
            .to(ProtocolGenerator::class.java)
            .`in`(Singleton::class.java)
    }

    override fun ResourceConfig.enhance() {
        register(ValidationFeature::class.java)
        register(UnhandledExceptionMapper::class.java)
    }

    companion object {
        const val PROJECT_MAPPER = "project_mapper"
    }
}
