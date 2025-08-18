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

package org.radarbase.appserver.jersey.event.state.dto

import org.radarbase.appserver.jersey.entity.Task
import org.radarbase.appserver.jersey.event.state.TaskState
import org.radarbase.appserver.jersey.utils.stringRepresentation
import java.time.Instant

class TaskStateEventDto(
    val task: Task?,
    val state: TaskState?,
    val additionalInfo: Map<String, String>?,
    val time: Instant?,
) {

    override fun toString(): String = stringRepresentation(
        TaskStateEventDto::task,
        TaskStateEventDto::state,
        TaskStateEventDto::additionalInfo,
        TaskStateEventDto::time,
    )
}
