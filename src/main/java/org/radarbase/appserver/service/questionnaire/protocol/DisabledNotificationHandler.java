package org.radarbase.appserver.service.questionnaire.protocol;

import org.radarbase.appserver.dto.protocol.Assessment;
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.entity.Task;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.service.questionnaire.notification.TaskNotificationGeneratorService;
import org.radarbase.appserver.service.questionnaire.notification.NotificationType;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Collections;

public class DisabledNotificationHandler implements ProtocolHandler {
    public DisabledNotificationHandler() { }

    @Override
    public AssessmentSchedule handle(AssessmentSchedule assessmentSchedule, Assessment assessment, User user) {
        List<Notification> notifications = Collections.emptyList();
        assessmentSchedule.setNotifications(notifications);
        return assessmentSchedule;
    }

}
