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

package org.radarbase.appserver.microservices.core.dto

import com.fasterxml.jackson.annotation.JsonFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.radarbase.appserver.microservices.core.dto.protocol.AssessmentType
import org.radarbase.appserver.microservices.core.entity.Task
import org.radarbase.appserver.microservices.core.event.state.TaskState
import org.radarbase.appserver.microservices.core.serialization.InstantSerializer
import java.time.Instant

@Suppress("unused")
@Serializable
class TaskDto(
    @Transient private val taskEntity: Task? = null,
) {
    var id: Long? = taskEntity?.id

    var completed: Boolean = taskEntity?.completed == true

    @Serializable(with = InstantSerializer::class)
    var timestamp: Instant? = taskEntity?.timestamp?.toInstant()

    var name: String? = taskEntity?.name

    var type: AssessmentType? = taskEntity?.type

    var estimatedCompletionTime: Int? = taskEntity?.estimatedCompletionTime

    var completionWindow: Long? = taskEntity?.completionWindow

    var warning: String? = taskEntity?.warning

    var isClinical: Boolean? = taskEntity?.isClinical

    @JsonFormat(shape = JsonFormat.Shape.NUMBER)
    @Serializable(with = InstantSerializer::class)
    var timeCompleted: Instant? = taskEntity?.timeCompleted?.toInstant()

    var showInCalendar: Boolean? = taskEntity?.showInCalendar

    var isDemo: Boolean? = taskEntity?.isDemo

    var priority: Int = taskEntity?.priority ?: 0

    var nQuestions: Int = taskEntity?.nQuestions ?: 0

    var status: TaskState = taskEntity?.status ?: TaskState.UNKNOWN
}
