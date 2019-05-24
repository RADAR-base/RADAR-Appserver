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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.radarbase.appserver.dto.fcm.FcmNotifications;
import org.radarbase.appserver.service.FcmNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@WebMvcTest(FcmNotificationController.class)
public class FcmNotificationControllerTest {

  private final transient Instant scheduledTime = Instant.now().plus(Duration.ofSeconds(100));
  @Autowired private transient MockMvc mockMvc;
  @Autowired private transient ObjectMapper objectMapper;
  @MockBean private transient FcmNotificationService notificationService;

  public static final String TITLE_1 = "Testing 1";
  public static final String FCM_MESSAGE_ID = "123456";
  public static final String PROJECT_ID = "test-project";
  public static final String USER_ID = "test-user";
  public static final String SOURCE_TYPE = "aRMT";
  public static final String NOTIFICATIONS_JSON_PATH = "$.notifications";
  public static final String NOTIFICATION_TITLE_JSON_PATH = "$.notifications[0].title";
  public static final String NOTIFICATION_FCMID_JSON_PATH = "$.notifications[0].fcmMessageId";

  @BeforeEach
  public void setUp() {
    FcmNotificationDto notificationDto =
        new FcmNotificationDto()
            .setBody("Test notif")
            .setTitle(TITLE_1)
            .setScheduledTime(scheduledTime)
            .setSourceId("test")
            .setFcmMessageId(FCM_MESSAGE_ID)
            .setTtlSeconds(86400)
            .setDelivered(false)
            .setId(1L);

    given(notificationService.getAllNotifications())
        .willReturn(new FcmNotifications().setNotifications(List.of(notificationDto)));

    given(notificationService.getNotificationById(1L)).willReturn(notificationDto);

    given(notificationService.getNotificationsByProjectIdAndSubjectId(PROJECT_ID, USER_ID))
        .willReturn(new FcmNotifications().setNotifications(List.of(notificationDto)));

    given(notificationService.getNotificationsByProjectId(PROJECT_ID))
        .willReturn(new FcmNotifications().setNotifications(List.of(notificationDto)));

    given(notificationService.getNotificationsBySubjectId(USER_ID))
        .willReturn(new FcmNotifications().setNotifications(List.of(notificationDto)));

    FcmNotificationDto notificationDto2 =
        new FcmNotificationDto()
            .setBody("Test notif")
            .setTitle("Testing 2")
            .setScheduledTime(scheduledTime)
            .setSourceId("test")
            .setFcmMessageId(FCM_MESSAGE_ID + "7")
            .setSourceType(SOURCE_TYPE)
            .setType("ESM")
            .setAppPackage(SOURCE_TYPE)
            .setTtlSeconds(86400)
            .setDelivered(false)
            .setId(2L);

    given(notificationService.addNotification(notificationDto2, USER_ID, PROJECT_ID))
        .willReturn(notificationDto2);

    given(notificationService.getNotificationById(2L)).willReturn(notificationDto2);

    doAnswer(
            (InvocationOnMock invocation) -> {
              String projectId = invocation.getArgument(0);
              String subjectId = invocation.getArgument(1);

              assertEquals(PROJECT_ID, projectId);
              assertEquals(USER_ID, subjectId);

              return null;
            })
        .when(notificationService)
        .removeNotificationsForUser(any(String.class), any(String.class));
  }

  @Test
  void getAllNotifications() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(URI.create("/" + Paths.NOTIFICATION_PATH)))
        .andExpect(status().isOk())
        .andExpect(jsonPath(NOTIFICATIONS_JSON_PATH, hasSize(1)))
        .andExpect(jsonPath(NOTIFICATION_TITLE_JSON_PATH, is(TITLE_1)))
        .andExpect(jsonPath(NOTIFICATION_FCMID_JSON_PATH, is(FCM_MESSAGE_ID)));
  }

  @Test
  void getNotificationUsingId() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(URI.create("/" + Paths.NOTIFICATION_PATH + "/1")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title", is(TITLE_1)))
        .andExpect(jsonPath("$.fcmMessageId", is(FCM_MESSAGE_ID)));
  }

  @Test
  @Disabled("Not implemented yet")
  void getFilteredNotifications() {}

  @Test
  void getNotificationsUsingProjectIdAndSubjectId() throws Exception {

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(
                    URI.create(
                        "/"
                            + Paths.PROJECT_PATH
                            + "/"
                            + PROJECT_ID
                            + "/"
                            + Paths.USER_PATH
                            + "/"
                            + USER_ID
                            + "/"
                            + Paths.NOTIFICATION_PATH)))
        .andExpect(status().isOk())
        .andExpect(jsonPath(NOTIFICATIONS_JSON_PATH, hasSize(1)))
        .andExpect(jsonPath(NOTIFICATION_TITLE_JSON_PATH, is(TITLE_1)))
        .andExpect(jsonPath(NOTIFICATION_FCMID_JSON_PATH, is(FCM_MESSAGE_ID)));
  }

  @Test
  void getNotificationsUsingProjectId() throws Exception {

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(
                    URI.create(
                        "/"
                            + Paths.PROJECT_PATH
                            + "/"
                            + PROJECT_ID
                            + "/"
                            + Paths.NOTIFICATION_PATH)))
        .andExpect(status().isOk())
        .andExpect(jsonPath(NOTIFICATIONS_JSON_PATH, hasSize(1)))
        .andExpect(jsonPath(NOTIFICATION_TITLE_JSON_PATH, is(TITLE_1)))
        .andExpect(jsonPath(NOTIFICATION_FCMID_JSON_PATH, is(FCM_MESSAGE_ID)));
  }

  @Test
  void getNotificationsUsingSubjectId() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(
                    URI.create(
                        "/" + Paths.USER_PATH + "/" + USER_ID + "/" + Paths.NOTIFICATION_PATH)))
        .andExpect(status().isOk())
        .andExpect(jsonPath(NOTIFICATIONS_JSON_PATH, hasSize(1)))
        .andExpect(jsonPath(NOTIFICATION_TITLE_JSON_PATH, is(TITLE_1)))
        .andExpect(jsonPath(NOTIFICATION_FCMID_JSON_PATH, is(FCM_MESSAGE_ID)));
  }

  @Test
  void addSingleNotification() throws Exception {

    FcmNotificationDto notificationDto2 =
        new FcmNotificationDto()
            .setBody("Test notif")
            .setTitle("Testing 2")
            .setScheduledTime(scheduledTime)
            .setSourceId("test")
            .setFcmMessageId(FCM_MESSAGE_ID + "7")
            .setSourceType(SOURCE_TYPE)
            .setType("ESM")
            .setAppPackage(SOURCE_TYPE)
            .setTtlSeconds(86400)
            .setDelivered(false);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(
                    URI.create(
                        "/"
                            + Paths.PROJECT_PATH
                            + "/"
                            + PROJECT_ID
                            + "/"
                            + Paths.USER_PATH
                            + "/"
                            + USER_ID
                            + "/"
                            + Paths.NOTIFICATION_PATH))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(notificationDto2)))
        .andExpect(jsonPath("$.title", is("Testing 2")))
        .andExpect(jsonPath("$.fcmMessageId", is(FCM_MESSAGE_ID + "7")))
        .andExpect(jsonPath("$.id", is(2)));
  }

  @Test
  void deleteNotificationsForUser() throws Exception {
    mockMvc.perform(
        MockMvcRequestBuilders.delete(
                URI.create(
                    "/"
                        + Paths.PROJECT_PATH
                        + "/"
                        + PROJECT_ID
                        + "/"
                        + Paths.USER_PATH
                        + "/"
                        + USER_ID
                        + "/"
                        + Paths.NOTIFICATION_PATH)));
  }
}
