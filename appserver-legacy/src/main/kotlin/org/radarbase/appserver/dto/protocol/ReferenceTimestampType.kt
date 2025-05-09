package org.radarbase.appserver.dto.protocol

import com.fasterxml.jackson.annotation.JsonProperty

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
