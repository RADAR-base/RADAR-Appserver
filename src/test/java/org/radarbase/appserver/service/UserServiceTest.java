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

  @Autowired UserService userService;

  @MockBean UserRepository userRepository;

  @MockBean ProjectRepository projectRepository;

  private Instant enrolmentDate = Instant.now().plus(Duration.ofSeconds(100));

  @BeforeEach
  void setUp() {
    // given
    Project project = new Project().setProjectId("test-project").setId(1L);

    Mockito.when(projectRepository.findByProjectId(project.getProjectId()))
        .thenReturn(Optional.of(project));

    User user =
        new User()
            .setFcmToken("xxxx")
            .setEnrolmentDate(enrolmentDate)
            .setProject(project)
            .setTimezone(0d)
            .setLanguage("en")
            .setSubjectId("test-user")
            .setId(1L);

    Mockito.when(userRepository.findAll()).thenReturn(List.of(user));

    Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

    Mockito.when(userRepository.findBySubjectId(user.getSubjectId())).thenReturn(Optional.of(user));

    Mockito.when(userRepository.findBySubjectIdAndProjectId("test-user", 1L))
        .thenReturn(Optional.of(user));

    Mockito.when(userRepository.findByProjectId(1L)).thenReturn(List.of(user));

    User userNew =
        new User()
            .setSubjectId("test-user-2")
            .setFcmToken("xxxx")
            .setProject(project)
            .setEnrolmentDate(enrolmentDate)
            .setLanguage("es")
            .setTimezone(0d);

    Mockito.when(userRepository.save(userNew)).thenReturn(userNew.setId(2L));

    User userUpdated =
        new User()
            .setSubjectId("test-user")
            .setFcmToken("xxxxyyy")
            .setProject(project)
            .setEnrolmentDate(enrolmentDate)
            .setLanguage("es")
            .setTimezone(72d)
            .setUserMetrics(
                new UserMetrics().setLastDelivered(enrolmentDate).setLastOpened(enrolmentDate));

    Mockito.when(userRepository.save(userUpdated)).thenReturn(userUpdated.setId(1L));
  }

  @Test
  void getAllRadarUsers() {
    FcmUsers users = userService.getAllRadarUsers();

    assertEquals("test-user", users.getUsers().get(0).getSubjectId());
    assertEquals("en", users.getUsers().get(0).getLanguage(), "en");
    assertEquals("test-project", users.getUsers().get(0).getProjectId());
  }

  @Test
  void getUserById() {

    FcmUserDto userDto = userService.getUserById(1L);

    assertEquals("test-user", userDto.getSubjectId());
    assertEquals("en", userDto.getLanguage());
    assertEquals("test-project", userDto.getProjectId());
  }

  @Test
  void getUserBySubjectId() {
    FcmUserDto userDto = userService.getUserBySubjectId("test-user");

    assertEquals("test-user", userDto.getSubjectId());
    assertEquals("en", userDto.getLanguage());
    assertEquals("test-project", userDto.getProjectId());
    assertEquals(Long.valueOf(1L), userDto.getId());
  }

  @Test
  void getUsersByProjectId() {
    FcmUsers users = userService.getUsersByProjectId("test-project");

    assertEquals("test-user", users.getUsers().get(0).getSubjectId());
    assertEquals("en", users.getUsers().get(0).getLanguage(), "en");
    assertEquals("test-project", users.getUsers().get(0).getProjectId());
  }

  @Test
  void saveUserInProject() {

    FcmUserDto userDtoNew =
        new FcmUserDto()
            .setSubjectId("test-user-2")
            .setFcmToken("xxxx")
            .setProjectId("test-project")
            .setEnrolmentDate(enrolmentDate)
            .setLanguage("es")
            .setTimezone(0d);

    FcmUserDto userDto = userService.saveUserInProject(userDtoNew);

    assertEquals("test-user-2", userDto.getSubjectId());
    assertEquals("es", userDto.getLanguage());
    assertEquals("test-project", userDto.getProjectId());
    assertEquals(Long.valueOf(2L), userDto.getId());
  }

  @Test
  void updateUser() {

    FcmUserDto userDtoNew =
        new FcmUserDto()
            .setSubjectId("test-user")
            .setFcmToken("xxxxyyy")
            .setProjectId("test-project")
            .setEnrolmentDate(enrolmentDate)
            .setLanguage("es")
            .setLastDelivered(enrolmentDate)
            .setLastOpened(enrolmentDate)
            .setTimezone(72d);

    FcmUserDto userDto = userService.updateUser(userDtoNew);

    assertEquals("test-user", userDto.getSubjectId());
    assertEquals("es", userDto.getLanguage());
    assertEquals("test-project", userDto.getProjectId());
    assertEquals(72d, userDto.getTimezone());
    assertEquals("xxxxyyy", userDto.getFcmToken());
    assertEquals(Long.valueOf(1L), userDto.getId());
  }

  @TestConfiguration
  static class UserServiceConfig {

    @Autowired UserRepository userRepository;

    @Autowired ProjectRepository projectRepository;

    private final UserConverter userConverter = new UserConverter();

    @Bean
    public UserService userServiceBeanConfig() {
      return new UserService(userConverter, userRepository, projectRepository);
    }
  }
}
