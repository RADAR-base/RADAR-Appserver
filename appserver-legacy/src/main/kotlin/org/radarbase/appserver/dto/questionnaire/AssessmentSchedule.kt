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

package org.radarbase.appserver.dto.questionnaire

import org.radarbase.appserver.entity.Notification
import org.radarbase.appserver.entity.Task
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
