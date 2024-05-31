package org.radarbase.appserver.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    String store(MultipartFile file, String projectId, String subjectId, String topicId);
}
