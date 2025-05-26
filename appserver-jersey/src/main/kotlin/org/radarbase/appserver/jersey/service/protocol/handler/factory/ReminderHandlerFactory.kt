package org.radarbase.appserver.jersey.service.protocol.handler.factory

import org.radarbase.appserver.jersey.service.protocol.handler.ProtocolHandler
import org.radarbase.appserver.jersey.service.protocol.handler.impl.SimpleReminderHandler

object ReminderHandlerFactory {
    val reminderHandler: ProtocolHandler
        get() = SimpleReminderHandler()
}
