package org.radarbase.fcm.dto;

import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.entity.User;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

public class FcmUserDto implements Serializable {

    private static final long serialVersionUID = 1L;

    // Unique user key
    private String id;

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

    public FcmUserDto(User user) {
        this.id = user.getId();
        this.projectId = user.getProject().getProjectId();
        this.subjectId = user.getSubjectId();
        this.sourceIds = user.getNotifications().stream().map(Notification::getSourceId).collect(Collectors.toList());
        this.lastOpened = LocalDateTime.ofInstant(user.getLastOpened(), ZoneOffset.UTC);
        this.lastDelivered = LocalDateTime.ofInstant(user.getLastDelivered(), ZoneOffset.UTC);

    }

    public String getId() {
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
