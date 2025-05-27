package org.radarbase.appserver.jersey.service.transmitter

import org.radarbase.appserver.jersey.entity.DataMessage
import org.radarbase.appserver.jersey.exception.MessageTransmitException

interface DataMessageTransmitter {
    @Throws(MessageTransmitException::class)
    fun send(dataMessage: DataMessage)
}
