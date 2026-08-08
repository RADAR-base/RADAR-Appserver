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

package org.radarbase.appserver.jersey.dto.protocol

import kotlinx.serialization.Serializable
import org.radarbase.appserver.jersey.serialization.ReferenceTimestampSerializer

@Serializable
data class AssessmentProtocol(
    var repeatProtocol: RepeatProtocol? = null,
    var reminders: ReminderTimePeriod? = null,
    var completionWindow: TimePeriod? = null,
    var repeatQuestionnaire: RepeatQuestionnaire? = null,
    @Serializable(with = ReferenceTimestampSerializer::class)
    var referenceTimestamp: ReferenceTimestamp? = null,
    var clinicalProtocol: ClinicalProtocol? = null,
    var notification: NotificationProtocol = NotificationProtocol(),
)
