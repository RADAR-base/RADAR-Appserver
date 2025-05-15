package org.radarbase.appserver.jersey.config

import org.radarbase.appserver.jersey.entity.Project
import org.radarbase.appserver.jersey.utils.checkInvalidDetails

data class DbConfig(
    val classes: List<String> = listOf(
        Project::class.qualifiedName!!,
    ),
    val jdbcDriver: String? = null,
    val jdbcUrl: String? = null,
    val username: String? = null,
    val password: String? = null,
    val hibernateDialect: String = "org.hibernate.dialect.PostgreSQLDialect",
    val additionalProperties: Map<String, String> = emptyMap(),
    val liquibase: LiquibaseConfig = LiquibaseConfig(),
) : Validation {
    override fun validate() {
        checkInvalidDetails<IllegalStateException>(
            {
                jdbcDriver.isNullOrBlank() || jdbcUrl.isNullOrBlank()
            },
            {
                "JDBC driver and URL must not be null or empty"
            },
        )
    }
}
