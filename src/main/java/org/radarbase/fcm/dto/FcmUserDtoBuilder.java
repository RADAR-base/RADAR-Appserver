package org.radarbase.fcm.dto;

import java.time.LocalDateTime;

public class FcmUserDtoBuilder {
    private String id;
    private String projectId;
    private String subjectId;
    private String sourceId;
    private LocalDateTime lastOpened;
    private LocalDateTime lastDelivered;
    private String fcmToken;

    public FcmUserDtoBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public FcmUserDtoBuilder setProjectId(String projectId) {
        this.projectId = projectId;
        return this;
    }

    public FcmUserDtoBuilder setSubjectId(String subjectId) {
        this.subjectId = subjectId;
        return this;
    }

    public FcmUserDtoBuilder setSourceId(String sourceId) {
        this.sourceId = sourceId;
        return this;
    }

    public FcmUserDtoBuilder setLastOpened(LocalDateTime lastOpened) {
        this.lastOpened = lastOpened;
        return this;
    }

    public FcmUserDtoBuilder setLastDelivered(LocalDateTime lastDelivered) {
        this.lastDelivered = lastDelivered;
        return this;
    }

    public FcmUserDtoBuilder setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
        return this;
    }

    public FcmUserDto createFcmUserDto() {
        return new FcmUserDto(id, projectId, subjectId, sourceId, lastOpened, lastDelivered, fcmToken);
    }
}