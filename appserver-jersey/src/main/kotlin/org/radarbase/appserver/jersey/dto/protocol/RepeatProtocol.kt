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
import kotlinx.serialization.Serializable
import org.radarbase.appserver.jersey.utils.annotation.CheckExactlyOneNotNull

@CheckExactlyOneNotNull(fieldNames = ["amount", "randomAmountBetween"])
@Serializable
data class RepeatProtocol(
    @field:NotNull
    var unit: String? = null,
    var amount: Int? = null,
    var randomAmountBetween: Array<Int>? = null,
    var dayOfWeek: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RepeatProtocol

        if (amount != other.amount) return false
        if (unit != other.unit) return false
        if (!randomAmountBetween.contentEquals(other.randomAmountBetween)) return false
        if (dayOfWeek != other.dayOfWeek) return false

        return true
    }

    override fun hashCode(): Int {
        var result = amount ?: 0
        result = 31 * result + (unit?.hashCode() ?: 0)
        result = 31 * result + (randomAmountBetween?.contentHashCode() ?: 0)
        result = 31 * result + (dayOfWeek?.hashCode() ?: 0)
        return result
    }
}
