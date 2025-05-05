/*
 * Copyright 2024 The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.appserver.service.storage

import io.minio.PutObjectArgs
import org.radarbase.appserver.config.S3StorageProperties
import org.radarbase.appserver.exception.FileStorageException
import org.radarbase.appserver.exception.InvalidFileDetailsException
import org.radarbase.appserver.exception.InvalidPathDetailsException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
@ConditionalOnExpression("\${radar.file-upload.enabled:false} && 's3' == '\${radar.storage.type:}'")
class S3StorageService @Autowired constructor(
    private val s3StorageProperties: S3StorageProperties,
    private val bucketClient: MinioClientInitializer
) : StorageService {

    override fun store(file: MultipartFile?, projectId: String?, subjectId: String?, topicId: String?): String {
        if (file == null || projectId.isNullOrBlank() || subjectId.isNullOrBlank() || topicId.isNullOrBlank() || file.isEmpty) {
            throw InvalidFileDetailsException("File, project, subject and topic IDs must not be empty")
        }

        try {
            val filePath = StoragePath.builder()
                .prefix(s3StorageProperties.path.prefix!!)
                .projectId(projectId)
                .subjectId(subjectId)
                .topicId(topicId)
                .collectPerDay(s3StorageProperties.path.collectPerDay)
                .filename(file.originalFilename!!)
                .build()

            println("Attempt storing file at path: ${filePath.pathInBucket}")

            bucketClient.client.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketClient.bucketNameOrThrow)
                    .`object`(filePath.pathInBucket)
                    .stream(file.inputStream, file.size, -1)
                    .build()
            )

            return filePath.pathInBucket
        } catch (e: IllegalArgumentException) {
            throw InvalidPathDetailsException("There is a problem resolving the path on the object storage", e)
        } catch (e: Exception) {
            throw FileStorageException("There is a problem storing the file on the object storage", e)
        }
    }
}
