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

package org.radarbase.appserver.jersey.event.state.dto

import org.radarbase.appserver.jersey.entity.DataMessage
import org.radarbase.appserver.jersey.event.state.MessageState
import java.time.Instant

/**
 * Create a new ApplicationEvent.
 *
 * @param dataMessage    the data message associated with this state event.
 * @param state          the current [MessageState]..
 * @param additionalInfo any additional info associated with the state change.
 */

class DataMessageStateEventDto(
    val dataMessage: DataMessage,
    state: MessageState,
    additionalInfo: Map<String, String>?,
    time: Instant,
) : MessageStateEventDto(state, additionalInfo, time) {

    override fun toString(): String {
        return "DataMessageStateEventDto(dataMessage=$dataMessage) ${super.toString()}"
    }
}
