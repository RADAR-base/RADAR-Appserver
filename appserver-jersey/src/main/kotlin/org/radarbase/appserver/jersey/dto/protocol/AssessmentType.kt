package org.radarbase.appserver.jersey.dto.protocol

import com.fasterxml.jackson.annotation.JsonProperty

enum class AssessmentType {
    @JsonProperty("scheduled")
    SCHEDULED,

    @JsonProperty("clinical")
    CLINICAL,

    @JsonProperty("triggered")
    TRIGGERED,

    @JsonProperty("all")
    ALL,
}
