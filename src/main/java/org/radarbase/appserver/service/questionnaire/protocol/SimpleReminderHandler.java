package org.radarbase.appserver.service.questionnaire.protocol;

import org.radarbase.appserver.dto.protocol.Assessment;
import org.radarbase.appserver.dto.protocol.ReminderTimePeriod;
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
import java.util.TimeZone;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SimpleReminderHandler implements ProtocolHandler {
    private transient TaskNotificationGeneratorService taskNotificationGeneratorService = new TaskNotificationGeneratorService();
    private transient TimeCalculatorService timeCalculatorService = new TimeCalculatorService();

    public SimpleReminderHandler() { }

    @Override
    public AssessmentSchedule handle(AssessmentSchedule assessmentSchedule, Assessment assessment, User user) {
        TimeZone timezone = TimeZone.getTimeZone(user.getTimezone());
        NotificationProtocol protocol = assessment.getProtocol().getNotification();
        String title = this.taskNotificationGeneratorService.getTitleText(user.getLanguage(), protocol.getTitle(), NotificationType.REMINDER);
        String body = this.taskNotificationGeneratorService.getBodyText(user.getLanguage(), protocol.getBody(), NotificationType.REMINDER, assessment.getEstimatedCompletionTime());
        List<Notification> notifications = generateReminders(assessmentSchedule.getTasks(), assessment, timezone, user, title, body);
        assessmentSchedule.setReminders(notifications);
        return assessmentSchedule;
    }

    public List<Notification> generateReminders(List<Task> tasks, Assessment assessment, TimeZone timezone, User user, String title, String body) {
        return tasks.parallelStream()
                .flatMap(task -> {
                    ReminderTimePeriod reminders = assessment.getProtocol().getReminders();
                    return IntStream.range(0, reminders.getRepeat()).mapToObj(i -> {
                        Instant timestamp = timeCalculatorService.advanceRepeat(task.getTimestamp().toInstant(), reminders, timezone);
                        Notification notification = this.taskNotificationGeneratorService.createNotification(task, timestamp, title, body);
                        notification.setUser(user);
                        return notification;
                    });
                        }
                )
                .filter(notification-> (Instant.now().isBefore(notification.getScheduledTime()
                        .plus(notification.getTtlSeconds(), ChronoUnit.SECONDS)))).collect(Collectors.toList());
    }

}
