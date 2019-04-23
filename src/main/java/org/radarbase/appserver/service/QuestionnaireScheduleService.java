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

package org.radarbase.appserver.service;

import org.radarbase.appserver.entity.Notification;
import org.radarbase.appserver.entity.User;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class QuestionnaireScheduleService {


    // TODO get github protocol and generate schedule using enrolmentDate
    // Use cached map of protocols per project
    // Use cached map of schedule of user

    public void getProtocolForProject(String projectId) {

    }

    public Set<Notification> getScheduleForUser(User user) {
        return null;
    }

    public void generateScheduleForUser(User user) {

    }
}
