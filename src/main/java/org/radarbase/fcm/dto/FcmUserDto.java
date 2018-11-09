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

package org.radarbase.fcm.dto;

import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.entity.User;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author yatharthranjan
 */
public class FcmUserDto implements Serializable {

    private static final long serialVersionUID = 1L;

    // Unique user key
    private Long id;

    // Project ID to be used in org.radarcns.kafka.ObservationKey record keys
    private String projectId;

    // User ID to be used in org.radarcns.kafka.ObservationKey record keys
    private String subjectId;

    // Source ID to be used in org.radarcns.kafka.ObservationKey record keys
    private List<String> sourceIds;

    // The most recent time when the app was opened
    private LocalDateTime lastOpened;

    // The most recent time when a notification for the app was delivered.
    private LocalDateTime lastDelivered;

    private String fcmToken;

    private Set<Notification> notifications;

    public FcmUserDto(User user) {
        this.id = user.getId();
        this.projectId = user.getProject().getProjectId();
        this.subjectId = user.getSubjectId();
        this.sourceIds = user.getNotifications().stream().map(Notification::getSourceId).collect(Collectors.toList());
        this.lastOpened = LocalDateTime.ofInstant(user.getUserMetrics().getLastOpened(), ZoneOffset.UTC);
        this.lastDelivered = LocalDateTime.ofInstant(user.getUserMetrics().getLastDelivered(), ZoneOffset.UTC);
        this.fcmToken = user.getFcmToken();
        this.notifications = user.getNotifications();
    }

    public Long getId() {
        return id;
    }

    public String getProjectId() {
        return projectId;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public List<String> getSourceId() {
        return sourceIds;
    }

    public LocalDateTime getLastOpened() {
        return lastOpened;
    }

    public LocalDateTime getLastDelivered() {
        return lastDelivered;
    }

    public String getFcmToken() {
        return fcmToken;
    }
}
