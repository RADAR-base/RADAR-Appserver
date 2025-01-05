/*
 *
 *  *
 *  *  * Copyright 2018 King's College London
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *  *
 *  *
 *
 */
package org.radarbase.appserver.dto.fcm

import jakarta.validation.constraints.Size
import org.radarbase.appserver.util.equalTo
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.Objects

/**
 * @author yatharthranjan
 */
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
