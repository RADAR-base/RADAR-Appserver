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

package org.radarbase.appserver.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.radarbase.appserver.service.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UploadController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "radar.file-upload.enabled=true",
    "security.radar.managementportal.enabled=false"
})
class UploadControllerTest {

  @Autowired private transient MockMvc mockMvc;

  @MockBean private transient StorageService storageService;

  private static final String PROJECT_ID = "my-project";
  private static final String SUBJECT_ID = "my-subject";
  private static final String TOPIC_ID = "my-topic";
  private static final byte[] file = "my-file-content".getBytes();
  private static final String FILE_PATH = "my-file-path/UUID.txt";

  @BeforeEach
  void setUp() {
    given(storageService.store(any(), eq(PROJECT_ID), eq(SUBJECT_ID), eq(TOPIC_ID)))
        .willReturn(FILE_PATH);
  }

  private transient MockMultipartFile multipartFile = new MockMultipartFile(
      "file", "my-file.txt", "text/plain", file
  );

  @Test
  void testUploadFile() throws Exception {
    String uri = String.format(
        "/projects/%s/users/%s/files/topics/%s/upload", PROJECT_ID, SUBJECT_ID, TOPIC_ID);
    mockMvc
        .perform(
            MockMvcRequestBuilders
                .multipart(uri)
                .file(multipartFile))
        .andExpect(status().isCreated())
        .andExpect(header().exists("Location"))
        .andExpect(header().string("Location", is(FILE_PATH)));
  }

}
