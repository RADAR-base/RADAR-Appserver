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

import org.radarbase.appserver.event.state.MessageState
import org.radarbase.appserver.util.OpenClass
import org.radarbase.appserver.util.stringRepresentation
import org.springframework.context.ApplicationEvent
import java.io.Serial
import java.time.Instant

@OpenClass
class MessageStateEventDto(
    source: Any,
    var state: MessageState,
    var additionalInfo: Map<String, String>?,
    var time: Instant,
) : ApplicationEvent(source) {

    companion object {
        @Serial
        private const val serialVersionUID = 327842183571937L
    }

    override fun toString(): String = stringRepresentation(
        MessageStateEventDto::state,
        MessageStateEventDto::additionalInfo,
        MessageStateEventDto::time,
    )
}
