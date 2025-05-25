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

    @field:NotNull
    @field:JsonFormat(
        shape  = JsonFormat.Shape.STRING,
        pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        timezone = "UTC"
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
        createdAt = user.createdAt,
        updatedAt = user.updatedAt,
        enrolmentDate = user.enrolmentDate,
        timezone = user.timezone,
        fcmToken = user.fcmToken,
        language = user.language,
        attributes = user.attributes
    )}
