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

import org.radarbase.fcm.dto.FcmUserDto;
import org.radarbase.fcm.dto.FcmUsers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author yatharthranjan
 */
@Service
@Transactional
public class UserService {

    @Transactional(readOnly = true)
    public FcmUsers getAllRadarUsers() {
        return null;
    }

    @Transactional
    public FcmUserDto storeRadarUser(String projectId, String subjectId, String sourceId) {
        // TODO: Future -- If any value is null get them using the MP api using others.
        // TODO: Store in DB first
        return null;
    }

    @Transactional(readOnly = true)
    public FcmUsers getUserByProjectId(String projectId) {
        return null;
    }
}
