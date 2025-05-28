package org.radarbase.appserver.jersey.dto.protocol

import jakarta.persistence.Column

/**
 * Data Transfer object (DTO) for Assessment. A project may represent a Protocol for scheduling
 * questionnaires.
 *
 * @see <a href="https://github.com/RADAR-base/RADAR-aRMT-protocols">aRMT Protocols</a>
 * @see Protocol
 */

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
