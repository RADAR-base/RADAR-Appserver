package org.radarbase.appserver.jersey.config.questionnaire

data class QuestionnaireProtocolConfig(
    val githubProtocolRepo: String? = null,
    val protocolFileName: String? = null,
    val githubBranch: String? = null,
)
