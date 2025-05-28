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
