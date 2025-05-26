package org.radarbase.appserver.jersey.service.protocol.task

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
