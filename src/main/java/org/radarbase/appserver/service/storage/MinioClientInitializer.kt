/*
 * Copyright 2024 The Hyve
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.appserver.service.storage

import io.minio.BucketExistsArgs
import io.minio.MinioClient
import org.radarbase.appserver.config.S3StorageProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Component

@Component
@ConditionalOnExpression("\${radar.file-upload.enabled:false} and 's3' == '\${radar.storage.type:}'")
class MinioClientInitializer(
    private val s3StorageProperties: S3StorageProperties
) {
    private var minioClient: MinioClient? = null
    private var bucketName: String? = null

    val client: MinioClient
        get() {
            if (minioClient == null) {
                initClient()
            }
            return minioClient!!
        }

    val bucketNameOrThrow: String
        get() = bucketName ?: throw IllegalStateException("Bucket name is not initialized")

    private fun initClient() {
        try {
            minioClient = MinioClient.builder()
                .endpoint(s3StorageProperties.url)
                .credentials(s3StorageProperties.accessKey, s3StorageProperties.secretKey)
                .build()

            bucketName = s3StorageProperties.bucketName

            val bucketExists = minioClient!!.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName!!).build()
            )

            if (!bucketExists) {
                throw RuntimeException("S3 bucket '$bucketName' does not exist")
            }
        } catch (e: Exception) {
            throw RuntimeException("Could not connect to S3", e)
        }
    }
}
