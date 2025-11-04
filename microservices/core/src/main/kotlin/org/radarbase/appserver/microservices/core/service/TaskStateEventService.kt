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

package org.radarbase.appserver.microservices.core.service

import org.radarbase.appserver.microservices.core.dto.TaskStateEventDto
import org.radarbase.appserver.microservices.core.entity.TaskStateEvent
import javax.naming.SizeLimitExceededException

interface TaskStateEventService {
    suspend fun addTaskStateEvent(taskStateEvent: TaskStateEvent)

    suspend fun getTaskStateEvents(projectId: String?, subjectId: String?, taskId: Long): List<TaskStateEventDto>

    suspend fun getTaskStateEventsByTaskId(taskId: Long): List<TaskStateEventDto>

    @Throws(SizeLimitExceededException::class)
    suspend fun publishTaskStateEventExternal(
        projectId: String,
        subjectId: String,
        taskId: Long,
        taskStateEventDto: TaskStateEventDto,
    )
}
