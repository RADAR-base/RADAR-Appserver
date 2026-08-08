package org.radarbase.appserver.dto.protocol;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum NotificationProtocolMode {
    @JsonProperty("standard")
    STANDARD,
    @JsonProperty("disabled")
    DISABLED,
    @JsonProperty("combined")
    COMBINED,
}