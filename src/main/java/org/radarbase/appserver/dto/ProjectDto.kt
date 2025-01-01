/*
 *
 *  *
 *  *  * Copyright 2018 King's College London
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *  *
 *  *
 *
 */

package org.radarbase.appserver.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.validation.constraints.NotEmpty
import org.jetbrains.annotations.NotNull
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
data class ProjectDto(
    var id: Long? = null,

    @field:NotNull
    @field:NotEmpty
    var projectId: String? = null,

    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var createdAt: Instant? = null,

    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var updatedAt: Instant? = null,
)