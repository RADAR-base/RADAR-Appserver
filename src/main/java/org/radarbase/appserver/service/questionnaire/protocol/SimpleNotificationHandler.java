package org.radarbase.appserver.service.questionnaire.protocol;

import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.radarbase.appserver.dto.fcm.FcmNotifications;
import org.radarbase.appserver.dto.protocol.Assessment;
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule;
import org.radarbase.appserver.entity.Task;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.service.FcmNotificationService;
import org.radarbase.appserver.service.questionnaire.notification.NotificationGeneratorService;
import org.radarbase.appserver.service.questionnaire.notification.NotificationType;

import java.time.Instant;
import java.util.Iterator;
import java.util.List;

public class SimpleNotificationHandler implements ProtocolHandler {
    private transient NotificationGeneratorService notificationGeneratorService = new NotificationGeneratorService();
    private transient FcmNotificationService notificationService;

    public SimpleNotificationHandler(FcmNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public AssessmentSchedule handle(AssessmentSchedule assessmentSchedule, Assessment assessment, User user) {
        FcmNotifications notifications = generateNotifications(assessmentSchedule.getTasks());
        this.notificationService.addNotifications(notifications, user.getSubjectId(), user.getProject().getProjectId());
        return assessmentSchedule;
    }

    public FcmNotifications generateNotifications(List<Task> tasks) {
        Iterator<Task> tasksIter = tasks.iterator();
        FcmNotifications notifications = new FcmNotifications();
        while (tasksIter.hasNext()) {
            Task task = tasksIter.next();
            FcmNotificationDto defaultNotification = this.notificationGeneratorService.createNotification(task, NotificationType.NOW, task.getTimestamp());
            if (Instant.now().isBefore(defaultNotification.getScheduledTime()))
                notifications.addNotification(defaultNotification);
        }
        return notifications;
    }

}
