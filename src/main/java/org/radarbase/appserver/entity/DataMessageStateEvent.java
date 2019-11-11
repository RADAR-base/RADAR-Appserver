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

package org.radarbase.appserver.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.radarbase.appserver.event.state.MessageState;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.Instant;

@Entity
@Getter
@Table(name = "data_message_state_events")
@NoArgsConstructor
public class DataMessageStateEvent extends MessageStateEvent {
    private static final long serialVersionUID = 876253616328518L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE, optional = false)
    @JoinColumn(name = "data_message_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    private DataMessage dataMessage;

    public DataMessageStateEvent(
            @NotNull DataMessage dataMessage,
            @NotNull MessageState state,
            @NotNull Instant time,
            String associatedInfo) {
        super(state, time, associatedInfo);
        this.dataMessage = dataMessage;
    }
}
