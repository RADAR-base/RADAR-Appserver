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

package org.radarbase.appserver.service.storage

import io.minio.MinioClient
import io.minio.PutObjectArgs
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.radarbase.appserver.config.S3StorageProperties
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.multipart.MultipartFile

@ExtendWith(SpringExtension::class)
@SpringBootTest(
    classes = [S3StorageService::class],
    properties = [
        "radar.file-upload.enabled=true",
        "radar.storage.type=s3",
        "radar.storage.s3.bucket-name=my-bucket",
        "radar.storage.s3.path.prefix=my-sub-path",
    ],
)
@EnableConfigurationProperties(S3StorageProperties::class)
class S3StorageServiceTest {

    @Autowired
    private lateinit var s3StorageService: S3StorageService

    @MockBean
    private lateinit var minioInit: MinioClientInitializer

    @Mock
    private lateinit var minioClient: MinioClient

    private val multipartFile = MockMultipartFile(
        "file",
        "my-file.txt",
        "text/plain",
        "my-file-content".toByteArray(),
    )

    companion object {
        private const val PROJECT_ID = "projectId"
        private const val SUBJECT_ID = "subjectId"
        private const val TOPIC_ID = "topicId"
    }

    @BeforeEach
    fun setUp() {
        given(minioInit.bucketNameOrThrow).willReturn("my-bucket")
        given(minioInit.client).willReturn(minioClient)
        given(minioClient.putObject(any<PutObjectArgs>())).willReturn(null)
    }

    @Test
    fun testInvalidArguments() {
        assertThrows<Exception> { s3StorageService.store(null, PROJECT_ID, SUBJECT_ID, TOPIC_ID) }
        assertThrows<Exception> { s3StorageService.store(mock(MultipartFile::class.java), null, SUBJECT_ID, TOPIC_ID) }
        assertThrows<Exception> { s3StorageService.store(mock(MultipartFile::class.java), PROJECT_ID, null, TOPIC_ID) }
        assertThrows<Exception> { s3StorageService.store(mock(MultipartFile::class.java), PROJECT_ID, SUBJECT_ID, null) }
        assertThrows<Exception> { s3StorageService.store(mock(MultipartFile::class.java), "", SUBJECT_ID, TOPIC_ID) }
        assertThrows<Exception> { s3StorageService.store(mock(MultipartFile::class.java), PROJECT_ID, "", TOPIC_ID) }
        assertThrows<Exception> { s3StorageService.store(mock(MultipartFile::class.java), PROJECT_ID, SUBJECT_ID, "") }
    }

    @Test
    fun testStore() {
        val path = s3StorageService.store(multipartFile, PROJECT_ID, SUBJECT_ID, TOPIC_ID)
        verify(minioClient).putObject(any<PutObjectArgs>())
        assertTrue(path.matches("my-sub-path/$PROJECT_ID/$SUBJECT_ID/$TOPIC_ID/[0-9]+_[a-z0-9-]+\\.txt".toRegex()))
    }
}
