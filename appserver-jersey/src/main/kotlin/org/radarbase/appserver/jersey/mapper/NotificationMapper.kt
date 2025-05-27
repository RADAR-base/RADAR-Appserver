package org.radarbase.appserver.jersey.mapper

import org.radarbase.appserver.jersey.dto.fcm.FcmNotificationDto
import org.radarbase.appserver.jersey.entity.Notification

class NotificationMapper : Mapper<FcmNotificationDto, Notification> {

    override suspend fun dtoToEntity(dto: FcmNotificationDto): Notification {
        return Notification.NotificationBuilder().apply {
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

    override suspend fun entityToDto(entity: Notification): FcmNotificationDto {
        return FcmNotificationDto(entity)
    }
}
