package org.radarbase.appserver.jersey.dto.protocol

import com.fasterxml.jackson.annotation.JsonProperty

@Suppress("unused")
enum class ReferenceTimestampType {
    @JsonProperty("date")
    DATE,

    @JsonProperty("datetime")
    DATETIME,

    @JsonProperty("datetimeutc")
    DATETIMEUTC,

    @JsonProperty("now")
    NOW,

    @JsonProperty("today")
    TODAY,
}
