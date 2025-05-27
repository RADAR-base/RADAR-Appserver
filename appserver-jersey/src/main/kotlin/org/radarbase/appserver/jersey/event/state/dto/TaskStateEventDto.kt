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
