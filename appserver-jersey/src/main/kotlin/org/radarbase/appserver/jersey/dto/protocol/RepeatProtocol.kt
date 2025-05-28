package org.radarbase.appserver.jersey.dto.protocol

import jakarta.validation.constraints.NotNull
import org.radarbase.appserver.jersey.utils.annotation.CheckExactlyOneNotNull

@CheckExactlyOneNotNull(fieldNames = ["amount", "randomAmountBetween"])
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
