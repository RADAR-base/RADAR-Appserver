package org.radarbase.appserver.service;

import org.radarbase.appserver.dto.RadarUserDto;
import org.radarbase.appserver.dto.RadarUsers;

public class RadarUserService {

    public RadarUsers getAllRadarUsers() {
        return null;
    }

    public RadarUserDto storeRadarUser(String projectId, String subjectId, String sourceId) {
        // TODO: Future -- If any value is null get them using the MP api using others.
        // TODO: Store in DB first
        return null;
    }

    public RadarUsers getUserByProjectId(String projectId) {
        return null;
    }
}
