package org.radarbase.appserver.exception

import java.io.Serial

@Suppress("unused")
class EmailMessageTransmitException : MessageTransmitException {

    companion object {
        @Serial
        private const val serialVersionUID = -1927189245766939L
    }

    constructor(message: String) : super(message)

    constructor(message: String, e: Throwable) : super(message, e)
}