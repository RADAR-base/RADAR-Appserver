package org.radarbase.appserver.jersey.service.protocol.handler.impl

import org.radarbase.appserver.jersey.dto.protocol.Assessment
import org.radarbase.appserver.jersey.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.service.protocol.handler.ProtocolHandler

/**
 * Handles the processing and management of clinical protocols.
 */
class ClinicalProtocolHandler : ProtocolHandler {
    /**
     * Handles the processing of an assessment schedule by updating its name based on the provided assessment.
     *
     * @param assessmentSchedule The assessment schedule to be updated.
     * @param assessment The assessment containing details used to update the schedule.
     * @param user The user associated with the assessment. This parameter may be null.
     * @return The updated assessment schedule with the name set based on the assessment's name.
     */
    override suspend fun handle(
        assessmentSchedule: AssessmentSchedule,
        assessment: Assessment,
        user: User
    ): AssessmentSchedule {
        assessmentSchedule.name = assessment.name
        return assessmentSchedule
    }
}
