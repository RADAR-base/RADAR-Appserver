package org.radarbase.appserver.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.format.annotation.DateTimeFormat
import java.time.Instant

/**
 * Data Transfer Object representing a [ManagementPortal](https://github.com/Radar-base/ManagementPortal) Project.
 *
 * This DTO is used to encapsulate project-related information,
 * including its unique identifier, project ID, and timestamps for
 * creation and updates. The timestamps follow ISO date-time format.
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ProjectDTO(
    var id: Long? = null,
    val projectId: String,

    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var createdAt: Instant? = null,

    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var updatedAt: Instant? = null,
)