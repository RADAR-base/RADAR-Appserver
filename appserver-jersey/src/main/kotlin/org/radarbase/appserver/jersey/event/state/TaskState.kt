package org.radarbase.appserver.jersey.event.state

enum class TaskState {
    ADDED, UPDATED, CANCELLED, SCHEDULED,
    COMPLETED,
    ERRORED, UNKNOWN
}
