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
package org.radarbase.appserver.event.state.dto

import org.radarbase.appserver.entity.DataMessage
import org.radarbase.appserver.event.state.MessageState
import java.io.Serial
import java.time.Instant

/**
 * Create a new ApplicationEvent.
 *
 * @param source         the object on which the event initially occurred (never `null`)
 * @param dataMessage    the data message associated with this state event.
 * @param state          the current [MessageState] change of the [                       ] entity.
 * @param additionalInfo any additional info associated with the state change.
 */

class DataMessageStateEventDto(
    source: Any,
    val dataMessage: DataMessage,
    state: MessageState,
    additionalInfo: Map<String, String>?,
    time: Instant
) : MessageStateEventDto(source, state, additionalInfo, time) {

    override fun toString(): String {
        return "DataMessageStateEventDto(dataMessage=$dataMessage) ${super.toString()}"
    }

    companion object {
        @Serial
        private const val serialVersionUID = 327842183571939L
    }
}
