package org.radarbase.appserver.jersey.config

import org.radarbase.appserver.jersey.config.github.GithubConfig
import org.radarbase.appserver.jersey.config.questionnaire.QuestionnaireProtocolConfig
import org.radarbase.jersey.enhancer.EnhancerFactory

data class AppserverConfig(
    val resourceConfig: Class<out EnhancerFactory>,
    val server: ServerConfig,
    val auth: AuthConfig = AuthConfig(),
    val fcm: FcmConfig = FcmConfig(),
    val github: GithubConfig = GithubConfig(),
    val quartz: SchedulerConfig = SchedulerConfig(),
    val db: DbConfig = DbConfig(),
    val emailConfig: EmailConfig = EmailConfig(),
    val protocol: QuestionnaireProtocolConfig = QuestionnaireProtocolConfig(),
) : Validation {
    override fun validate() {
        listOf(auth, server, db).forEach { validation: Validation ->
            validation.validate()
        }
    }
}
