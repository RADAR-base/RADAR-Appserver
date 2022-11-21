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
import static org.radarbase.appserver.controller.FcmNotificationControllerTest.PROJECT_ID;
import static org.radarbase.appserver.controller.FcmNotificationControllerTest.USER_ID;
import static org.radarbase.appserver.controller.RadarProjectControllerTest.ID_JSON_PATH;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.radarbase.appserver.dto.fcm.FcmUserDto;
import org.radarbase.appserver.dto.fcm.FcmUsers;
import org.radarbase.appserver.exception.InvalidUserDetailsException;
import org.radarbase.appserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.radarbase.appserver.exception.InvalidUserDetailsException;

@ExtendWith(SpringExtension.class)
@WebMvcTest(RadarUserController.class)
public class RadarUserControllerTest {

    public static final String FCM_TOKEN_1 = "xxxx";
    private static final String FCM_TOKEN_2 = "xxxxyyy";
    private static final String FCM_TOKEN_3 = "xxxxyyyzzz";
    private static final String FCM_TOKEN_JSON_PATH = "$.fcmToken";
    private static final String LANGUAGE_JSON_PATH = "$.language";
    private static final String ENROLMENT_DATE_JSON_PATH = "$.enrolmentDate";
    public static final String TIMEZONE = "Europe/London";
    @Autowired
    private transient MockMvc mockMvc;
    @Autowired
    private transient ObjectMapper objectMapper;
    @MockBean
    private transient UserService userService;
    private transient Instant enrolmentDate = Instant.now().plus(Duration.ofSeconds(100));

    @BeforeEach
    void setUp() {
        FcmUserDto userDto =
                new FcmUserDto()
                        .setSubjectId(USER_ID)
                        .setFcmToken(FCM_TOKEN_1)
                        .setProjectId(PROJECT_ID)
                        .setEnrolmentDate(enrolmentDate)
                        .setLanguage("es")
                        .setTimezone(TIMEZONE);

        given(userService.getAllRadarUsers())
                .willReturn(new FcmUsers().setUsers(List.of(userDto.setId(1L))));

        given(userService.getUserById(1L)).willReturn(userDto.setId(1L));

        given(userService.getUserBySubjectId(USER_ID)).willReturn(userDto.setId(1L));

        given(userService.getUsersByProjectId(PROJECT_ID))
                .willReturn(new FcmUsers().setUsers(List.of(userDto.setId(1L))));

        FcmUserDto userDtoNew =
                new FcmUserDto()
                        .setSubjectId(USER_ID + "-new")
                        .setFcmToken(FCM_TOKEN_2)
                        .setProjectId(PROJECT_ID)
                        .setEnrolmentDate(enrolmentDate)
                        .setLanguage("en")
                        .setTimezone(TIMEZONE)
                        .setId(2L);

        given(userService.saveUserInProject(userDtoNew)).willReturn(userDtoNew.setId(2L));

        FcmUserDto userDtoSameToken =
                new FcmUserDto()
                        .setSubjectId(USER_ID + "-new-user")
                        .setFcmToken(FCM_TOKEN_2)
                        .setProjectId(PROJECT_ID)
                        .setEnrolmentDate(enrolmentDate)
                        .setLanguage("en")
                        .setTimezone(TIMEZONE)
                        .setId(5L);

        given(userService.saveUserInProject(userDtoSameToken)).willThrow(new InvalidUserDetailsException("User already exists"));

        FcmUserDto userUpdated =
                new FcmUserDto()
                        .setSubjectId(USER_ID + "-updated")
                        .setFcmToken(FCM_TOKEN_3)
                        .setProjectId(PROJECT_ID)
                        .setEnrolmentDate(enrolmentDate)
                        .setLanguage("da")
                        .setTimezone(TIMEZONE)
                        .setId(1L);

        given(userService.updateUser(userUpdated)).willReturn(userUpdated);
    }

    @Test
    void addUserToProject() throws Exception {
        FcmUserDto userDtoNew =
                new FcmUserDto()
                        .setSubjectId(USER_ID + "-new")
                        .setFcmToken(FCM_TOKEN_2)
                        .setEnrolmentDate(enrolmentDate)
                        .setLanguage("en")
                        .setTimezone(TIMEZONE)
                        .setId(2L);

        mockMvc
                .perform(
                        MockMvcRequestBuilders.post(new URI("/projects/test-project/users"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userDtoNew)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath(FCM_TOKEN_JSON_PATH, is(FCM_TOKEN_2)))
                .andExpect(jsonPath(LANGUAGE_JSON_PATH, is("en")))
                .andExpect(jsonPath(ENROLMENT_DATE_JSON_PATH, is(enrolmentDate.toString())))
                .andExpect(jsonPath(ID_JSON_PATH, is(2)));

        FcmUserDto userDtoSameToken =
                new FcmUserDto()
                        .setSubjectId(USER_ID + "-new-user")
                        .setFcmToken(FCM_TOKEN_2)
                        .setEnrolmentDate(enrolmentDate)
                        .setLanguage("en")
                        .setTimezone(TIMEZONE)
                        .setId(5L);

        mockMvc
                .perform(
                        MockMvcRequestBuilders.post(new URI("/projects/test-project/users"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userDtoSameToken)))
                .andExpect(status().isExpectationFailed());

    }

    @Test
    void updateUserInProject() throws Exception {
        FcmUserDto userDtoUpdated =
                new FcmUserDto()
                        .setSubjectId("test-user-updated")
                        .setFcmToken(FCM_TOKEN_3)
                        .setEnrolmentDate(enrolmentDate)
                        .setLanguage("da")
                        .setTimezone(TIMEZONE)
                        .setId(1L);

        mockMvc
                .perform(
                        MockMvcRequestBuilders.put(new URI("/projects/test-project/users/test-user-updated"))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userDtoUpdated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath(FCM_TOKEN_JSON_PATH, is(FCM_TOKEN_3)))
                .andExpect(jsonPath(LANGUAGE_JSON_PATH, is("da")))
                .andExpect(jsonPath(ENROLMENT_DATE_JSON_PATH, is(enrolmentDate.toString())))
                .andExpect(jsonPath(ID_JSON_PATH, is(1)));
    }

    @Test
    void getAllRadarUsers() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get(new URI("/users")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[0].fcmToken", is(FCM_TOKEN_1)))
                .andExpect(jsonPath("$.users[0].language", is("es")))
                .andExpect(jsonPath("$.users[0].enrolmentDate", is(enrolmentDate.toString())));
    }

    @Test
    void getRadarUserUsingId() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get(new URI("/users/user?id=1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath(FCM_TOKEN_JSON_PATH, is(FCM_TOKEN_1)))
                .andExpect(jsonPath(LANGUAGE_JSON_PATH, is("es")))
                .andExpect(jsonPath(ENROLMENT_DATE_JSON_PATH, is(enrolmentDate.toString())));
    }

    @Test
    void getRadarUserUsingSubjectId() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get(new URI("/users/test-user")))
                .andExpect(status().isOk())
                .andExpect(jsonPath(FCM_TOKEN_JSON_PATH, is(FCM_TOKEN_1)))
                .andExpect(jsonPath(LANGUAGE_JSON_PATH, is("es")))
                .andExpect(jsonPath(ENROLMENT_DATE_JSON_PATH, is(enrolmentDate.toString())));
    }

    @Test
    void getUsersUsingProjectId() throws Exception {
        mockMvc
                .perform(MockMvcRequestBuilders.get(new URI("/projects/test-project/users")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.users[0].fcmToken", is(FCM_TOKEN_1)))
                .andExpect(jsonPath("$.users[0].language", is("es")))
                .andExpect(jsonPath("$.users[0].enrolmentDate", is(enrolmentDate.toString())));
    }
}
