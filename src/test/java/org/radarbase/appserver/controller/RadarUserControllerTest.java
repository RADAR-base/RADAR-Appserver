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

import static org.hamcrest.Matchers.is;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.radarbase.appserver.dto.fcm.FcmUserDto;
import org.radarbase.appserver.dto.fcm.FcmUsers;
import org.radarbase.appserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@RunWith(SpringRunner.class)
@WebMvcTest(RadarUserController.class)
class RadarUserControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @MockBean private UserService userService;

  private Instant enrolmentDate = Instant.now().plus(Duration.ofSeconds(100));

  @BeforeEach
  void setUp() {
    FcmUserDto userDto =
        new FcmUserDto()
            .setSubjectId("test-user")
            .setFcmToken("xxxx")
            .setProjectId("test-project")
            .setEnrolmentDate(enrolmentDate)
            .setLanguage("es")
            .setTimezone(0d);

    given(userService.getAllRadarUsers())
        .willReturn(new FcmUsers().setUsers(List.of(userDto.setId(1L))));

    given(userService.getUserById(1L)).willReturn(userDto.setId(1L));

    given(userService.getUserBySubjectId("test-user")).willReturn(userDto.setId(1L));

    given(userService.getUsersByProjectId("test-project"))
        .willReturn(new FcmUsers().setUsers(List.of(userDto.setId(1L))));

    FcmUserDto userDtoNew =
        new FcmUserDto()
            .setSubjectId("test-user-new")
            .setFcmToken("xxxxyyy")
            .setProjectId("test-project")
            .setEnrolmentDate(enrolmentDate)
            .setLanguage("en")
            .setTimezone(0d)
            .setId(2L);

    given(userService.saveUserInProject(userDtoNew)).willReturn(userDtoNew.setId(2L));

    FcmUserDto userUpdated =
        new FcmUserDto()
            .setSubjectId("test-user-updated")
            .setFcmToken("xxxxyyyzzz")
            .setProjectId("test-project")
            .setEnrolmentDate(enrolmentDate)
            .setLanguage("da")
            .setTimezone(0d)
            .setId(1L);

    given(userService.updateUser(userUpdated)).willReturn(userUpdated);
  }

  @Test
  void addUser() throws Exception {
    FcmUserDto userDtoNew =
        new FcmUserDto()
            .setSubjectId("test-user-new")
            .setFcmToken("xxxxyyy")
            .setProjectId("test-project")
            .setEnrolmentDate(enrolmentDate)
            .setLanguage("en")
            .setTimezone(0d)
            .setId(2L);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(new URI("/users"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDtoNew)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.fcmToken", is("xxxxyyy")))
        .andExpect(jsonPath("$.language", is("en")))
        .andExpect(jsonPath("$.enrolmentDate", is(enrolmentDate.toString())))
        .andExpect(jsonPath("$.id", is(2)));
  }

  @Test
  void addUserToProject() throws Exception {
    FcmUserDto userDtoNew =
        new FcmUserDto()
            .setSubjectId("test-user-new")
            .setFcmToken("xxxxyyy")
            .setEnrolmentDate(enrolmentDate)
            .setLanguage("en")
            .setTimezone(0d)
            .setId(2L);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(new URI("/projects/test-project/users"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDtoNew)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.fcmToken", is("xxxxyyy")))
        .andExpect(jsonPath("$.language", is("en")))
        .andExpect(jsonPath("$.enrolmentDate", is(enrolmentDate.toString())))
        .andExpect(jsonPath("$.id", is(2)));
  }

  @Test
  void updateUserInProject() throws Exception {
    FcmUserDto userDtoUpdated =
        new FcmUserDto()
            .setSubjectId("test-user-updated")
            .setFcmToken("xxxxyyyzzz")
            .setEnrolmentDate(enrolmentDate)
            .setLanguage("da")
            .setTimezone(0d)
            .setId(1L);

    mockMvc
        .perform(
            MockMvcRequestBuilders.put(new URI("/projects/test-project/users"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDtoUpdated)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fcmToken", is("xxxxyyyzzz")))
        .andExpect(jsonPath("$.language", is("da")))
        .andExpect(jsonPath("$.enrolmentDate", is(enrolmentDate.toString())))
        .andExpect(jsonPath("$.id", is(1)));
  }

  @Test
  void updateUser() throws Exception {
    FcmUserDto userDtoUpdated =
        new FcmUserDto()
            .setSubjectId("test-user-updated")
            .setFcmToken("xxxxyyyzzz")
            .setProjectId("test-project")
            .setEnrolmentDate(enrolmentDate)
            .setLanguage("da")
            .setTimezone(0d)
            .setId(1L);

    mockMvc
        .perform(
            MockMvcRequestBuilders.put(new URI("/users"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDtoUpdated)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fcmToken", is("xxxxyyyzzz")))
        .andExpect(jsonPath("$.language", is("da")))
        .andExpect(jsonPath("$.enrolmentDate", is(enrolmentDate.toString())))
        .andExpect(jsonPath("$.id", is(1)));
  }

  @Test
  void getAllRadarUsers() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(new URI("/users")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.users[0].fcmToken", is("xxxx")))
        .andExpect(jsonPath("$.users[0].language", is("es")))
        .andExpect(jsonPath("$.users[0].enrolmentDate", is(enrolmentDate.toString())));
  }

  @Test
  void getRadarUserUsingId() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(new URI("/users/user?id=1")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fcmToken", is("xxxx")))
        .andExpect(jsonPath("$.language", is("es")))
        .andExpect(jsonPath("$.enrolmentDate", is(enrolmentDate.toString())));
  }

  @Test
  void getRadarUserUsingSubjectId() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(new URI("/users/test-user")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.fcmToken", is("xxxx")))
        .andExpect(jsonPath("$.language", is("es")))
        .andExpect(jsonPath("$.enrolmentDate", is(enrolmentDate.toString())));
  }

  @Test
  void getUsersUsingProjectId() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(new URI("/projects/test-project/users")))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.users[0].fcmToken", is("xxxx")))
        .andExpect(jsonPath("$.users[0].language", is("es")))
        .andExpect(jsonPath("$.users[0].enrolmentDate", is(enrolmentDate.toString())));
  }
}
