package org.radarbase.appserver.jersey.dto.questionnaire

import org.radarbase.appserver.jersey.entity.Notification
import org.radarbase.appserver.jersey.entity.Task
import java.time.Instant

data class AssessmentSchedule(
    var name: String? = null,
    var referenceTimestamp: Instant? = null,
    var referenceTimestamps: List<Instant>? = null,
    var tasks: List<Task>? = null,
    var notifications: List<Notification>? = null,
    var reminders: List<Notification>? = null,
) {
    fun hasTasks(): Boolean {
        return !tasks.isNullOrEmpty()
    }
}
