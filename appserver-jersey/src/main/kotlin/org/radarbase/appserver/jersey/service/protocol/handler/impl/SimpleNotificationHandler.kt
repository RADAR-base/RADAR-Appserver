package org.radarbase.appserver.jersey.service.protocol.handler.impl

import kotlinx.coroutines.Dispatchers
import org.radarbase.appserver.jersey.dto.protocol.Assessment
import org.radarbase.appserver.jersey.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.jersey.entity.Notification
import org.radarbase.appserver.jersey.entity.Task
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.service.protocol.handler.ProtocolHandler
import org.radarbase.appserver.jersey.service.questionnaire_schedule.notification.NotificationType
import org.radarbase.appserver.jersey.service.questionnaire_schedule.notification.TaskNotificationGeneratorService
import org.radarbase.appserver.jersey.utils.mapParallel
import java.time.Instant
import java.time.temporal.ChronoUnit

class SimpleNotificationHandler : ProtocolHandler {
    private val taskNotificationGeneratorService = TaskNotificationGeneratorService()

    override suspend fun handle(
        assessmentSchedule: AssessmentSchedule,
        assessment: Assessment,
        user: User,
    ): AssessmentSchedule {
        val notificationProtocol = assessment.protocol?.notification
        val estimatedCompletionTime = assessment.estimatedCompletionTime
        val assessmentScheduleTasks: List<Task>? = assessmentSchedule.tasks

        if (notificationProtocol == null || estimatedCompletionTime == null || assessmentScheduleTasks == null) {
            return assessmentSchedule.also {
                it.notifications = emptyList()
            }
        }

        val title = this.taskNotificationGeneratorService.getTitleText(
            user.language,
            notificationProtocol.title,
            NotificationType.NOW,
        )
        val body = this.taskNotificationGeneratorService.getBodyText(
            user.language,
            notificationProtocol.body,
            NotificationType.NOW,
            estimatedCompletionTime,
        )

        generateNotifications(
            assessmentScheduleTasks,
            user,
            title,
            body,
            notificationProtocol.email.enabled,
        ).also { notifications ->
            assessmentSchedule.notifications = notifications
        }

        return assessmentSchedule
    }

    suspend fun generateNotifications(
        tasks: List<Task>, user: User,
        title: String, body: String, emailEnabled: Boolean,
    ): List<Notification> {
        return tasks.mapParallel(Dispatchers.Default) { task: Task ->
            task.timestamp?.let { taskTimeStamp ->
                this.taskNotificationGeneratorService.createNotification(
                    task,
                    taskTimeStamp.toInstant(),
                    title,
                    body,
                    emailEnabled,
                ).apply {
                    this.user = user
                }
            }
        }.filterNotNull().filter { notification: Notification ->
            val scheduledTime = notification.scheduledTime
            val ttlSeconds = notification.ttlSeconds

            scheduledTime != null && Instant.now().isBefore(
                scheduledTime
                    .plus(ttlSeconds.toLong(), ChronoUnit.SECONDS),
            )
        }
    }
}
