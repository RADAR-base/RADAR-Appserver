package org.radarbase.appserver.exception.entity

import java.time.Instant

class ErrorDetailsWithCause(
    timestamp: Instant?,
    status: Int,
    val cause: String?,
    message: String?,
    path: String?,
) : ErrorDetails(timestamp, status, message, path)
