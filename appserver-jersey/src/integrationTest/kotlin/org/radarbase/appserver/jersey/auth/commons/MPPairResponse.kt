package org.radarbase.appserver.jersey.auth.commons

import kotlinx.serialization.Serializable

@Serializable
class MPPairResponse(
    val tokenUrl: String,
)
