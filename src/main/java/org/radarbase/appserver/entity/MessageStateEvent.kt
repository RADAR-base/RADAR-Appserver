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

import org.radarbase.appserver.event.state.MessageState
import java.io.Serializable
import java.time.Instant
import javax.persistence.*
import javax.validation.constraints.NotNull

@MappedSuperclass
abstract class MessageStateEvent : Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null

    @field:Column(nullable = false)
    @field:Enumerated(EnumType.STRING)
    lateinit var state: MessageState

    @field:Column(nullable = false)
    lateinit var time: @NotNull Instant

    @field:Column(name = "associated_info", length = 1250)
    var associatedInfo: String? = null

    companion object {
        private const val serialVersionUID = 876253616328519L
    }
}