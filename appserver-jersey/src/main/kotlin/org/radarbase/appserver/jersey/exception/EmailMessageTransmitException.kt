package org.radarbase.appserver.jersey.exception

class EmailMessageTransmitException : MessageTransmitException {
    constructor(message: String) : super(
        "fcm_message_transmit_exception",
        message,
    )
}
