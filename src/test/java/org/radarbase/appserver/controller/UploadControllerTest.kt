/*
 *  Copyright 2024 The Hyve
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.radarbase.appserver.controller

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito.given
import org.radarbase.appserver.service.storage.StorageService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.header
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(UploadController::class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = [
    "radar.file-upload.enabled=true",
    "security.radar.managementportal.enabled=false"
])
class UploadControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var storageService: StorageService

    private companion object {
        const val PROJECT_ID = "my-project"
        const val SUBJECT_ID = "my-subject"
        const val TOPIC_ID = "my-topic"
        val file = "my-file-content".toByteArray()
        const val FILE_PATH = "my-file-path/UUID.txt"
    }

    private val multipartFile = MockMultipartFile(
        "file", "my-file.txt", "text/plain", file
    )

    @BeforeEach
    fun setUp() {
        given(storageService.store(any(), eq(PROJECT_ID), eq(SUBJECT_ID), eq(TOPIC_ID)))
            .willReturn(FILE_PATH)
    }

    @Test
    fun `test upload file`() {
        val uri = "/projects/$PROJECT_ID/users/$SUBJECT_ID/files/topics/$TOPIC_ID/upload"

        mockMvc.perform(
            multipart(uri).file(multipartFile)
        )
            .andExpect(status().isCreated)
            .andExpect(header().exists("Location"))
            .andExpect(header().string("Location", FILE_PATH))
    }
}
