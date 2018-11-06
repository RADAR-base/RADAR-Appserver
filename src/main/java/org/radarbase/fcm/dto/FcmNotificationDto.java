package org.radarbase.fcm.dto;

import org.radarbase.appserver.entity.Notification;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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

    public FcmNotificationDto(Notification notificationEntity) {
        this.id = notificationEntity.getId();
        this.scheduledTime = LocalDateTime.ofInstant(notificationEntity.getScheduledTime(), ZoneOffset.UTC);
        this.title = notificationEntity.getTitle();
        this.body = notificationEntity.getBody();
        this.delivered = notificationEntity.isDelivered();
        this.fcmMessageId = notificationEntity.getFcmMessageId();
        this.fcmToken = notificationEntity.getUser().getFcmToken();
    }
}
