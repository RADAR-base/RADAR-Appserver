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

import kotlinx.serialization.Serializable
import org.radarbase.appserver.jersey.utils.equalTo
import org.radarbase.appserver.jersey.utils.stringRepresentation
import java.util.Objects

@Serializable
open class TimePeriod(var unit: String? = null, var amount: Int? = null) {
    override fun toString(): String = stringRepresentation(
        TimePeriod::amount,
        TimePeriod::unit,
    )

    override fun equals(other: Any?): Boolean = equalTo(
        other,
        TimePeriod::amount,
        TimePeriod::unit,
    )

    override fun hashCode(): Int {
        return Objects.hash(amount, unit)
    }
}
