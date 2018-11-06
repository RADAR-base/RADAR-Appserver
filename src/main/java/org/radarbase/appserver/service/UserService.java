package org.radarbase.appserver.service;

import org.radarbase.fcm.dto.FcmUserDto;
import org.radarbase.fcm.dto.FcmUsers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
