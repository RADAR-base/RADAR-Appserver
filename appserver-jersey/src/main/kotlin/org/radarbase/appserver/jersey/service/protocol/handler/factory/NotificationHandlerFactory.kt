package org.radarbase.appserver.jersey.service.protocol.handler.factory

import org.radarbase.appserver.jersey.dto.protocol.NotificationProtocol
import org.radarbase.appserver.jersey.dto.protocol.NotificationProtocolMode
import org.radarbase.appserver.jersey.service.protocol.handler.ProtocolHandler
import org.radarbase.appserver.jersey.service.protocol.handler.impl.DisabledNotificationHandler
import org.radarbase.appserver.jersey.service.protocol.handler.impl.SimpleNotificationHandler
import java.io.IOException

object NotificationHandlerFactory {
    @Throws(IOException::class)
    fun getNotificationHandler(protocol: NotificationProtocol): ProtocolHandler {
        return when (protocol.mode) {
            NotificationProtocolMode.DISABLED -> DisabledNotificationHandler()
            NotificationProtocolMode.COMBINED -> throw IOException("Combined Notification Protocol Mode is not supported yet")
            else -> SimpleNotificationHandler()
        }
    }
}
