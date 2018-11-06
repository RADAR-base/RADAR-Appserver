package org.radarbase.appserver.entity;

import javax.persistence.*;
import java.time.Instant;
import java.util.List;

@Entity
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;

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

    public String getId() {
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
