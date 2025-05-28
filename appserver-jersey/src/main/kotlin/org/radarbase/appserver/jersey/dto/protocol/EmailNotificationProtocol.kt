package org.radarbase.appserver.jersey.dto.protocol

import com.fasterxml.jackson.annotation.JsonProperty

data class EmailNotificationProtocol(
    @field:JsonProperty("enabled")
    val enabled: Boolean = false,

    @field:JsonProperty("title")
    var title: LanguageText? = null,

    @field:JsonProperty("text")
    var body: LanguageText? = null,
)
