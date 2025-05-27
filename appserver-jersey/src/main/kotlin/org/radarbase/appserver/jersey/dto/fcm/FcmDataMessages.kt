package org.radarbase.appserver.jersey.dto.fcm

import jakarta.validation.constraints.Size
import org.radarbase.appserver.jersey.utils.equalTo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Objects

class FcmDataMessages {

    @field:Size(max = 200)
    private var _dataMessages: MutableList<FcmDataMessageDto> = mutableListOf()

    val dataMessages: List<FcmDataMessageDto>
        get() = _dataMessages

    fun withDataMessages(dataMessages: List<FcmDataMessageDto>): FcmDataMessages {
        this._dataMessages = dataMessages.toMutableList()
        return this
    }

    fun addDataMessage(dataMessageDto: FcmDataMessageDto): FcmDataMessages {
        if (!_dataMessages.contains(dataMessageDto)) {
            this._dataMessages.add(dataMessageDto)
        } else {
            logger.info("Data message {} already exists in the Fcm Data Messages.", dataMessageDto)
        }
        return this
    }

    override fun equals(other: Any?): Boolean = equalTo(
        other,
        FcmDataMessages::_dataMessages,
    )

    override fun hashCode(): Int {
        return Objects.hash(_dataMessages)
    }


    companion object {
        private val logger: Logger = LoggerFactory.getLogger(FcmDataMessages::class.java)
    }
}
