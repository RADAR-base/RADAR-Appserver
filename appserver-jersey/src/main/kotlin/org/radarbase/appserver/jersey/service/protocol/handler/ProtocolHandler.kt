package org.radarbase.appserver.jersey.service.protocol.handler

import org.radarbase.appserver.jersey.dto.protocol.Assessment
import org.radarbase.appserver.jersey.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.jersey.entity.User

/**
 * Interface for handling protocol-related tasks. Implementations are responsible for processing
 * and updating an [AssessmentSchedule] based on the associated [protocol][Assessment] details.
 */
interface ProtocolHandler {
    /**
     * Processes the given assessment schedule and updates it with appropriate data based on the input parameters.
     *
     * @param assessmentSchedule The assessment schedule that will be updated.
     * @param assessment The assessment containing protocol details and metadata used for processing.
     * @param user The user whose information, such as timezone and enrolment date, is utilized in the processing.
     * @return The updated [AssessmentSchedule] containing the results of the handling process.
     */
    fun handle(assessmentSchedule: AssessmentSchedule, assessment: Assessment, user: User): AssessmentSchedule
}
