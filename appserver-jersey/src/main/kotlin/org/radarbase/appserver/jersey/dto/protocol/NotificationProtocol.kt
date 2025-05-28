package org.radarbase.appserver.jersey.dto.protocol

import com.fasterxml.jackson.annotation.JsonProperty

data class NotificationProtocol(
    @field:JsonProperty("mode")
    var mode: NotificationProtocolMode = NotificationProtocolMode.STANDARD,
    @field:JsonProperty("title")
    var title: LanguageText? = null,
    @field:JsonProperty("text")
    var body: LanguageText? = null,
    @field:JsonProperty("email")
    var email: EmailNotificationProtocol = EmailNotificationProtocol(),
)
