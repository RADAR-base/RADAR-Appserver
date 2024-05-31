/*
 *
 *  *  Copyright 2024 The Hyve
 *  *
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *
 */

package org.radarbase.appserver.service.storage;

import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.config.S3StorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;


@Slf4j
@Service
@ConditionalOnExpression("${file-upload.enabled:false} and 's3' == '${storage.type:}'")
public class S3StorageService extends RandomUuidFilenameStorageService implements StorageService {

    @Autowired
    private S3StorageProperties s3StorageProperties;

    @Autowired
    private MinioClientInitializer bucketClient;
    private String subPath = "";

    @PostConstruct
    public void init() {
        if (s3StorageProperties.getSubPath() != null) {
            subPath = s3StorageProperties.getSubPath().replaceAll("^/|/$", "");
            if (!subPath.isEmpty()) {
                subPath = subPath + "/";
            }
        }
    }

    public String store(MultipartFile file, String projectId, String subjectId, String topicId) {
        Assert.notNull(file, "File must not be null");
        Assert.notEmpty(new String[]{projectId, subjectId, topicId}, "Project, subject and topic IDs must not be empty");
        String filePath = String.format("%s%s/%s/%s/%s", subPath, projectId, subjectId, topicId, generateRandomFilename(file.getOriginalFilename()));
        log.debug("Storing file at path: {}", filePath);
        try {
            bucketClient.getClient().putObject(PutObjectArgs
                .builder()
                .bucket(bucketClient.getBucketName())
                .object(filePath)
                .stream(file.getInputStream(), file.getSize(), -1)
                .build());
        } catch (Exception e) {
            throw new RuntimeException("Could not store file", e);
        }
        return filePath;
    }

}
