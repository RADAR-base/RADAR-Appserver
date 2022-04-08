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
package org.radarbase.appserver.converter

import org.radarbase.appserver.dto.fcm.FcmNotificationDto
import org.radarbase.appserver.entity.Notification
import org.springframework.beans.factory.config.ConfigurableBeanFactory
import org.springframework.context.annotation.Scope
import org.springframework.stereotype.Component

/**
 * Converter [Converter] class for [Notification] entity.
 *
 * @author yatharthranjan
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
class NotificationConverter : Converter<Notification, FcmNotificationDto> {

    override fun dtoToEntity(notificationDto: FcmNotificationDto): Notification {
        return Notification(
            body = notificationDto.body,
            scheduledTime = notificationDto.scheduledTime,
            title = notificationDto.title,
            sourceId = notificationDto.sourceId,
            type = notificationDto.type,
            ttlSeconds = notificationDto.ttlSeconds,
            fcmMessageId = notificationDto.hashCode().toString(),
            appPackage = notificationDto.appPackage,
            sourceType = notificationDto.sourceType,
            additionalData = notificationDto.additionalData,
            androidChannelId = notificationDto.androidChannelId,
            bodyLocArgs = notificationDto.bodyLocArgs,
            bodyLocKey = notificationDto.bodyLocKey,
            titleLocArgs = notificationDto.titleLocArgs,
            titleLocKey = notificationDto.titleLocKey,
            badge = notificationDto.badge,
            clickAction = notificationDto.clickAction,
            color = notificationDto.color,
            fcmCondition = notificationDto.fcmCondition,
            fcmTopic = notificationDto.fcmTopic,
            icon = notificationDto.icon,
            mutableContent = notificationDto.isMutableContent,
            priority = notificationDto.priority,
            sound = notificationDto.sound,
            subtitle = notificationDto.subtitle,
            tag = notificationDto.tag,
        )
    }

    override fun entityToDto(notification: Notification): FcmNotificationDto {
        return FcmNotificationDto(notification)
    }
}