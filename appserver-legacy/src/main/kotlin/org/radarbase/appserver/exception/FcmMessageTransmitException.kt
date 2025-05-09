package org.radarbase.appserver.exception

import java.io.Serial

@Suppress("unused")
class FcmMessageTransmitException : MessageTransmitException {
    constructor(message: String) : super(message)
    constructor(message: String, e: Throwable) : super(message, e)

    companion object {
        @Serial
        private const val serialVersionUID: Long = -923871442166939L
    }
}
