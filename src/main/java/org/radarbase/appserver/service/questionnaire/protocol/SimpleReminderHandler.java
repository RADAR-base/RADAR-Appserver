package org.radarbase.appserver.service.questionnaire.protocol;

import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.radarbase.appserver.dto.fcm.FcmNotifications;
import org.radarbase.appserver.dto.protocol.Assessment;
import org.radarbase.appserver.dto.protocol.ReminderTimePeriod;
import org.radarbase.appserver.dto.protocol.TimePeriod;
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule;
import org.radarbase.appserver.entity.Task;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.service.FcmNotificationService;
import org.radarbase.appserver.service.questionnaire.notification.NotificationGeneratorService;
import org.radarbase.appserver.service.questionnaire.notification.NotificationType;

import java.time.Instant;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SimpleReminderHandler implements ProtocolHandler {
    private transient NotificationGeneratorService notificationGeneratorService = new NotificationGeneratorService();
    private transient TimeCalculatorService timeCalculatorService = new TimeCalculatorService();

    private transient FcmNotificationService notificationService;

    public SimpleReminderHandler(FcmNotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @Override
    public AssessmentSchedule handle(AssessmentSchedule assessmentSchedule, Assessment assessment, User user) {
        TimeZone timezone = TimeZone.getTimeZone(user.getTimezone());
        FcmNotifications notifications = generateReminders(assessmentSchedule.getTasks(), assessment, timezone);
        this.notificationService.addNotifications(notifications, user.getSubjectId(), user.getProject().getProjectId());
        return assessmentSchedule;
    }

    public FcmNotifications generateReminders(List<Task> tasks, Assessment assessment, TimeZone timezone) {
        List<FcmNotificationDto> notifications = tasks.parallelStream()
                .flatMap(task -> {
                    ReminderTimePeriod reminders = assessment.getProtocol().getReminders();
                    return IntStream.range(0, reminders.getRepeat()).mapToObj(i -> {
                        Instant timestamp = timeCalculatorService.advanceRepeat(task.getTimestamp().toInstant(), reminders, timezone);
                        return this.notificationGeneratorService.createNotification(task, NotificationType.REMINDER, timestamp);
                    });
                        }
                ).collect(Collectors.toList());

        return new FcmNotifications(notifications);
    }

}
