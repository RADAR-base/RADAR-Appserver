package org.radarbase.appserver.jersey.dto.protocol

import org.radarbase.appserver.jersey.utils.equalTo
import org.radarbase.appserver.jersey.utils.stringRepresentation
import java.util.Objects

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
