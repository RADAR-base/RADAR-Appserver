package org.radarbase.appserver.config

import org.radarbase.jersey.enhancer.EnhancerFactory

data class AppserverConfig (
    val resourceConfig: Class<out EnhancerFactory>,
    val auth: AuthConfig = AuthConfig(),
    val fcm: FcmConfig = FcmConfig(),
    val github: GithubConfig = GithubConfig(),
    val quartz: SchedulerConfig = SchedulerConfig(),
)
