package org.radarbase.appserver.jersey.dto.fcm

import jakarta.validation.constraints.Size
import org.radarbase.appserver.jersey.utils.equalTo
import org.radarbase.appserver.jersey.utils.stringRepresentation
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Objects

class FcmNotifications {

    @field:Size(max = 200)
    private var _notifications: MutableList<FcmNotificationDto> = mutableListOf()

    val notifications: List<FcmNotificationDto>
        get() = _notifications

    fun withNotifications(notifications: List<FcmNotificationDto>): FcmNotifications = apply {
        this._notifications = notifications.toMutableList()
    }

    fun addNotification(notificationDto: FcmNotificationDto): FcmNotifications = apply {
        if (!_notifications.contains(notificationDto)) {
            this._notifications.add(notificationDto)
        } else {
            logger.info("Notification {} already exists in the Fcm Notifications.", notificationDto)
        }
    }

    override fun equals(other: Any?): Boolean = equalTo(
        other,
        FcmNotifications::_notifications,
    )

    override fun toString(): String = stringRepresentation(
        FcmNotifications::_notifications,
    )

    override fun hashCode(): Int {
        return Objects.hash(_notifications)
    }


    companion object {
        private val logger: Logger = LoggerFactory.getLogger(FcmNotifications::class.java)
    }
}
