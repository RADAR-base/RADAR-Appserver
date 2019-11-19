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

package org.radarbase.appserver.event.state;

import lombok.Getter;
import lombok.ToString;
import org.radarbase.appserver.entity.Notification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.Map;

@Getter
@ToString
public class NotificationStateEvent extends MessageStateEvent {
    private static final long serialVersionUID = 327842183571938L;

    private Notification notification;

    /**
     * Create a new ApplicationEvent.
     *
     * @param source         the object on which the event initially occurred (never {@code null})
     * @param notification   the notification associated with this state event.
     * @param state          the current {@link MessageState} change of the {@link
     *                       Notification} entity.
     * @param additionalInfo any additional info associated with the state change.
     */
    public NotificationStateEvent(
            Object source,
            @NonNull Notification notification,
            @NonNull MessageState state,
            @Nullable Map<String, String> additionalInfo,
            @NonNull Instant time) {
        super(source, state, additionalInfo, time);
        this.notification = notification;
    }
}
