package org.radarbase.appserver.dto.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AssessmentType {
    @JsonProperty("scheduled")
    SCHEDULED,
    @JsonProperty("clinical")
    CLINICAL,
    @JsonProperty("triggered")
    TRIGGERED,
    @JsonProperty("all")
    ALL
}