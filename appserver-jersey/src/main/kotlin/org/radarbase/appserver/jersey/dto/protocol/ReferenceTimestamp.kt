package org.radarbase.appserver.jersey.dto.protocol

import com.fasterxml.jackson.annotation.JsonProperty

data class ReferenceTimestamp(
    @field:JsonProperty("timestamp")
    var timestamp: String? = null,
    @field:JsonProperty("format")
    var format: ReferenceTimestampType? = null,
)
