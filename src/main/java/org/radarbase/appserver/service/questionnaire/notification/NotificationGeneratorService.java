package org.radarbase.appserver.service.questionnaire.notification;

import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.entity.Task;

import java.time.Instant;

public class NotificationGeneratorService {
    private transient int SECONDS_TO_MILLIS = 1000;

    public Notification createNotification(Task task, NotificationType type, Instant notificationTimestamp) {

        Notification.NotificationBuilder current = new Notification.NotificationBuilder();
        current.scheduledTime(notificationTimestamp);
        current.ttlSeconds(calculateTtlSeconds(task, notificationTimestamp));
        current.type(task.getName());
        current.sourceType("Type");
        current.sourceId("id");
        current.appPackage("org.phidatalab.radar-armt");
        current.task(task);

        switch (type) {
            case REMINDER:
                current.title("Missed a questionnaire?");
                current.body("It seems you haven't answered all of our questions. Could you please do that now?");
                break;
            case NOW:
            default:
                current.title("Questionnaire time");
                current.body("Won't usually take longer than " + task.getEstimatedCompletionTime() + " minutes");
                break;
        }
        return current.build();
    }

    private int calculateTtlSeconds(Task task, Instant notificationTimestamp) {
        Long endTime = task.getTimestamp().getTime() + 1000000 / SECONDS_TO_MILLIS;
        Long timeUntilEnd = endTime - notificationTimestamp.getEpochSecond();
        return timeUntilEnd.intValue();
    }

}
