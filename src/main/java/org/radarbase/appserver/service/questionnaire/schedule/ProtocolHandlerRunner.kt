package org.radarbase.appserver.service.questionnaire.schedule

import org.radarbase.appserver.dto.protocol.Assessment
import org.radarbase.appserver.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.entity.User
import org.radarbase.appserver.service.questionnaire.protocol.ProtocolHandler

class ProtocolHandlerRunner {
    @Transient
    private val protocolHandlers: MutableList<ProtocolHandler> = ArrayList<ProtocolHandler>()

    fun runProtocolHandlers(assessment: Assessment, user: User): AssessmentSchedule {
        var assessmentSchedule = AssessmentSchedule()
        for (leaf in this.protocolHandlers) {
            assessmentSchedule = leaf.handle(assessmentSchedule, assessment, user)
        }
        return assessmentSchedule
    }

    fun addProtocolHandler(protocolHandler: ProtocolHandler?) {
        if (protocolHandler != null) this.protocolHandlers.add(protocolHandler)
    }
}
