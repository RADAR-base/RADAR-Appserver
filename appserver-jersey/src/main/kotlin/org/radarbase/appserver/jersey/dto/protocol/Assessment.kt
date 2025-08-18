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

package org.radarbase.appserver.jersey.dto.protocol

import jakarta.persistence.Column
import kotlinx.serialization.Serializable

/**
 * Data Transfer object (DTO) for Assessment. A project may represent a Protocol for scheduling
 * questionnaires.
 *
 * @see <a href="https://github.com/RADAR-base/RADAR-aRMT-protocols">aRMT Protocols</a>
 * @see Protocol
 */

@Serializable
data class Assessment(
    var name: String? = null,
    private var _type: AssessmentType? = null,
    var showIntroduction: String? = null,
    var questionnaire: DefinitionInfo? = null,
    var startText: LanguageText? = null,
    var endText: LanguageText? = null,
    var warn: LanguageText? = null,
    var estimatedCompletionTime: Int? = null,
    var protocol: AssessmentProtocol? = null,
    @Column(name = "\"order\"")
    var order: Int = 0,
    var nQuestions: Int = 0,
    var showInCalendar: Boolean = true,
    var isDemo: Boolean = false,
) {
    var type: AssessmentType?
        get() {
            return _type
                ?: if (protocol?.clinicalProtocol != null) AssessmentType.CLINICAL else AssessmentType.SCHEDULED
        }
        set(value) {
            _type = value
        }
}
