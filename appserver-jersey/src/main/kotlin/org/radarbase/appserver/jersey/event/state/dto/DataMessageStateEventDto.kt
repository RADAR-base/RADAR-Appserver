package org.radarbase.appserver.jersey.event.state.dto

import org.radarbase.appserver.jersey.entity.DataMessage
import org.radarbase.appserver.jersey.event.state.MessageState
import java.time.Instant

/**
 * Create a new ApplicationEvent.
 *
 * @param dataMessage    the data message associated with this state event.
 * @param state          the current [MessageState]..
 * @param additionalInfo any additional info associated with the state change.
 */

class DataMessageStateEventDto(
    val dataMessage: DataMessage,
    state: MessageState,
    additionalInfo: Map<String, String>?,
    time: Instant,
) : MessageStateEventDto(state, additionalInfo, time) {

    override fun toString(): String {
        return "DataMessageStateEventDto(dataMessage=$dataMessage) ${super.toString()}"
    }
}
