/*
 * Copyright 2025 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.appserver.jersey.service.questionnaire.schedule

import org.radarbase.appserver.jersey.dto.protocol.Assessment
import org.radarbase.appserver.jersey.dto.questionnaire.AssessmentSchedule
import org.radarbase.appserver.jersey.entity.User
import org.radarbase.appserver.jersey.service.protocol.handler.ProtocolHandler

/**
 * A class responsible for managing and executing protocol handlers for processing
 * and generating an updated assessment schedule. This class allows the dynamic
 * addition of protocol handlers which are executed sequentially to apply transformations
 * or updates on the assessment schedule.
 */
class ProtocolHandlerRunner {

    /**
     * Holds a mutable list of protocol handlers that can be dynamically added and used to process
     * various [ProtocolHandler] implementations.
     * The protocol handlers are executed sequentially, and each handler modifies the
     * [AssessmentSchedule] based on its logic.
     */
    private val protocolHandlers: MutableList<ProtocolHandler> = mutableListOf()

    /**
     * Executes all registered protocol handlers on the provided assessment and user data, sequentially updating
     * the `AssessmentSchedule`. Each protocol handler processes the schedule and applies its logic based on the
     * assessment and user details.
     *
     * @param assessment The assessment containing protocol details and metadata to be processed.
     * @param user The user whose information, such as timezone and enrolment date, is utilized during processing.
     * @return The final updated assessment schedule after being processed by all protocol handlers.
     */
    suspend fun runProtocolHandlers(assessment: Assessment, user: User): AssessmentSchedule {
        var assessmentSchedule = AssessmentSchedule()
        protocolHandlers.forEach { leaf: ProtocolHandler ->
            assessmentSchedule = leaf.handle(assessmentSchedule, assessment, user)
        }
        return assessmentSchedule
    }

    /**
     * Adds a `ProtocolHandler` to the list of protocol handlers if it is not null.
     *
     * @param protocolHandler The `ProtocolHandler` instance to be added. If the parameter is null,
     * no action is performed.
     */
    fun addProtocolHandler(protocolHandler: ProtocolHandler?) {
        if (protocolHandler != null) this.protocolHandlers.add(protocolHandler)
    }
}
