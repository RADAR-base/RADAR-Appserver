package org.radarbase.appserver.jersey.dto.protocol

import org.radarbase.appserver.jersey.utils.equalTo
import org.radarbase.appserver.jersey.utils.stringRepresentation
import java.util.Objects

class ReminderTimePeriod(
    val repeat: Int? = null,
) : TimePeriod() {
    override fun toString(): String = stringRepresentation(
        ReminderTimePeriod::amount,
        ReminderTimePeriod::unit,
        ReminderTimePeriod::repeat,
    )

    override fun equals(other: Any?): Boolean = equalTo(
        other,
        ReminderTimePeriod::repeat,
    )

    override fun hashCode(): Int {
        return Objects.hash(repeat)
    }
}
