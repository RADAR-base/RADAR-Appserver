package org.radarbase.appserver.jersey.exception

import jakarta.ws.rs.core.Response
import org.radarbase.jersey.exception.HttpApplicationException

open class MessageTransmitException : HttpApplicationException {
    constructor(message: String) : super(
        Response.Status.INTERNAL_SERVER_ERROR,
        "message_transmit_exception",
        message,
    )

    constructor(code: String, message: String) : super(
        Response.Status.INTERNAL_SERVER_ERROR,
        code,
        message,
    )

}
