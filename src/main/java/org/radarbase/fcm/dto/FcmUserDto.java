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

import org.radarbase.appserver.entity.User;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * @author yatharthranjan
 */
public class FcmUserDto implements Serializable {

    private static final long serialVersionUID = 1L;

    // Unique user key
    private Long id;

    // Project ID to be used in org.radarcns.kafka.ObservationKey record keys
    private String projectId;

    @NotNull
    // User ID to be used in org.radarcns.kafka.ObservationKey record keys
    private String subjectId;

    // The most recent time when the app was opened
    private LocalDateTime lastOpened;

    // The most recent time when a notification for the app was delivered.
    private LocalDateTime lastDelivered;

    @NotNull
    private LocalDateTime enrolmentDate;

    //Timezone offset of the user in seconds
    @NotNull
    private double timezone;

    private String fcmToken;

    public FcmUserDto(User user) {
        this.id = user.getId();
        this.projectId = user.getProject().getProjectId();
        this.subjectId = user.getSubjectId();
        this.lastOpened = LocalDateTime.ofInstant(user.getUserMetrics().getLastOpened(), ZoneOffset.UTC);
        this.lastDelivered = user.getUserMetrics().getLastDelivered() == null ? null : LocalDateTime.ofInstant(user.getUserMetrics().getLastDelivered(), ZoneOffset.UTC);
        this.fcmToken = user.getFcmToken();
        this.enrolmentDate = LocalDateTime.ofInstant(user.getEnrolmentDate(), ZoneOffset.UTC);
        this.timezone = user.getTimezone();
    }

    public FcmUserDto() {

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

    public LocalDateTime getLastOpened() {
        return lastOpened;
    }

    public LocalDateTime getLastDelivered() {
        return lastDelivered;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public FcmUserDto setId(Long id) {
        this.id = id;
        return this;
    }

    public FcmUserDto setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public FcmUserDto setSubjectId(String subjectId) {
        this.subjectId = subjectId;
        return this;
    }

    public FcmUserDto setLastOpened(LocalDateTime lastOpened) {
        this.lastOpened = lastOpened;
        return this;
    }

    public FcmUserDto setLastDelivered(LocalDateTime lastDelivered) {
        this.lastDelivered = lastDelivered;
        return this;
    }

    public FcmUserDto setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
        return this;
    }

    public LocalDateTime getEnrolmentDate() {
        return enrolmentDate;
    }

    public void setEnrolmentDate(LocalDateTime enrolmentDate) {
        this.enrolmentDate = enrolmentDate;
    }

    public double getTimezone() {
        return timezone;
    }

    public void setTimezone(double timezone) {
        this.timezone = timezone;
    }

    @Override
    public String toString() {
        return "FcmUserDto{" +
                "id=" + id +
                ", projectId='" + projectId + '\'' +
                ", subjectId='" + subjectId + '\'' +
                ", lastOpened=" + lastOpened +
                ", lastDelivered=" + lastDelivered +
                ", enrolmentDate=" + enrolmentDate +
                ", timezone=" + timezone +
                ", fcmToken='" + fcmToken + '\'' +
                '}';
    }
}
