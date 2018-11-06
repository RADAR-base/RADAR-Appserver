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

@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private User user;

    private String sourceId;

    private Instant scheduledTime;

    private String title;

    private String body;

    private int ttlSeconds;

    private String fcmMessageId;

    private boolean delivered;

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public String getSourceId() {
        return sourceId;
    }

    public Instant getScheduledTime() {
        return scheduledTime;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public int getTtlSeconds() {
        return ttlSeconds;
    }

    public String getFcmMessageId() {
        return fcmMessageId;
    }

    public boolean isDelivered() {
        return delivered;
    }
}
