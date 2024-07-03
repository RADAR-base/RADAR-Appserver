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

import io.minio.BucketExistsArgs;
import io.minio.MinioClient;
import org.radarbase.appserver.config.S3StorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@ConditionalOnExpression("${file-upload.enabled:false} and 's3' == '${storage.type:}'")
public class MinioClientInitializer {

    private transient MinioClient minioClient;
    private transient String bucketName;

    @Autowired
    private transient S3StorageProperties s3StorageProperties;

    @PostConstruct
    public void init() {
        try {
            minioClient =
                MinioClient.builder()
                    .endpoint(s3StorageProperties.getUrl())
                    .credentials(s3StorageProperties.getAccessKey(), s3StorageProperties.getSecretKey())
                    .build();
            bucketName = s3StorageProperties.getBucketName();
            boolean found =
                minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                throw new RuntimeException(String.format("S3 bucket '%s' does not exist", bucketName));
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not connect to S3", e);
        }
    }

    public MinioClient getClient() {
        return minioClient;
    }

    public String getBucketName() {
        return bucketName;
    }

}
