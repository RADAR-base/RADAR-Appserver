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
import static org.junit.jupiter.api.Assertions.*;
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
class FcmNotificationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private FcmNotificationService notificationService;

  private final Instant scheduledTime = Instant.now().plus(Duration.ofSeconds(100));

  @BeforeEach
  public void setUp() {
    FcmNotificationDto notificationDto = new FcmNotificationDto()
        .setBody("Test notif")
        .setTitle("Testing 1")
        .setScheduledTime(scheduledTime)
        .setSourceId("test")
        .setFcmMessageId("123456")
        .setTtlSeconds(86400)
        .setDelivered(false)
        .setId(1L);

    given(notificationService.getAllNotifications())
        .willReturn(
            new FcmNotifications().setNotifications(List.of(notificationDto))
        );

    given(notificationService.getNotificationById(1L))
        .willReturn(notificationDto);

    given(notificationService.getNotificationsByProjectIdAndSubjectId("test-project",
        "test-user"))
        .willReturn(new FcmNotifications().setNotifications(List.of(notificationDto)));


    given(notificationService.getNotificationsByProjectId("test-project"))
        .willReturn(new FcmNotifications().setNotifications(List.of(notificationDto)));

    given(notificationService.getNotificationsBySubjectId("test-user"))
        .willReturn(new FcmNotifications().setNotifications(List.of(notificationDto)));

    FcmNotificationDto notificationDto2 = new FcmNotificationDto()
        .setBody("Test notif")
        .setTitle("Testing 2")
        .setScheduledTime(scheduledTime)
        .setSourceId("test")
        .setFcmMessageId("1234567")
        .setSourceType("aRMT")
        .setType("ESM")
        .setAppPackage("aRMT")
        .setTtlSeconds(86400)
        .setDelivered(false)
        .setId(2L);

    given(notificationService.addNotification(notificationDto2, "test-user", "test-project"))
        .willReturn(notificationDto2);

    given(notificationService.getNotificationById(2L))
        .willReturn(notificationDto2);


    doAnswer( (invocation) -> {

      String projectId = invocation.getArgument(0);
      String subjectId = invocation.getArgument(1);

      assertEquals("test-project", projectId);
      assertEquals("test-user", subjectId);

      return null;

    }).when(notificationService).removeNotificationsForUser(any(String.class), any(String.class));


  }

  @Test
  void getAllNotifications() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get(URI.create("/notifications"))
          .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.notifications", hasSize(1)))
        .andExpect(jsonPath("$.notifications[0].title", is("Testing 1")))
        .andExpect(jsonPath("$.notifications[0].fcmMessageId", is("123456")));

  }

  @Test
  void getNotificationUsingId() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get(URI.create("/notifications/1"))
          .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title", is("Testing 1")))
        .andExpect(jsonPath("$.fcmMessageId", is("123456")));
  }

  @Test
  @Disabled("Not implemented yet")
  void getFilteredNotifications() {}

  @Test
  void getNotificationsUsingProjectIdAndSubjectId() throws Exception {

    mockMvc.perform(MockMvcRequestBuilders.get(URI.create("/projects/test-project/users/test-user/notifications"))
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.notifications", hasSize(1)))
        .andExpect(jsonPath("$.notifications[0].title", is("Testing 1")))
        .andExpect(jsonPath("$.notifications[0].fcmMessageId", is("123456")));
  }

  @Test
  void getNotificationsUsingProjectId() throws Exception {

    mockMvc.perform(MockMvcRequestBuilders.get(URI.create("/projects/test-project/notifications"))
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.notifications", hasSize(1)))
        .andExpect(jsonPath("$.notifications[0].title", is("Testing 1")))
        .andExpect(jsonPath("$.notifications[0].fcmMessageId", is("123456")));
  }

  @Test
  void getNotificationsUsingSubjectId() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.get(URI.create("/users/test-user/notifications"))
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.notifications", hasSize(1)))
      .andExpect(jsonPath("$.notifications[0].title", is("Testing 1")))
      .andExpect(jsonPath("$.notifications[0].fcmMessageId", is("123456")));
  }

  @Test
  void addSingleNotification() throws Exception {

    FcmNotificationDto notificationDto2 = new FcmNotificationDto()
        .setBody("Test notif")
        .setTitle("Testing 2")
        .setScheduledTime(scheduledTime)
        .setSourceId("test")
        .setFcmMessageId("1234567")
        .setSourceType("aRMT")
        .setType("ESM")
        .setAppPackage("aRMT")
        .setTtlSeconds(86400)
        .setDelivered(false);

    mockMvc.perform(MockMvcRequestBuilders.post(URI.create("/projects/test-project/users/test-user/notifications"))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsBytes(notificationDto2)))
        .andExpect(jsonPath("$.title", is("Testing 2")))
        .andExpect(jsonPath("$.fcmMessageId", is("1234567")))
        .andExpect(jsonPath("$.id", is(2)));
  }

  @Test
  void deleteNotificationsForUser() throws Exception {
    mockMvc.perform(MockMvcRequestBuilders.delete(URI.create("/projects/test-project/users/test-user/notifications"))
        .contentType(MediaType.APPLICATION_JSON));
  }
}