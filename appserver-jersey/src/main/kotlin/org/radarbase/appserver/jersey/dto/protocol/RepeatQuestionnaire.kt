package org.radarbase.appserver.jersey.dto.protocol

import jakarta.validation.constraints.NotNull
import org.radarbase.appserver.jersey.utils.annotation.CheckExactlyOneNotNull

/**
 * Data Transfer object (DTO) for RepeatQuestionnaire. Handles repeat configurations for questionnaires.
 */
@CheckExactlyOneNotNull(fieldNames = ["unitsFromZero", "randomUnitsFromZeroBetween", "dayOfWeekMap"])
data class RepeatQuestionnaire(
    @field:NotNull
    var unit: String? = null,
    var unitsFromZero: List<Int>? = null,
    var randomUnitsFromZeroBetween: List<Array<Int>>? = null,
    var dayOfWeekMap: Map<String, RepeatQuestionnaire>? = null,
)
