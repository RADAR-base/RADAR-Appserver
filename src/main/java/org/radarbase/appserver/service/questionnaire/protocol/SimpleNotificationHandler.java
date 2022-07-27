package org.radarbase.appserver.service.questionnaire.protocol;

import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.radarbase.appserver.dto.fcm.FcmNotifications;
import org.radarbase.appserver.dto.protocol.Assessment;
import org.radarbase.appserver.dto.protocol.TimePeriod;
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.entity.Task;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.service.FcmNotificationService;
import org.radarbase.appserver.service.questionnaire.notification.NotificationGeneratorService;
import org.radarbase.appserver.service.questionnaire.notification.NotificationType;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleNotificationHandler implements ProtocolHandler {
    private transient NotificationGeneratorService notificationGeneratorService = new NotificationGeneratorService();
    private transient FcmNotificationService notificationService;

    public SimpleNotificationHandler(FcmNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public AssessmentSchedule handle(AssessmentSchedule assessmentSchedule, Assessment assessment, User user) {
        List<Notification> notifications = generateNotifications(assessmentSchedule.getTasks(), user);
        this.notificationService.addNotifications(notifications, user);
        return assessmentSchedule;
    }

    public List<Notification> generateNotifications(List<Task> tasks, User user) {
        List<Notification> notifications = tasks.parallelStream()
                .map(task ->{
                        Notification notification = this.notificationGeneratorService
                                .createNotification(task, NotificationType.NOW, task.getTimestamp().toInstant());
                        notification.setUser(user);
                        return notification;
                })
                .filter(notification-> (Instant.now().isBefore(notification.getScheduledTime()))).collect(Collectors.toList());

        return notifications;
    }

}
