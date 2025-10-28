package org.radarbase.appserver.microservices.core.service

import org.radarbase.appserver.microservices.core.dto.TaskStateEventDto
import org.radarbase.appserver.microservices.core.entity.TaskStateEvent
import javax.naming.SizeLimitExceededException

interface TaskStateEventService {
    suspend fun addTaskStateEvent(taskStateEvent: TaskStateEvent)

    suspend fun getTaskStateEvents(projectId: String?, subjectId: String?, taskId: Long): List<TaskStateEventDto>

    suspend fun getTaskStateEventsByTaskId(taskId: Long): List<TaskStateEventDto>

    @Throws(SizeLimitExceededException::class)
    suspend fun publishNotificationStateEventExternal(
        projectId: String,
        subjectId: String,
        taskId: Long,
        taskStateEventDto: TaskStateEventDto,
    )
}
