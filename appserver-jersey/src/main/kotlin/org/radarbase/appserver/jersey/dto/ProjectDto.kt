package org.radarbase.appserver.jersey.dto

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
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
data class ProjectDto(
    var id: Long? = null,

    @field:NotNull
    @field:NotEmpty
    var projectId: String? = null,

    @field:JsonFormat(
        shape  = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        timezone = "UTC"
    )
    var createdAt: Instant? = null,

    @field:JsonFormat(
        shape  = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        timezone = "UTC"
    )
    var updatedAt: Instant? = null,
)
