package org.radarbase.appserver.jersey.dto.protocol

import com.fasterxml.jackson.annotation.JsonProperty

enum class NotificationProtocolMode {
    @JsonProperty("standard")
    STANDARD,

    @JsonProperty("disabled")
    DISABLED,

    @JsonProperty("combined")
    COMBINED,
}
