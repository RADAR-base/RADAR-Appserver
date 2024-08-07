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

package org.radarbase.appserver.dto.protocol;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

/**
 * @author yatharthranjan
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AssessmentProtocol {

    private RepeatProtocol repeatProtocol;

    private ReminderTimePeriod reminders;

    private TimePeriod completionWindow;

    private RepeatQuestionnaire repeatQuestionnaire;

    private ReferenceTimestamp referenceTimestamp;

    private ClinicalProtocol clinicalProtocol;

    private NotificationProtocol notification = new NotificationProtocol();

    @JsonDeserialize(using = ReferenceTimestampDeserializer.class)
    public void setReferenceTimestamp(Object responseObject) {
        if(responseObject instanceof ReferenceTimestamp)
            this.referenceTimestamp = (ReferenceTimestamp) responseObject;
    }
}
