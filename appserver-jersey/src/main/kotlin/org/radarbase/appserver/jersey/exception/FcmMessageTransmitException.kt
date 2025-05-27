package org.radarbase.appserver.jersey.exception

class FcmMessageTransmitException : MessageTransmitException {
    constructor(message: String) : super(
        "fcm_message_transmit_exception",
        message,
    )
}
