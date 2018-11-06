package org.radarbase.fcm.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public class FcmNotificationDto implements Serializable {

    private static final long serialVersionUID = 3L;

    private String id;

    private LocalDateTime scheduledTime;

    private boolean delivered;

    private String title;

    private String body;

    private int ttlSeconds;

    private String fcmToken;

    private String fcmMessageId;

    public FcmNotificationDto(String id, LocalDateTime scheduledTime, boolean delivered, String title, String body, int ttlSeconds, String fcmToken, String fcmMessageId) {
        this.id = id;
        this.scheduledTime = scheduledTime;
        this.delivered = delivered;
        this.title = title;
        this.body = body;
        this.ttlSeconds = ttlSeconds;
        this.fcmToken = fcmToken;
        this.fcmMessageId = fcmMessageId;
    }
}
