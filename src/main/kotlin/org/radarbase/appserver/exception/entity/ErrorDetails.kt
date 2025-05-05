package org.radarbase.appserver.exception.entity

import org.radarbase.appserver.util.OpenClass
import java.time.Instant

@OpenClass
class ErrorDetails(
    var timestamp: Instant? = null,
    var status: Int = 0,
    var message: String? = null,
    var path: String? = null
)