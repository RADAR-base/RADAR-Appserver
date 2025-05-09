package org.radarbase.appserver.exception

import org.radarbase.appserver.util.OpenClass
import java.io.Serial

@OpenClass
class MessageTransmitException : Exception {
    companion object {
        @Serial
        private const val serialVersionUID = -281834508766939L
    }

    constructor(message: String) : super(message)
    constructor(message: String, e: Throwable) : super(message, e)
}
