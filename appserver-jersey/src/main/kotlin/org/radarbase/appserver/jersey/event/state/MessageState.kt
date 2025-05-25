package org.radarbase.appserver.jersey.event.state

enum class MessageState {
    // Database controlled
    ADDED, UPDATED, CANCELLED,

    // Scheduler Controlled
    SCHEDULED,
    EXECUTED,

    // Controlled by entities outside the appserver.
    // These will need to be reported to the appserver.
    DELIVERED, OPENED, DISMISSED,

    // Misc
    ERRORED, UNKNOWN
}
