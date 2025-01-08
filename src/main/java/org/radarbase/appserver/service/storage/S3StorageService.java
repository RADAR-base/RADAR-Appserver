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
import org.radarbase.appserver.exception.FileStorageException;
import org.radarbase.appserver.exception.InvalidFileDetailsException;
import org.radarbase.appserver.exception.InvalidPathDetailsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;


@Slf4j
@Service
@ConditionalOnExpression("${radar.file-upload.enabled:false} and 's3' == '${radar.storage.type:}'")
public class S3StorageService implements StorageService {

    @Autowired
    private transient S3StorageProperties s3StorageProperties;

    @Autowired
    private transient MinioClientInitializer bucketClient;

    public String store(MultipartFile file, String projectId, String subjectId, String topicId) {
        if (
            file == null || projectId == null || subjectId == null || topicId == null
            || file.isEmpty()|| projectId.isEmpty() || subjectId.isEmpty() || topicId.isEmpty()) {
            throw new InvalidFileDetailsException("File, project, subject and topic IDs must not be empty");
        }

        try {
            StoragePath filePath = StoragePath.builder()
                .prefix(s3StorageProperties.getPath().getPrefix())
                .projectId(projectId)
                .subjectId(subjectId)
                .topicId(topicId)
                .collectPerDay(s3StorageProperties.getPath().getCollectPerDay())
                .filename(file.getOriginalFilename())
                .build();

            log.debug("Attempt storing file at path: {}", filePath.getFullPath());

            bucketClient.getClient().putObject(PutObjectArgs
                .builder()
                .bucket(bucketClient.getBucketName())
                .object(filePath.getFullPath())
                .stream(file.getInputStream(), file.getSize(), -1)
                .build());

            return filePath.getFullPath();
        } catch (IllegalArgumentException e) {
            throw new InvalidPathDetailsException("There is a problem resolving the path on the object storage", e);
        } catch (Exception e) {
            throw new FileStorageException("There is a problem storing the file on the object storage", e);
        }
    }

}
