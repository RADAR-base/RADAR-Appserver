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

package org.radarbase.appserver.entity;

import org.hibernate.validator.constraints.UniqueElements;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.*;

/**
 * @author yatharthranjan
 */
@Table(name = "user", uniqueConstraints = {@UniqueConstraint(columnNames = {"subject_id", "fcm_token", "project_id"})})
@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Column(name = "subject_id", nullable = false)
    private String subjectId;

    @NotNull
    @Column(name = "fcm_token", nullable = false)
    private String fcmToken;

    @NotNull
    @ManyToOne()
    private Project project;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)
    @UniqueElements
    private Set<Notification> notifications;

    @OneToOne(cascade = CascadeType.ALL)
    private UserMetrics userMetrics;

    public User() {
        this.notifications = Collections.synchronizedSet(new HashSet<>());
    }

    public Long getId() {
        return id;
    }

    public String getSubjectId() {
        return subjectId;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public Project getProject() {
        return project;
    }

    public Set<Notification> getNotifications() {
        return notifications;
    }

    public User setSubjectId(String subjectId) {
        this.subjectId = subjectId;
        return this;
    }

    public User setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
        return this;
    }

    public User setProject(Project project) {
        this.project = project;
        return this;
    }

    public User setNotifications(Set<Notification> notifications) {
        this.notifications = notifications;
        return this;
    }

    public User addNotification(Notification notification) {
        this.notifications.add(notification);
        return this;
    }

    public User addNotifications(Set<Notification> notifications) {
        this.notifications.addAll(notifications);
        return this;
    }

    public UserMetrics getUserMetrics() {
        return userMetrics;
    }

    public void setUserMetrics(UserMetrics userMetrics) {
        this.userMetrics = userMetrics;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(getSubjectId(), user.getSubjectId()) &&
                Objects.equals(getFcmToken(), user.getFcmToken()) &&
                Objects.equals(getProject(), user.getProject());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSubjectId(), getFcmToken(), getProject());
    }
}
