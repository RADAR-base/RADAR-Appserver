/*
 *  Copyright 2018 King's College London
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.radarbase.appserver.service.questionnaire.notification

import org.radarbase.appserver.dto.protocol.LanguageText
import org.radarbase.appserver.entity.Notification
import org.radarbase.appserver.entity.Notification.NotificationBuilder
import org.radarbase.appserver.entity.Task
import java.time.Instant

class TaskNotificationGeneratorService {

    fun createNotification(
        task: Task, notificationTimestamp: Instant,
        title: String?, body: String?, emailEnabled: Boolean
    ): Notification {
        return NotificationBuilder().apply {
            scheduledTime(notificationTimestamp)
            ttlSeconds(calculateTtlSeconds(task, notificationTimestamp))
            type(task.name)
            sourceType("Type")
            sourceId("id")
            appPackage("org.phidatalab.radar-armt")
            task(task)
            title(title)
            body(body)
            emailEnabled(emailEnabled)
        }.build()
    }

    fun getTitleText(language: String?, title: LanguageText?, type: NotificationType): String {
        return when {
            title != null -> title.getText(language)
            else -> when (type) {
                NotificationType.REMINDER -> "Missed a questionnaire?"
                else -> "Questionnaire time!!"
            }
        }
    }

    fun getBodyText(language: String?, body: LanguageText?, type: NotificationType, time: Int): String {
        return when {
            body != null -> body.getText(language)
            else -> when (type) {
                NotificationType.REMINDER -> "It seems you haven't answered all of our questions. Could you please do that now?"
                else -> "Won't usually take longer than $time minutes"
            }
        }
    }

    private fun calculateTtlSeconds(task: Task, notificationTimestamp: Instant): Int {
        val endTime = task.timestamp!!.getTime() + task.completionWindow!!
        val timeUntilEnd = endTime - notificationTimestamp.toEpochMilli()
        return timeUntilEnd.toInt()
    }
}
