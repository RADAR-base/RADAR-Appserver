package org.radarbase.appserver.service.questionnaire.notification;

import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.entity.Task;
import org.radarbase.appserver.dto.protocol.LanguageText;

import java.time.Instant;

public class TaskNotificationGeneratorService {
    private transient int SECONDS_TO_MILLIS = 1000;

    public Notification createNotification(Task task, Instant notificationTimestamp, String title, String body) {

        Notification.NotificationBuilder current = new Notification.NotificationBuilder();
        current.scheduledTime(notificationTimestamp);
        current.ttlSeconds(calculateTtlSeconds(task, notificationTimestamp));
        current.type(task.getName());
        current.sourceType("Type");
        current.sourceId("id");
        current.appPackage("org.phidatalab.radar-armt");
        current.task(task);
        current.title(title);
        current.body(body);

        return current.build();
    }

    public String getTitleText(String language, LanguageText title, NotificationType type) {
        if (title == null) {
            switch (type) {
                case REMINDER:
                    return "Missed a questionnaire?";
                case NOW:
                default:
                    return "Questionnaire time";
            }
        }
        return title.getText(language);
    }

    public String getBodyText(String language, LanguageText body, NotificationType type, int time) {
        if (body == null) {
            switch (type) {
                case REMINDER:
                    return "It seems you haven't answered all of our questions. Could you please do that now?";
                case NOW:
                default:
                    return "Won't usually take longer than " + time + " minutes";
            }
        }
        return body.getText(language);
    }

    private int calculateTtlSeconds(Task task, Instant notificationTimestamp) {
        Long endTime = task.getTimestamp().getTime() + task.getCompletionWindow();
        Long timeUntilEnd = endTime - notificationTimestamp.toEpochMilli();
        return timeUntilEnd.intValue();
    }

}
