package org.radarbase.appserver.service.questionnaire.protocol

import org.radarbase.appserver.dto.protocol.Assessment
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.entity.Notification
import org.radarbase.appserver.entity.Task
import org.radarbase.appserver.entity.User
import org.radarbase.appserver.service.questionnaire.notification.NotificationType
import org.radarbase.appserver.service.questionnaire.notification.TaskNotificationGeneratorService
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.function.IntFunction
import java.util.stream.Collectors
import java.util.stream.IntStream

class SimpleReminderHandler : ProtocolHandler {
    @Transient
    private val taskNotificationGeneratorService = TaskNotificationGeneratorService()

    @Transient
    private val timeCalculatorService = TimeCalculatorService()

    override fun handle(
        assessmentSchedule: AssessmentSchedule,
        assessment: Assessment,
        user: User
    ): AssessmentSchedule {
        val timezone = TimeZone.getTimeZone(user.timezone)
        val protocol = assessment.protocol?.notification
        val assessmentScheduleTasks: List<Task>? = assessmentSchedule.tasks
        val estimatedCompletionTime = assessment.estimatedCompletionTime

        if (protocol == null || assessmentScheduleTasks == null || estimatedCompletionTime == null) {
            return assessmentSchedule.also {
                it.reminders = emptyList()
            }
        }

        val title =
            this.taskNotificationGeneratorService.getTitleText(user.language, protocol.title, NotificationType.REMINDER)
        val body = this.taskNotificationGeneratorService.getBodyText(
            user.language,
            protocol.body,
            NotificationType.REMINDER,
            estimatedCompletionTime
        )

        val notifications = generateReminders(
            assessmentScheduleTasks,
            assessment,
            timezone,
            user,
            title,
            body,
            protocol.email.enabled
        )
        assessmentSchedule.reminders = notifications
        return assessmentSchedule
    }

    fun generateReminders(
        tasks: List<Task>, assessment: Assessment, timezone: TimeZone,
        user: User, title: String, body: String, emailEnabled: Boolean
    ): List<Notification> {
        return tasks.parallelStream()
            .flatMap { task: Task ->
                val reminders = assessment.protocol?.reminders  ?: return@flatMap null
                val repeatReminders = reminders.repeat ?: return@flatMap null

                (0 until repeatReminders).map { _: Int ->
                    val timestamp = timeCalculatorService.advanceRepeat(task.timestamp!!.toInstant(), reminders, timezone)
                    taskNotificationGeneratorService.createNotification(
                        task, timestamp, title, body, emailEnabled
                    ).also {
                        it.user = user
                    }
                }.stream()
            }
            .filter { notification ->
                (Instant.now().isBefore(
                    notification.scheduledTime!!
                        .plus(notification.ttlSeconds.toLong(), ChronoUnit.SECONDS)
                ))
            }.collect(Collectors.toList())
    }
}
