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

import org.radarbase.appserver.dto.RadarProjectDto;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.entity.Project;
import org.radarbase.appserver.entity.User;
import org.radarbase.fcm.dto.FcmNotificationDto;
import org.radarbase.fcm.dto.FcmUserDto;

public class ConverterFactory {

    private static final Converter<Project, RadarProjectDto> projectConverter = new ProjectConverter();
    private static final Converter<Notification, FcmNotificationDto> notificationConverter = new NotificationConverter();
    private static final Converter<User, FcmUserDto> userConverter = new UserConverter();

    public static Converter<Project, RadarProjectDto> getProjectConverter() {
        return projectConverter;
    }

    public static Converter<Notification, FcmNotificationDto> getNotificationConverter() {
        return notificationConverter;
    }

    public static Converter<User, FcmUserDto> getUserConverter() {
        return userConverter;
    }
}
