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

package org.radarbase.appserver.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.radarbase.appserver.controller.FcmNotificationControllerTest.PROJECT_ID;
import static org.radarbase.appserver.controller.FcmNotificationControllerTest.USER_ID;
import static org.radarbase.appserver.controller.RadarUserControllerTest.FCM_TOKEN_1;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.radarbase.appserver.converter.UserConverter;
import org.radarbase.appserver.dto.fcm.FcmUserDto;
import org.radarbase.appserver.dto.fcm.FcmUsers;
import org.radarbase.appserver.entity.Project;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.entity.UserMetrics;
import org.radarbase.appserver.repository.ProjectRepository;
import org.radarbase.appserver.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
class UserServiceTest {

    @Autowired
    private transient UserService userService;

    @MockBean
    private transient UserRepository userRepository;

    @MockBean
    private transient ProjectRepository projectRepository;

    private transient Instant enrolmentDate = Instant.now().plus(Duration.ofSeconds(100));
    private static final String TIMEZONE = "Europe/London";

    @BeforeEach
    void setUp() {
        // given
        Project project = new Project().setProjectId(PROJECT_ID).setId(1L);

        Mockito.when(projectRepository.findByProjectId(project.getProjectId()))
                .thenReturn(Optional.of(project));

        User user =
                new User()
                        .setFcmToken(FCM_TOKEN_1)
                        .setEnrolmentDate(enrolmentDate)
                        .setProject(project)
                        .setTimezone(TIMEZONE)
                        .setLanguage("en")
                        .setSubjectId(USER_ID)
                        .setId(1L);

        Mockito.when(userRepository.findAll()).thenReturn(List.of(user));

        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Mockito.when(userRepository.findBySubjectId(user.getSubjectId())).thenReturn(Optional.of(user));

        Mockito.when(userRepository.findBySubjectIdAndProjectId(USER_ID, 1L))
                .thenReturn(Optional.of(user));

        Mockito.when(userRepository.findByProjectId(1L)).thenReturn(List.of(user));

        User userNew =
                new User()
                        .setSubjectId(USER_ID + "-2")
                        .setFcmToken(FCM_TOKEN_1)
                        .setProject(project)
                        .setEnrolmentDate(enrolmentDate)
                        .setLanguage("es")
                        .setTimezone(TIMEZONE);

        Mockito.when(userRepository.save(userNew)).thenReturn(userNew.setId(2L));

        User userUpdated =
                new User()
                        .setSubjectId(USER_ID)
                        .setFcmToken("xxxxyyy")
                        .setProject(project)
                        .setEnrolmentDate(enrolmentDate)
                        .setLanguage("es")
                        .setTimezone(TIMEZONE)
                        .setUserMetrics(
                                new UserMetrics().setLastDelivered(enrolmentDate).setLastOpened(enrolmentDate));

        Mockito.when(userRepository.save(userUpdated)).thenReturn(userUpdated.setId(1L));
    }

    @Test
    void getAllRadarUsers() {
        FcmUsers users = userService.getAllRadarUsers();

        assertEquals(USER_ID, users.getUsers().get(0).getSubjectId());
        assertEquals("en", users.getUsers().get(0).getLanguage(), "en");
        assertEquals(PROJECT_ID, users.getUsers().get(0).getProjectId());
    }

    @Test
    void getUserById() {

        FcmUserDto userDto = userService.getUserById(1L);

        assertEquals(USER_ID, userDto.getSubjectId());
        assertEquals("en", userDto.getLanguage());
        assertEquals(PROJECT_ID, userDto.getProjectId());
    }

    @Test
    void getUserBySubjectId() {
        FcmUserDto userDto = userService.getUserBySubjectId(USER_ID);

        assertEquals(USER_ID, userDto.getSubjectId());
        assertEquals("en", userDto.getLanguage());
        assertEquals(PROJECT_ID, userDto.getProjectId());
        assertEquals(Long.valueOf(1L), userDto.getId());
    }

    @Test
    void getUsersByProjectId() {
        FcmUsers users = userService.getUsersByProjectId(PROJECT_ID);

        assertEquals(USER_ID, users.getUsers().get(0).getSubjectId());
        assertEquals("en", users.getUsers().get(0).getLanguage(), "en");
        assertEquals(PROJECT_ID, users.getUsers().get(0).getProjectId());
    }

    @Test
    void saveUserInProject() {

        FcmUserDto userDtoNew =
                new FcmUserDto()
                        .setSubjectId(USER_ID + "-2")
                        .setFcmToken(FCM_TOKEN_1)
                        .setProjectId(PROJECT_ID)
                        .setEnrolmentDate(enrolmentDate)
                        .setLanguage("es")
                        .setTimezone(TIMEZONE);

        FcmUserDto userDto = userService.saveUserInProject(userDtoNew);

        assertEquals(USER_ID + "-2", userDto.getSubjectId());
        assertEquals("es", userDto.getLanguage());
        assertEquals(PROJECT_ID, userDto.getProjectId());
        assertEquals(Long.valueOf(2L), userDto.getId());
    }

    @Test
    void updateUser() {

        FcmUserDto userDtoNew =
                new FcmUserDto()
                        .setSubjectId(USER_ID)
                        .setFcmToken("xxxxyyy")
                        .setProjectId(PROJECT_ID)
                        .setEnrolmentDate(enrolmentDate)
                        .setLanguage("es")
                        .setLastDelivered(enrolmentDate)
                        .setLastOpened(enrolmentDate)
                        .setTimezone(TIMEZONE);

        FcmUserDto userDto = userService.updateUser(userDtoNew);

        assertEquals(USER_ID, userDto.getSubjectId());
        assertEquals("es", userDto.getLanguage());
        assertEquals(PROJECT_ID, userDto.getProjectId());
        assertEquals("Europe/London", userDto.getTimezone());
        assertEquals("xxxxyyy", userDto.getFcmToken());
        assertEquals(Long.valueOf(1L), userDto.getId());
    }

    @TestConfiguration
    static class UserServiceConfig {

        @Autowired
        private transient UserRepository userRepository;

        @Autowired
        private transient ProjectRepository projectRepository;

        private final transient UserConverter userConverter = new UserConverter();

        @Bean
        public UserService userServiceBeanConfig() {
            return new UserService(userConverter, userRepository, projectRepository);
        }
    }
}
