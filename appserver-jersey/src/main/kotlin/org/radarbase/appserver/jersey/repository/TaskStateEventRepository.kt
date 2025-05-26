package org.radarbase.appserver.jersey.repository

import org.radarbase.appserver.jersey.entity.TaskStateEvent

interface TaskStateEventRepository : BaseRepository<TaskStateEvent> {
    suspend fun findByTaskId(taskId: Long): List<TaskStateEvent>
    suspend fun countByTaskId(taskId: Long): Long
}
