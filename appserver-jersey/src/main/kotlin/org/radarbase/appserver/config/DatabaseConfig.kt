package org.radarbase.appserver.config

data class DbConfig(
    val classes: List<String> = emptyList(),
    val jdbcDriver: String? = null,
    val jdbcUrl: String? = null,
    val username: String? = null,
    val password: String? = null,
    val hibernateDialect: String = "org.hibernate.dialect.PostgreSQLDialect",
    val additionalProperties: Map<String, String> = emptyMap(),
)
