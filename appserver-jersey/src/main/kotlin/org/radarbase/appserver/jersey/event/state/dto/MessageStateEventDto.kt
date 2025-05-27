package org.radarbase.appserver.jersey.event.state.dto

import org.radarbase.appserver.jersey.event.state.MessageState
import org.radarbase.appserver.jersey.utils.stringRepresentation
import java.time.Instant

open class MessageStateEventDto(
    var state: MessageState,
    var additionalInfo: Map<String, String>?,
    var time: Instant,
) {
    override fun toString(): String = stringRepresentation(
        MessageStateEventDto::state,
        MessageStateEventDto::additionalInfo,
        MessageStateEventDto::time,
    )
}
