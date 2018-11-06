package org.radarbase.appserver.dto;

import java.time.LocalDateTime;

public class ScheduleNotificationDto {

    private LocalDateTime scheduledTime;

    private String title;

    private String body;

    private int ttlSeconds;

    private String fcmToken;

    private String fcmMessageId;

    private String subjectId;

    private String projectId;

    private String sourceId;
}
