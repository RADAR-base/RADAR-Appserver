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

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String subjectId;

    private String fcmToken;

    // The most recent time when the app was opened
    private Instant lastOpened;

    // The most recent time when a notification for the app was delivered.
    private Instant lastDelivered;


    @ManyToOne()
    private Project project;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user", orphanRemoval = true)
    private List<Notification> notifications;

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

    public List<Notification> getNotifications() {
        return notifications;
    }

    public Instant getLastOpened() {
        return lastOpened;
    }

    public Instant getLastDelivered() {
        return lastDelivered;
    }
}
