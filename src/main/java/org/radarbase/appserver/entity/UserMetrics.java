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

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.time.Instant;

/**
 * @author yatharthranjan
 */
@Table(name = "user_metrics")
@Entity
public class UserMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    // The most recent time when the app was opened
    @Nullable
    private Instant lastOpened;

    // The most recent time when a notification for the app was delivered.
    @Nullable
    private Instant lastDelivered;

    @NonNull
    @OneToOne
    @PrimaryKeyJoinColumn
    private User user;

    public UserMetrics(Instant lastOpened, Instant lastDelivered) {
        this.lastOpened = lastOpened;
        this.lastDelivered = lastDelivered;
    }

    public Long getId() {
        return id;
    }

    public Instant getLastOpened() {
        return lastOpened;
    }

    public Instant getLastDelivered() {
        return lastDelivered;
    }

    public UserMetrics setLastOpened(Instant lastOpened) {
        this.lastOpened = lastOpened;
        return this;
    }

    public UserMetrics setLastDelivered(Instant lastDelivered) {
        this.lastDelivered = lastDelivered;
        return this;
    }

    public User getUser() {
        return user;
    }

    public UserMetrics setUser(User user) {
        this.user = user;
        return this;
    }
}
