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
package org.radarbase.appserver.entity

import jakarta.persistence.Column
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.MappedSuperclass
import jakarta.validation.constraints.NotNull
import org.radarbase.appserver.event.state.MessageState
import java.io.Serial
import java.io.Serializable
import java.time.Instant

@MappedSuperclass
class MessageStateEvent(

    @field:NotNull
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    var state: MessageState? = null,

    @field:NotNull
    @Column(nullable = false)
    var time: Instant? = null,

    @Column(name = "associated_info", length = 1250)
    var associatedInfo: String? = null,

) : Serializable {

    companion object {
        @Serial
        private const val serialVersionUID = 876253616328519L
    }
}
