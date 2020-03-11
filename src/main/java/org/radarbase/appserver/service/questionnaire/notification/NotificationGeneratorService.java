package org.radarbase.appserver.service.questionnaire.notification;

import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.radarbase.appserver.dto.fcm.FcmNotifications;
import org.radarbase.appserver.dto.protocol.Assessment;
import org.radarbase.appserver.entity.Task;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class NotificationGeneratorService {
    private transient int SECONDS_TO_MILLIS = 1000;

    public FcmNotificationDto createNotification(Task task, NotificationType type, Instant notificationTimestamp) {
        FcmNotificationDto current = new FcmNotificationDto();
        current.setScheduledTime(notificationTimestamp);
        current.setTtlSeconds(calculateTtlSeconds(task, notificationTimestamp));
        current.setType(task.getName());
        current.setSourceType("Type");
        current.setSourceId("id");
        current.setAppPackage("org.phidatalab.radar-armt");

        switch (type) {
            case NOW:
            default:
                current.setTitle("Questionnaire time");
                current.setBody("Won't usually take longer than " + task.getEstimatedCompletionTime() + " minutes");
                break;
        }
        return current;
    }

    private int calculateTtlSeconds(Task task, Instant notificationTimestamp) {
        Long endTime = task.getTimestamp().getEpochSecond() + 1000000 / SECONDS_TO_MILLIS;
        Long timeUntilEnd = endTime - notificationTimestamp.getEpochSecond();
        return timeUntilEnd.intValue();
    }

}
