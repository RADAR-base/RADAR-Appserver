package org.radarbase.appserver.service.questionnaire.notification;

import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.radarbase.appserver.entity.Task;

import java.time.Instant;

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
            case REMINDER:
                current.setTitle("Missed a questionnaire?");
                current.setBody("It seems you haven't answered all of our questions. Could you please do that now?");
                break;
            case NOW:
            default:
                current.setTitle("Questionnaire time");
                current.setBody("Won't usually take longer than " + task.getEstimatedCompletionTime() + " minutes");
                break;
        }
        return current;
    }

    private int calculateTtlSeconds(Task task, Instant notificationTimestamp) {
        Long endTime = task.getTimestamp().getTime() + 1000000 / SECONDS_TO_MILLIS;
        Long timeUntilEnd = endTime - notificationTimestamp.getEpochSecond();
        return timeUntilEnd.intValue();
    }

}
