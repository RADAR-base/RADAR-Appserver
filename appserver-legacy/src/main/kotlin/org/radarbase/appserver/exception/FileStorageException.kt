package org.radarbase.appserver.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus
import java.io.Serial

@Suppress("unused")
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class FileStorageException : RuntimeException {
    companion object {
        @Serial
        private const val serialVersionUID: Long = -793674245766939L
    }

    constructor(message: String) : super(message)

    constructor(message: String, `object`: Any) : super("$message $`object`")
}
