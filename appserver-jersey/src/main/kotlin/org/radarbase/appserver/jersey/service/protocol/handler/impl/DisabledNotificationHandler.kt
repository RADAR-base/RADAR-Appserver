package org.radarbase.appserver.jersey.service.protocol.handler.impl

class DisabledNotificationHandler : ProtocolHandler {
    override fun handle(
        assessmentSchedule: AssessmentSchedule,
        assessment: Assessment,
        user: User
    ): AssessmentSchedule {
        return assessmentSchedule.also {
            it.notifications = emptyList()
        }
    }
}
