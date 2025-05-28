package org.radarbase.appserver.jersey.dto.protocol

/**
 * Data Transfer object (DTO) for Protocol. A project may represent a `Protocol` for scheduling
 * questionnaires.
 *
 * @see <a href="https://github.com/RADAR-base/RADAR-aRMT-protocols">aRMT Protocols</a>
 */
data class Protocol(
    var version: String? = null,
    var schemaVersion: String? = null,
    var name: String? = null,
    var healthIssues: List<String>? = null,
    var protocols: List<Assessment>? = null,
) {
    fun hasAssessment(assessment: String?): Boolean {
        return protocols?.any { it.name == assessment } == true
    }
}
