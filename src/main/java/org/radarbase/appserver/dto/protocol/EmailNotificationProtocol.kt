package org.radarbase.appserver.dto.protocol

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor

@JsonIgnoreProperties(ignoreUnknown = true)
data class EmailNotificationProtocol (
    @field:JsonProperty("enabled")
    val enabled: Boolean = false,

    @field:JsonProperty("title")
    var title: LanguageText? = null,

    @field:JsonProperty("text")
    var body: LanguageText? = null
    )
