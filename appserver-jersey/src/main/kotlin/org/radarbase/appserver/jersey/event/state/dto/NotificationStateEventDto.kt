package org.radarbase.appserver.jersey.event.state.dto

import org.radarbase.appserver.jersey.entity.Notification
import org.radarbase.appserver.jersey.event.state.MessageState
import java.time.Instant

/**
 * Create a new ApplicationEvent.
 *
 * @param notification   the notification associated with this state event.
 * @param state          the current [MessageState].
 * @param additionalInfo any additional info associated with the state change.
 */
class NotificationStateEventDto(
    val notification: Notification,
    state: MessageState,
    additionalInfo: Map<String, String>?,
    time: Instant,
) : MessageStateEventDto(state, additionalInfo, time) {

    override fun toString(): String {
        return "NotificationStateEventDto(notification=$notification) ${super.toString()}"
    }
}
