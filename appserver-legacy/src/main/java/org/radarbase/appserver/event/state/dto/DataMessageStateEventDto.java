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
import org.radarbase.appserver.entity.DataMessage;
import org.radarbase.appserver.event.state.MessageState;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
@ToString
public class DataMessageStateEventDto extends MessageStateEventDto {
    private static final long serialVersionUID = 327842183571939L;

    private DataMessage dataMessage;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source         the object on which the event initially occurred (never {@code null})
     * @param dataMessage    the data message associated with this state event.
     * @param state          the current {@link MessageState} change of the {@link
     *                       DataMessage} entity.
     * @param additionalInfo any additional info associated with the state change.
     */
    public DataMessageStateEventDto(
            Object source,
            @NonNull DataMessage dataMessage,
            @NonNull MessageState state,
            @Nullable Map<String, String> additionalInfo,
            @NonNull Instant time) {
        super(source, state, additionalInfo, time);
        this.dataMessage = dataMessage;
    }
}
