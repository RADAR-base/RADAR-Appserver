package org.radarbase.appserver.service.questionnaire.protocol;

import org.radarbase.appserver.dto.protocol.Assessment;
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule;
import org.radarbase.appserver.dto.protocol.NotificationProtocol;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.entity.Task;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.service.questionnaire.notification.TaskNotificationGeneratorService;
import org.radarbase.appserver.service.questionnaire.notification.NotificationType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleNotificationHandler implements ProtocolHandler {
    private transient TaskNotificationGeneratorService taskNotificationGeneratorService = new TaskNotificationGeneratorService();

    public SimpleNotificationHandler() { }

    @Override
    public AssessmentSchedule handle(AssessmentSchedule assessmentSchedule, Assessment assessment, User user) {
        NotificationProtocol protocol = assessment.getProtocol().getNotification();
        String title = this.taskNotificationGeneratorService.getTitleText(user.getLanguage(), protocol.getTitle(), NotificationType.NOW);
        String body = this.taskNotificationGeneratorService.getBodyText(user.getLanguage(), protocol.getBody(), NotificationType.NOW, assessment.getEstimatedCompletionTime());
        List<Notification> notifications = generateNotifications(assessmentSchedule.getTasks(), user, title, body);
        assessmentSchedule.setNotifications(notifications);
        return assessmentSchedule;
    }

    public List<Notification> generateNotifications(List<Task> tasks, User user, String title, String body) {
        return tasks.parallelStream()
                .map(task -> {
                        Notification notification = this.taskNotificationGeneratorService
                                .createNotification(
                                        task,
                                        task.getTimestamp().toInstant(),
                                        title,
                                        body);
                        notification.setUser(user);
                        return notification;
                })
                .filter(notification-> (Instant.now().isBefore(notification.getScheduledTime()
                        .plus(notification.getTtlSeconds(), ChronoUnit.SECONDS)))).collect(Collectors.toList());
    }

}