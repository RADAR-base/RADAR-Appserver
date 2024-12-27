package org.radarbase.appserver.dto

import org.springframework.format.annotation.DateTimeFormat
import java.time.Instant

data class ProjectDTO(
    var id: Long? = null,
    var projectId: String? = null,

    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var createdAt: Instant? = null,

    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var updatedAt: Instant? = null,
)