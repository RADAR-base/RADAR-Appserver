/*
 * Copyright 2025 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.appserver.jersey.dto.fcm

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.radarbase.appserver.jersey.entity.User
import java.time.Instant

@Suppress("unused")
data class FcmUserDto(
    var id: Long? = null,

    /**
     * Project ID to be used in org.radarcns.kafka.ObservationKey record keys
     */
    var projectId: String? = null,

    /**
     * User ID to be used in org.radarcns.kafka.ObservationKey record keys
     */
    @field:NotEmpty
    var subjectId: String? = null,

    /**
     * Email address of the user (optional, needed when sending notifications via email)
     */
    @field:Email
    var email: String? = null,

    /**
     * The most recent time when the app was opened
     */
    var lastOpened: Instant? = null,

    /**
     * The most recent time when a notification for the app was delivered
     */
    var lastDelivered: Instant? = null,

    @field:JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        timezone = "UTC",
    )
    var createdAt: Instant? = null,

    @field:JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        timezone = "UTC",
    )
    var updatedAt: Instant? = null,

    @field:NotNull
    @field:JsonFormat(
        shape = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        timezone = "UTC",
    )
    var enrolmentDate: Instant? = null,

    @field:NotNull
    var timezone: String? = null,

    /**
     * Timezone of the user based on tz database names
     */
    var fcmToken: String? = null,

    var language: String? = null,

    @field:Size(max = 100)
    var attributes: Map<String?, String?>? = null,
) {
    constructor(user: User) : this(
        id = user.id,
        projectId = user.project?.projectId,
        subjectId = user.subjectId,
        email = user.emailAddress,
        lastOpened = user.usermetrics?.lastOpened,
        lastDelivered = user.usermetrics?.lastDelivered,
        createdAt = user.createdAt?.toInstant(),
        updatedAt = user.updatedAt?.toInstant(),
        enrolmentDate = user.enrolmentDate,
        timezone = user.timezone,
        fcmToken = user.fcmToken,
        language = user.language,
        attributes = user.attributes,
    ) }
