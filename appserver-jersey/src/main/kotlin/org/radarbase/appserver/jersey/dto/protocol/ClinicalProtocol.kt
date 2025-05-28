package org.radarbase.appserver.jersey.dto.protocol

data class ClinicalProtocol(
    var requiresInClinicCompletion: Boolean = false,
    var repeatAfterClinicVisit: RepeatQuestionnaire? = null,
)
