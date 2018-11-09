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

package org.radarbase.appserver.service;

import org.radarbase.appserver.converter.Converter;
import org.radarbase.appserver.converter.NotificationConverter;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.repository.NotificationRepository;
import org.radarbase.fcm.dto.FcmNotificationDto;
import org.radarbase.fcm.dto.FcmNotifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * @author yatharthranjan
 */
@Service
public class FcmNotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    private static final Converter<Notification, FcmNotificationDto> notificationConverter = new NotificationConverter();

    public FcmNotifications getAllNotifications() {
        List<Notification> notifications = notificationRepository.findAll();
        return new FcmNotifications().setNotifications(notificationConverter.entitiesToDtos(notifications));
    }

    public FcmNotificationDto getNotificationById(long id) {
        Optional<Notification> notification = notificationRepository.findById(id);
        return notificationConverter.entityToDto(notification.get());
    }
}