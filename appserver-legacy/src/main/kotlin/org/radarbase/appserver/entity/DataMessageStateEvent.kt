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

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import jakarta.validation.constraints.NotNull
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import org.radarbase.appserver.event.state.MessageState
import java.io.Serial
import java.time.Instant

@Entity
@Table(name = "data_message_state_events")
class DataMessageStateEvent :MessageStateEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null

    @field:NotNull
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "data_message_id", nullable = false)
    @field:OnDelete(action = OnDeleteAction.CASCADE)
    @field:JsonIgnore
    var dataMessage: DataMessage? = null


    constructor(
        dataMessage: DataMessage?,
        state: MessageState,
        time: Instant,
        associatedInfo: String?
    ): super(state, time, associatedInfo) {
        this.dataMessage = dataMessage
    }

    constructor()

    companion object {
        @Serial
        private const val serialVersionUID = 876253616328520L
    }
}
