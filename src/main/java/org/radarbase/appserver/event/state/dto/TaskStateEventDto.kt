package org.radarbase.appserver.event.state.dto

import org.radarbase.appserver.entity.Task
import org.radarbase.appserver.event.state.TaskState
import org.radarbase.appserver.util.stringRepresentation
import org.springframework.context.ApplicationEvent
import java.io.Serial
import java.time.Instant

class TaskStateEventDto(
    source: Any,
    val task: Task,
    val state: TaskState,
    val additionalInfo: MutableMap<String, String>?,
    val time: Instant
) : ApplicationEvent(source) {

    override fun toString(): String = stringRepresentation(
        TaskStateEventDto::task,
        TaskStateEventDto::state,
        TaskStateEventDto::additionalInfo,
        TaskStateEventDto::time
    )

    companion object {
        @Serial
        private const val serialVersionUID = 327842183571948L
    }
}
