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
import org.radarbase.appserver.converter.ProjectConverter;
import org.radarbase.appserver.converter.UserConverter;
import org.radarbase.appserver.dto.RadarProjectDto;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.entity.Project;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.entity.UserMetrics;
import org.radarbase.appserver.exception.NotFoundException;
import org.radarbase.appserver.repository.ProjectRepository;
import org.radarbase.appserver.repository.UserRepository;
import org.radarbase.fcm.dto.FcmNotificationDto;
import org.radarbase.fcm.dto.FcmNotifications;
import org.radarbase.fcm.dto.FcmUserDto;
import org.radarbase.fcm.dto.FcmUsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Optional;

/**
 * @author yatharthranjan
 */
@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RadarProjectService projectService;

    private static final Converter<User, FcmUserDto> userConverter = new UserConverter();
    private static final Converter<Notification, FcmNotificationDto> notificationConverter = new NotificationConverter();
    private static final Converter<Project, RadarProjectDto> projectConverter = new ProjectConverter();

    @Transactional(readOnly = true)
    public FcmUsers getAllRadarUsers() {
        return null;
    }

    @Transactional
    public FcmUserDto storeRadarUser(FcmUserDto userDto) {
        // TODO: Future -- If any value is null get them using the MP api using others. (eg only subject id, then get project id and source ids from MP)
        // TODO: Store in DB first
        return null;
    }

    @Transactional(readOnly = true)
    public FcmUsers getUserByProjectId(String projectId) {
        return null;
    }


    @Transactional(readOnly = true)
    public FcmUserDto getUserBySubjectId(String subjectId){
        return null;
    }

    @Transactional(readOnly = true)
    public FcmNotifications getNotificationsBySubjectId(String subjectId){
        return null;
    }
}
