package org.radarbase.appserver.jersey.service.transmitter

import org.radarbase.appserver.jersey.entity.Notification
import org.radarbase.appserver.jersey.exception.MessageTransmitException

interface NotificationTransmitter {
    @Throws(MessageTransmitException::class)
    fun send(notification: Notification)
}
