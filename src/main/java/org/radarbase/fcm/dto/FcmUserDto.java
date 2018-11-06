package org.radarbase.fcm.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class FcmUserDto implements Serializable {

    private static final long serialVersionUID = 1L;

    // Unique user key
    private String id;

    // Project ID to be used in org.radarcns.kafka.ObservationKey record keys
    private String projectId;

    // User ID to be used in org.radarcns.kafka.ObservationKey record keys
    private String subjectId;

    // Source ID to be used in org.radarcns.kafka.ObservationKey record keys
    private String sourceId;

    // The most recent time when the app was opened
    private LocalDateTime lastOpened;

    // The most recent time when a notification for the app was deliered.
    private LocalDateTime lastDelivered;

    private String fcmToken;

    FcmUserDto(String id, String projectId, String subjectId, String sourceId, LocalDateTime lastOpened, LocalDateTime lastDelivered, String fcmToken) {
        this.id = id;
        this.projectId = projectId;
        this.subjectId = subjectId;
        this.sourceId = sourceId;
        this.lastOpened = lastOpened;
        this.lastDelivered = lastDelivered;
        this.fcmToken = fcmToken;
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

    public String getSourceId() {
        return sourceId;
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
