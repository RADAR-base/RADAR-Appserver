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

package org.radarbase.appserver.dto.questionnaire;

import lombok.Data;
import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.entity.Task;

import java.time.Instant;
import java.util.List;

@Data
public class AssessmentSchedule {
    private String name;

    private Instant referenceTimestamp;

    private List<Instant> referenceTimestamps;

    private List<Task> tasks;

    private List<Notification> notifications;

    private List<Notification> reminders;

    public boolean hasTasks() {
        return tasks != null && !tasks.isEmpty();
    }
}
