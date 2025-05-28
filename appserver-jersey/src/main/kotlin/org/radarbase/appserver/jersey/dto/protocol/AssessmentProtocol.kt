package org.radarbase.appserver.jersey.dto.protocol

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import org.radarbase.appserver.jersey.utils.deserializer.ReferenceTimestampDeserializer

data class AssessmentProtocol(
    var repeatProtocol: RepeatProtocol? = null,
    var reminders: ReminderTimePeriod? = null,
    var completionWindow: TimePeriod? = null,
    var repeatQuestionnaire: RepeatQuestionnaire? = null,
    var referenceTimestamp: ReferenceTimestamp? = null,
    var clinicalProtocol: ClinicalProtocol? = null,
    var notification: NotificationProtocol = NotificationProtocol(),
) {
    @JsonDeserialize(using = ReferenceTimestampDeserializer::class)
    fun setReferenceTimestamp(responseObject: Any?) {
        if (responseObject is ReferenceTimestamp) {
            this.referenceTimestamp = responseObject
        }
    }
}
