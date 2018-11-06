package org.radarbase.appserver.entity;

import javax.persistence.*;
import java.time.Instant;

@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;

    @ManyToOne
    private User user;

    private String sourceId;

    private Instant scheduledTime;

    private String title;

    private String body;

    private int ttlSeconds;

    private String fcmMessageId;

    private boolean delivered;

    public String getId() {
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
