package org.radarbase.appserver.mapper

import org.radarbase.appserver.dto.fcm.FcmNotificationDto
import org.radarbase.appserver.entity.Notification
import org.radarbase.appserver.entity.Notification.NotificationBuilder
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/**
 * Converter [Mapper] class for [Notification] entity.
 *
 * @author yatharthranjan
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class NotificationMapper : Mapper<FcmNotificationDto, Notification> {

    override fun dtoToEntity(dto: FcmNotificationDto): Notification {
        return NotificationBuilder().apply {
            body(dto.body)
            scheduledTime(dto.scheduledTime)
            title(dto.title)
            sourceId(dto.sourceId)
            type(dto.type)
            ttlSeconds(dto.ttlSeconds)
            fcmMessageId(dto.hashCode().toString())
            appPackage(dto.appPackage)
            sourceType(dto.sourceType)
            additionalData(dto.additionalData)
            androidChannelId(dto.androidChannelId)
            bodyLocArgs(dto.bodyLocArgs)
            bodyLocKey(dto.bodyLocKey)
            titleLocKey(dto.titleLocKey)
            titleLocArgs(dto.titleLocArgs)
            badge(dto.badge)
            clickAction(dto.clickAction)
            color(dto.color)
            fcmCondition(dto.fcmCondition)
            fcmTopic(dto.fcmTopic)
            icon(dto.icon)
            mutableContent(dto.mutableContent)
            priority(dto.priority)
            sound(dto.sound)
            subtitle(dto.subtitle)
            tag(dto.tag)
            emailEnabled(dto.emailEnabled)
            emailTitle(dto.emailTitle)
            emailBody(dto.emailBody)
        }.build()
    }

    override fun entityToDto(entity: Notification): FcmNotificationDto {
        return FcmNotificationDto(entity)
    }
}
