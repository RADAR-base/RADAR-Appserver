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

package org.radarbase.appserver.event.state.dto;

import java.time.Instant;
import java.util.Map;
import lombok.Getter;
import lombok.ToString;
import org.radarbase.appserver.event.state.MessageState;
import org.springframework.context.ApplicationEvent;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
@ToString
public class MessageStateEventDto extends ApplicationEvent {
    private static final long serialVersionUID = 327842183571937L;

    private MessageState state;
    private Map<String, String> additionalInfo;
    private Instant time;

    public MessageStateEventDto(
            Object source,
            @NonNull MessageState state,
            @Nullable Map<String, String> additionalInfo,
            @NonNull Instant time) {
        super(source);
        this.state = state;
        this.additionalInfo = additionalInfo;
        this.time = time;
    }
}
