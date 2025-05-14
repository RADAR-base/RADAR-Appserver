package org.radarbase.appserver.enhancer

import jakarta.inject.Singleton
import org.glassfish.hk2.api.TypeLiteral
import org.glassfish.jersey.internal.inject.AbstractBinder
import org.radarbase.appserver.config.AppserverConfig
import org.radarbase.appserver.dto.ProjectDto
import org.radarbase.appserver.entity.Project
import org.radarbase.appserver.mapper.Mapper
import org.radarbase.appserver.mapper.ProjectMapper
import org.radarbase.appserver.repository.ProjectRepository
import org.radarbase.appserver.repository.impl.ProjectRepositoryImpl
import org.radarbase.appserver.service.ProjectService
import org.radarbase.jersey.enhancer.JerseyResourceEnhancer

class AppserverResourceEnhancer(private val config: AppserverConfig) : JerseyResourceEnhancer {

    override val packages: Array<String>
        get() = arrayOf(
            "org.radarbase.appserver.resource",
        )

    override val classes: Array<Class<*>>
        get() = super.classes

    override fun AbstractBinder.enhance() {
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
    }

    companion object {
        const val PROJECT_MAPPER = "project_mapper"
    }
}
