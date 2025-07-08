package org.radarbase.appserver.jersey.auth.commons

import kotlinx.serialization.Serializable

@Serializable
data class MPMetaToken(
    val refreshToken: String,
)
