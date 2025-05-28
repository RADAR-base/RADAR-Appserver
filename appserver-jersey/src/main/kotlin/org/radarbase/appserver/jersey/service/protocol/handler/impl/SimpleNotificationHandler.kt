/*
 * Copyright 2025 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
