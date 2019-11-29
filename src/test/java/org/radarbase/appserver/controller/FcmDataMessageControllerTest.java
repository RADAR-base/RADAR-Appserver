/*
 *
 *  *
 *  *  * Copyright 2018 King's College London
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *  *
 *  *
 *
 */

package org.radarbase.appserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.radarbase.appserver.dto.fcm.FcmDataMessageDto;
import org.radarbase.appserver.dto.fcm.FcmDataMessages;
import org.radarbase.appserver.service.FcmDataMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(FcmDataMessageController.class)
public class FcmDataMessageControllerTest {

    public static final String FCM_MESSAGE_ID = "123456";
    public static final String PROJECT_ID = "test-project";
    public static final String USER_ID = "test-user";
    private static final String SOURCE_TYPE = "aRMT";
    private static final String SOURCE_ID = "test";
    private static final String DATA_MESSAGES_JSON_PATH = "$.dataMessages";
    private static final String DATA_MESSAGE_FCMID_JSON_PATH = "$.dataMessages[0].fcmMessageId";
    private final transient Instant scheduledTime = Instant.now().plus(Duration.ofSeconds(100));
    @Autowired
    private transient MockMvc mockMvc;
    @Autowired
    private transient ObjectMapper objectMapper;
    @MockBean
    private transient FcmDataMessageService dataMessageService;

    @BeforeEach
    public void setUp() {
        FcmDataMessageDto dataMessageDto =
                new FcmDataMessageDto()
                        .setScheduledTime(scheduledTime)
                        .setSourceId(SOURCE_ID)
                        .setFcmMessageId(FCM_MESSAGE_ID)
                        .setTtlSeconds(86400)
                        .setDelivered(false)
                        .setId(1L);

        given(dataMessageService.getAllDataMessages())
                .willReturn(new FcmDataMessages().setDataMessages(List.of(dataMessageDto)));

        given(dataMessageService.getDataMessageById(1L)).willReturn(dataMessageDto);

        given(dataMessageService.getDataMessagesByProjectIdAndSubjectId(PROJECT_ID, USER_ID))
                .willReturn(new FcmDataMessages().setDataMessages(List.of(dataMessageDto)));

        given(dataMessageService.getDataMessagesByProjectId(PROJECT_ID))
                .willReturn(new FcmDataMessages().setDataMessages(List.of(dataMessageDto)));

        given(dataMessageService.getDataMessagesBySubjectId(USER_ID))
                .willReturn(new FcmDataMessages().setDataMessages(List.of(dataMessageDto)));

        FcmDataMessageDto dataMessageDto2 =
                new FcmDataMessageDto()
                        .setScheduledTime(scheduledTime)
                        .setSourceId(SOURCE_ID)
                        .setFcmMessageId(FCM_MESSAGE_ID + "7")
                        .setSourceType(SOURCE_TYPE)
                        .setAppPackage(SOURCE_TYPE)
                        .setTtlSeconds(86400)
                        .setDelivered(false)
                        .setId(2L);

        given(dataMessageService.addDataMessage(dataMessageDto2, USER_ID, PROJECT_ID))
                .willReturn(dataMessageDto2);

        FcmDataMessageDto dataMessageDto3 =
                new FcmDataMessageDto()
                        .setScheduledTime(scheduledTime)
                        .setSourceId(SOURCE_ID)
                        .setFcmMessageId(FCM_MESSAGE_ID + "7")
                        .setSourceType(SOURCE_TYPE)
                        .setAppPackage(SOURCE_TYPE)
                        .setTtlSeconds(86400)
                        .setDelivered(false);

        FcmDataMessages fcmDataMessages =
                new FcmDataMessages().setDataMessages(List.of(dataMessageDto2, dataMessageDto3));

        given(dataMessageService.addDataMessages(fcmDataMessages, USER_ID, PROJECT_ID))
                .willReturn(fcmDataMessages);

        given(dataMessageService.getDataMessageById(2L)).willReturn(dataMessageDto2);

        doAnswer(
                (InvocationOnMock invocation) -> {
                    String projectId = invocation.getArgument(0);
                    String subjectId = invocation.getArgument(1);

                    assertEquals(PROJECT_ID, projectId);
                    assertEquals(USER_ID, subjectId);

                    return null;
                })
                .when(dataMessageService)
                .removeDataMessagesForUser(any(String.class), any(String.class));
    }

    @Test
    void getAllDataMessages() throws Exception {
        mockMvc
                .perform(
                        MockMvcRequestBuilders.get(URI.create("/" + PathsUtil.MESSAGING_DATA_PATH)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(DATA_MESSAGES_JSON_PATH, hasSize(1)))
                .andExpect(jsonPath(DATA_MESSAGE_FCMID_JSON_PATH, is(FCM_MESSAGE_ID)));
    }

    @Test
    void getDataMessageUsingId() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders
                        .get(URI.create("/" + PathsUtil.MESSAGING_DATA_PATH + "/1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fcmMessageId", is(FCM_MESSAGE_ID)));
    }

    @Test
    @Disabled("Not implemented yet")
    void getFilteredDataMessages() {
        // TODO
    }

    @Test
    void getDataMessagesUsingProjectIdAndSubjectId() throws Exception {

        mockMvc
                .perform(
                        MockMvcRequestBuilders.get(
                                URI.create(
                                        "/"
                                                + PathsUtil.PROJECT_PATH
                                                + "/"
                                                + PROJECT_ID
                                                + "/"
                                                + PathsUtil.USER_PATH
                                                + "/"
                                                + USER_ID
                                                + "/"
                                                + PathsUtil.MESSAGING_DATA_PATH)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(DATA_MESSAGES_JSON_PATH, hasSize(1)))
                .andExpect(jsonPath(DATA_MESSAGE_FCMID_JSON_PATH, is(FCM_MESSAGE_ID)));
    }

    @Test
    void getDataMessagesUsingProjectId() throws Exception {

        mockMvc
                .perform(
                        MockMvcRequestBuilders.get(
                                URI.create(
                                        "/" + PathsUtil.PROJECT_PATH + "/" + PROJECT_ID + "/"
                                                + PathsUtil.MESSAGING_DATA_PATH)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(DATA_MESSAGES_JSON_PATH, hasSize(1)))
                .andExpect(jsonPath(DATA_MESSAGE_FCMID_JSON_PATH, is(FCM_MESSAGE_ID)));
    }

    @Test
    void getDataMessagesUsingSubjectId() throws Exception {
        mockMvc
                .perform(
                        MockMvcRequestBuilders.get(
                                URI.create("/" + PathsUtil.USER_PATH + "/" + USER_ID + "/"
                                        + PathsUtil.MESSAGING_DATA_PATH)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(DATA_MESSAGES_JSON_PATH, hasSize(1)))
                .andExpect(jsonPath(DATA_MESSAGE_FCMID_JSON_PATH, is(FCM_MESSAGE_ID)));
    }

    @Test
    void addSingleDataMessage() throws Exception {

        FcmDataMessageDto dataMessageDto2 =
                new FcmDataMessageDto()
                        .setScheduledTime(scheduledTime)
                        .setSourceId(SOURCE_ID)
                        .setFcmMessageId(FCM_MESSAGE_ID + "7")
                        .setSourceType(SOURCE_TYPE)
                        .setAppPackage(SOURCE_TYPE)
                        .setTtlSeconds(86400)
                        .setDelivered(false);

        mockMvc
                .perform(
                        MockMvcRequestBuilders.post(
                                URI.create(
                                        "/"
                                                + PathsUtil.PROJECT_PATH
                                                + "/"
                                                + PROJECT_ID
                                                + "/"
                                                + PathsUtil.USER_PATH
                                                + "/"
                                                + USER_ID
                                                + "/"
                                                + PathsUtil.MESSAGING_DATA_PATH))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(dataMessageDto2)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fcmMessageId", is(FCM_MESSAGE_ID + "7")))
                .andExpect(jsonPath("$.id", is(2)));
    }

    @Test
    void addBatchDataMessages() throws Exception {

        FcmDataMessageDto dataMessageDto2 =
                new FcmDataMessageDto()
                        .setScheduledTime(scheduledTime)
                        .setSourceId(SOURCE_ID)
                        .setFcmMessageId(FCM_MESSAGE_ID + "7")
                        .setSourceType(SOURCE_TYPE)
                        .setAppPackage(SOURCE_TYPE)
                        .setTtlSeconds(86400)
                        .setDelivered(false);

        FcmDataMessageDto dataMessageDto3 =
                new FcmDataMessageDto()
                        .setScheduledTime(scheduledTime)
                        .setSourceId(SOURCE_ID)
                        .setFcmMessageId(FCM_MESSAGE_ID + "7")
                        .setSourceType(SOURCE_TYPE)
                        .setAppPackage(SOURCE_TYPE)
                        .setTtlSeconds(86400)
                        .setDelivered(false);

        FcmDataMessages fcmDataMessages =
                new FcmDataMessages().setDataMessages(List.of(dataMessageDto2, dataMessageDto3));

        mockMvc
                .perform(
                        MockMvcRequestBuilders.post(
                                URI.create(
                                        "/"
                                                + PathsUtil.PROJECT_PATH
                                                + "/"
                                                + PROJECT_ID
                                                + "/"
                                                + PathsUtil.USER_PATH
                                                + "/"
                                                + USER_ID
                                                + "/"
                                                + PathsUtil.MESSAGING_DATA_PATH
                                                + "/batch"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsBytes(fcmDataMessages)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(DATA_MESSAGES_JSON_PATH, hasSize(2)))
                .andExpect(jsonPath(DATA_MESSAGE_FCMID_JSON_PATH, is(FCM_MESSAGE_ID + "7")));
    }

    @Test
    void deleteDataMessagesForUser() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.delete(
                        URI.create(
                                "/"
                                        + PathsUtil.PROJECT_PATH
                                        + "/"
                                        + PROJECT_ID
                                        + "/"
                                        + PathsUtil.USER_PATH
                                        + "/"
                                        + USER_ID
                                        + "/"
                                        + PathsUtil.MESSAGING_DATA_PATH)));
    }
}
