package org.radarbase.appserver.jersey.exception.entity

data class ErrorResponse(
    val error: String,
    val description: String,
)
