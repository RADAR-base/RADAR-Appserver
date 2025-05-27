package org.radarbase.appserver.jersey.service.transmitter

import org.radarbase.appserver.jersey.entity.DataMessage
import org.radarbase.appserver.jersey.entity.Notification

class FcmTransmitter: DataMessageTransmitter, NotificationTransmitter {
    override fun send(dataMessage: DataMessage) {
        TODO("Not yet implemented")
    }

    override fun send(notification: Notification) {
        TODO("Not yet implemented")
    }
}
