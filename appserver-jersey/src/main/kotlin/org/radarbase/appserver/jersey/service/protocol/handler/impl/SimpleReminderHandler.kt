package org.radarbase.appserver.jersey.service.protocol.handler.impl

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.radarbase.appserver.jersey.dto.protocol.Assessment
import org.radarbase.appserver.jersey.dto.protocol.TimePeriod
import org.radarbase.appserver.jersey.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.jersey.entity.Notification
import org.radarbase.appserver.jersey.entity.Task
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.service.protocol.handler.ProtocolHandler
import org.radarbase.appserver.jersey.service.protocol.time.TimeCalculatorService
import org.radarbase.appserver.jersey.service.questionnaire_schedule.notification.NotificationType
import org.radarbase.appserver.jersey.service.questionnaire_schedule.notification.TaskNotificationGeneratorService
import org.radarbase.appserver.jersey.utils.flatMapParallel
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.TimeZone

class SimpleReminderHandler : ProtocolHandler {
    private val taskNotificationGeneratorService = TaskNotificationGeneratorService()
    private val timeCalculatorService = TimeCalculatorService()

    override suspend fun handle(
        assessmentSchedule: AssessmentSchedule,
        assessment: Assessment,
        user: User,
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

        val title = this.taskNotificationGeneratorService.getTitleText(
            user.language,
            protocol.title,
            NotificationType.REMINDER,
        )
        val body = this.taskNotificationGeneratorService.getBodyText(
            user.language,
            protocol.body,
            NotificationType.REMINDER,
            estimatedCompletionTime,
        )

        val notifications = generateReminders(
            assessmentScheduleTasks,
            assessment,
            timezone,
            user,
            title,
            body,
            protocol.email.enabled,
        )
        assessmentSchedule.reminders = notifications
        return assessmentSchedule
    }

    suspend fun generateReminders(
        tasks: List<Task>, assessment: Assessment, timezone: TimeZone,
        user: User, title: String, body: String, emailEnabled: Boolean,
    ): List<Notification> = coroutineScope {
        tasks.flatMapParallel { task: Task ->
            val reminders =     assessment.protocol?.reminders ?: return@flatMapParallel emptyList()
            val repeatReminders = reminders.repeat ?: return@flatMapParallel emptyList()

            (1..repeatReminders).map { repeat: Int ->
                async {
                    val offset = TimePeriod(reminders.unit, reminders.amount!! * repeat)

                    val timestamp = timeCalculatorService.advanceRepeat(task.timestamp!!.toInstant(), offset, timezone)
                    taskNotificationGeneratorService.createNotification(
                        task, timestamp, title, body, emailEnabled,
                    ).also {
                        it.user = user
                    }
                }
            }.awaitAll()
        }.filter { notification ->
            (Instant.now().isBefore(
                notification.scheduledTime!!
                    .plus(notification.ttlSeconds.toLong(), ChronoUnit.SECONDS),
            ))
        }
    }
}
