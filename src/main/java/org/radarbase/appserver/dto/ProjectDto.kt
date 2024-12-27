package org.radarbase.appserver.dto

import org.springframework.format.annotation.DateTimeFormat
import java.io.Serial
import java.io.Serializable
import java.time.Instant

data class ProjectDto(
    var id: Long? = null,
    var projectId: String? = null,

    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var createdAt: Instant? = null,

    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    var updatedAt: Instant? = null
): Serializable {
    companion object {
        @Serial
        private const val serialVersionUID: Long = 1L
    }
}