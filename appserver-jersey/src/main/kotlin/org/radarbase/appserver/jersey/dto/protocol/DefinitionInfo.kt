package org.radarbase.appserver.jersey.dto.protocol

import java.net.URI

data class DefinitionInfo(
    val repository: URI? = null,
    val name: String? = null,
    val avsc: String? = null,
)
