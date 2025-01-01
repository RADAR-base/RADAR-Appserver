package org.radarbase.appserver.dto.fcm

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.radarbase.appserver.entity.User
import org.springframework.format.annotation.DateTimeFormat
import java.time.Instant

@JsonIgnoreProperties(ignoreUnknown = true)
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

    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var createdAt: Instant? = null,

    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    var updatedAt: Instant? = null,

    @field:NotNull
    @field:DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    val enrolmentDate: Instant? = null,

    @field:NotNull
    val timezone: String? = null,

    /**
     * Timezone of the user based on tz database names
     */
    var fcmToken: String? = null,

    var language: String? = null,

    @field:Size(max = 100)
    val attributes: Map<String, String>? = null,
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
        attributes = user.attributes
    )}