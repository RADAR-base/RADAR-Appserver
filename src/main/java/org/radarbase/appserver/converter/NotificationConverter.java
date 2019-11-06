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

package org.radarbase.appserver.converter;

import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.radarbase.appserver.entity.Notification;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Converter {@link Converter} class for {@link Notification} entity.
 *
 * @author yatharthranjan
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class NotificationConverter implements Converter<Notification, FcmNotificationDto> {

    @Override
    public Notification dtoToEntity(FcmNotificationDto notificationDto) {

        return ((Notification.NotificationBuilder) new Notification.NotificationBuilder()
                .mutableContent(notificationDto.isMutableContent())
                .priority(notificationDto.getPriority())
                .fcmCondition(notificationDto.getFcmCondition())
                .fcmTopic(notificationDto.getFcmTopic())
                .fcmMessageId(String.valueOf(notificationDto.hashCode()))
                .appPackage(notificationDto.getAppPackage())
                .sourceType(notificationDto.getSourceType())
                .sourceId(notificationDto.getSourceId())
                .ttlSeconds(notificationDto.getTtlSeconds())
                .scheduledTime(notificationDto.getScheduledTime()))
                .body(notificationDto.getBody())
                .title(notificationDto.getTitle())
                .type(notificationDto.getType())
                .additionalData(notificationDto.getAdditionalData())
                .androidChannelId(notificationDto.getAndroidChannelId())
                .bodyLocArgs(notificationDto.getBodyLocArgs())
                .bodyLocKey(notificationDto.getBodyLocKey())
                .titleLocKey(notificationDto.getTitleLocKey())
                .titleLocArgs(notificationDto.getTitleLocArgs())
                .badge(notificationDto.getBadge())
                .clickAction(notificationDto.getClickAction())
                .color(notificationDto.getColor())
                .icon(notificationDto.getIcon())
                .sound(notificationDto.getSound())
                .subtitle(notificationDto.getSubtitle())
                .tag(notificationDto.getTag())
                .build();
    }

    @Override
    public FcmNotificationDto entityToDto(Notification notification) {
        return new FcmNotificationDto(notification);
    }
}
