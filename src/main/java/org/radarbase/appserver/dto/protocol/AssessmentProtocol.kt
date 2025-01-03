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

package org.radarbase.appserver.dto.protocol

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize

/**
 * Data Transfer object (DTO) for AssessmentProtocol. Handles details of scheduling protocols.
 *
 * @author yatharthranjan
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class AssessmentProtocol(
    var repeatProtocol: RepeatProtocol? = null,
    var reminders: ReminderTimePeriod? = null,
    var completionWindow: TimePeriod? = null,
    var repeatQuestionnaire: RepeatQuestionnaire? = null,
    var referenceTimestamp: ReferenceTimestamp? = null,
    var clinicalProtocol: ClinicalProtocol? = null,
    var notification: NotificationProtocol = NotificationProtocol()
) {
    @JsonDeserialize(using = ReferenceTimestampDeserializer::class)
    fun setReferenceTimestamp(responseObject: Any?) {
        if (responseObject is ReferenceTimestamp) {
            this.referenceTimestamp = responseObject
        }
    }
}
