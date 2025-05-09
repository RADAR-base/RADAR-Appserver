package org.radarbase.appserver.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.io.Serial

@Suppress("unused")
@ResponseStatus(HttpStatus.EXPECTATION_FAILED)
class InvalidPathDetailsException : IllegalArgumentException {
    companion object {
        @Serial
        private const val serialVersionUID = -793674245766939L
    }

    constructor(message: String) : super(message)

    constructor(message: String, obj: Any) : super("$message $obj")
}
