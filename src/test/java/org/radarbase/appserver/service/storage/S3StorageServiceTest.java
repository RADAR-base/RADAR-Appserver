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

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.radarbase.appserver.config.S3StorageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    classes = {S3StorageService.class},
    properties = {
        "file-upload.enabled=true",
        "storage.type=s3",
        "storage.s3.bucket-name=my-bucket",
        "storage.s3.path.prefix=my-sub-path",
    }
)
@EnableConfigurationProperties({S3StorageProperties.class})
class S3StorageServiceTest {

    @Autowired
    private S3StorageService s3StorageService;

    @MockBean
    private MinioClientInitializer minioInit;

    @Mock
    private MinioClient minioClient;

    MockMultipartFile multipartFile = new MockMultipartFile(
        "file", "my-file.txt", "text/plain", "my-file-content".getBytes()
    );

    @BeforeEach
    public void setUp() throws Exception {
        given(minioInit.getBucketName()).willReturn("my-bucket");
        given(minioInit.getClient()).willReturn(minioClient);
        given(minioClient.putObject(any(PutObjectArgs.class))).willReturn(null);
    }

    @Test
    void testInvalidArguments() {
        assertThrows(Exception.class, () -> s3StorageService.store(null, "projectId", "subjectId", "topicId"));
        assertThrows(Exception.class, () -> s3StorageService.store(mock(MultipartFile.class), null, "subjectId", "topicId"));
        assertThrows(Exception.class, () -> s3StorageService.store(mock(MultipartFile.class), "projectId", null, "topicId"));
        assertThrows(Exception.class, () -> s3StorageService.store(mock(MultipartFile.class), "projectId", "subjectId", null));
        assertThrows(Exception.class, () -> s3StorageService.store(mock(MultipartFile.class), "", "subjectId", "topicId"));
        assertThrows(Exception.class, () -> s3StorageService.store(mock(MultipartFile.class), "projectId", "", "topicId"));
        assertThrows(Exception.class, () -> s3StorageService.store(mock(MultipartFile.class), "projectId", "subjectId", ""));
    }

    @Test
    void testStore() throws Exception {
        String path = s3StorageService.store(multipartFile, "projectId", "subjectId", "topicId");
        verify(minioClient).putObject(any());
        assertTrue(path.matches("[0-9]+_[a-z0-9-]+\\.txt"));
    }

}