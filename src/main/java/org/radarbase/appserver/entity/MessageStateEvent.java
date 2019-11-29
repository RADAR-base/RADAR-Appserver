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

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.radarbase.appserver.event.state.MessageState;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;

@MappedSuperclass
@Getter
@NoArgsConstructor
public abstract class MessageStateEvent implements Serializable {
    private static final long serialVersionUID = 876253616328519L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageState state;

    @NotNull
    @Column(nullable = false)
    private Instant time;

    @Column(name = "associated_info", length = 1250)
    private String associatedInfo;

    public MessageStateEvent(
            @NotNull MessageState state,
            @NotNull Instant time,
            String associatedInfo) {
        this.state = state;
        this.time = time;
        this.associatedInfo = associatedInfo;
    }
}
