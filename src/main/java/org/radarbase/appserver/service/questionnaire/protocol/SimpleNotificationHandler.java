package org.radarbase.appserver.service.questionnaire.protocol;

import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.radarbase.appserver.dto.fcm.FcmNotifications;
import org.radarbase.appserver.dto.protocol.Assessment;
import org.radarbase.appserver.dto.protocol.TimePeriod;
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule;
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
        FcmNotifications notifications = generateNotifications(assessmentSchedule.getTasks());
        this.notificationService.addNotifications(notifications, user.getSubjectId(), user.getProject().getProjectId());
        return assessmentSchedule;
    }

    public FcmNotifications generateNotifications(List<Task> tasks) {
        List<FcmNotificationDto> notifications = tasks.parallelStream()
                .map(task ->
                this.notificationGeneratorService.createNotification(task, NotificationType.NOW, task.getTimestamp().toInstant())
                ).filter(notification-> (Instant.now().isBefore(notification.getScheduledTime()))).collect(Collectors.toList());


        return new FcmNotifications(notifications);
    }

}
