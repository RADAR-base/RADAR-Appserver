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
    return new Notification()
        .setBody(notificationDto.getBody())
        .setScheduledTime(notificationDto.getScheduledTime())
        .setTitle(notificationDto.getTitle())
        .setSourceId(notificationDto.getSourceId())
        .setType(notificationDto.getType())
        .setTtlSeconds(notificationDto.getTtlSeconds())
        .setFcmMessageId(String.valueOf(notificationDto.hashCode()))
        .setAppPackage(notificationDto.getAppPackage())
        .setSourceType(notificationDto.getSourceType())
        .setAdditionalData(notificationDto.getAdditionalData())
        .setAndroidChannelId(notificationDto.getAndroidChannelId())
        .setBodyLocArgs(notificationDto.getBodyLocArgs())
        .setBodyLocKey(notificationDto.getBodyLocKey())
        .setTitleLocKey(notificationDto.getTitleLocKey())
        .setTitleLocArgs(notificationDto.getTitleLocArgs())
        .setBadge(notificationDto.getBadge())
        .setClickAction(notificationDto.getClickAction())
        .setColor(notificationDto.getColor())
        .setFcmCondition(notificationDto.getFcmCondition())
        .setFcmTopic(notificationDto.getFcmTopic())
        .setIcon(notificationDto.getIcon())
        .setMutableContent(notificationDto.isMutableContent())
        .setPriority(notificationDto.getPriority())
        .setSound(notificationDto.getSound())
        .setSubtitle(notificationDto.getSubtitle())
        .setTag(notificationDto.getTag());
  }

  @Override
  public FcmNotificationDto entityToDto(Notification notification) {
    return new FcmNotificationDto(notification);
  }
}
