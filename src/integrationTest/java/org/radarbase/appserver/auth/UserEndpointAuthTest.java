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

package org.radarbase.appserver.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.radarbase.appserver.auth.ProjectEndpointAuthTest.createURLWithPort;

import java.time.Instant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.runner.RunWith;
import org.radarbase.appserver.auth.common.MPOAuthHelper;
import org.radarbase.appserver.auth.common.OAuthHelper;
import org.radarbase.appserver.dto.ProjectDto;
import org.radarbase.appserver.dto.fcm.FcmUserDto;
import org.radarbase.appserver.dto.fcm.FcmUsers;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(OrderAnnotation.class)
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class UserEndpointAuthTest {

  public static final String USER_PATH = "/users";
  public static final String DEFAULT_PROJECT = "/radar";
  private static final HttpHeaders HEADERS = new HttpHeaders();
  private static HttpHeaders AUTH_HEADER;
  private static TestRestTemplate restTemplate = new TestRestTemplate();
  private final transient FcmUserDto userDto =
      new FcmUserDto()
          .setProjectId("radar")
          .setLanguage("en")
          .setEnrolmentDate(Instant.now())
          .setFcmToken("xxx")
          .setSubjectId("sub-1");
  @LocalServerPort private transient int port;

  @BeforeAll
  static void init() {
    OAuthHelper oAuthHelper = new MPOAuthHelper();
    AUTH_HEADER = new HttpHeaders();
    AUTH_HEADER.setBearerAuth(oAuthHelper.getAccessToken());
  }

  @BeforeEach
  public void createProject() {
    ProjectDto projectDto = new ProjectDto().setProjectId("radar");
    HttpEntity<ProjectDto> projectEntity = new HttpEntity<>(projectDto, AUTH_HEADER);

    restTemplate.exchange(
        createURLWithPort(port, ProjectEndpointAuthTest.PROJECT_PATH),
        HttpMethod.POST,
        projectEntity,
        ProjectDto.class);
  }

  @Test
  public void unauthorisedViewSingleUser() {

    HttpEntity<FcmUserDto> userDtoHttpEntity = new HttpEntity<>(null, HEADERS);

    ResponseEntity<FcmUserDto> responseEntity =
        restTemplate.exchange(
            createURLWithPort(port, ProjectEndpointAuthTest.PROJECT_PATH)
                + DEFAULT_PROJECT
                + USER_PATH
                + "/sub-1",
            HttpMethod.GET,
            userDtoHttpEntity,
            FcmUserDto.class);

    assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
  }

  @Test
  public void unauthorisedCreateUser() {
    HttpEntity<FcmUserDto> userDtoHttpEntity = new HttpEntity<>(userDto, HEADERS);

    ResponseEntity<FcmUserDto> responseEntity =
        restTemplate.exchange(
            createURLWithPort(
                port, ProjectEndpointAuthTest.PROJECT_PATH + DEFAULT_PROJECT + USER_PATH),
            HttpMethod.POST,
            userDtoHttpEntity,
            FcmUserDto.class);

    assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
  }

  @Test
  @Order(1)
  public void createUser() {
    HttpEntity<FcmUserDto> userDtoHttpEntity = new HttpEntity<>(userDto, AUTH_HEADER);

    ResponseEntity<FcmUserDto> responseEntity =
        restTemplate.exchange(
            createURLWithPort(
                port, ProjectEndpointAuthTest.PROJECT_PATH + DEFAULT_PROJECT + USER_PATH),
            HttpMethod.POST,
            userDtoHttpEntity,
            FcmUserDto.class);

    if (responseEntity.getStatusCode().equals(HttpStatus.EXPECTATION_FAILED)) {
      // The auth was successful but expectation failed if the user already exits.
      // Since this is just an auth test we can return.
      return;
    }

    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
  }

  @Test
  @Order(2)
  public void viewUser() {
    HttpEntity<FcmUserDto> userDtoHttpEntity = new HttpEntity<>(null, AUTH_HEADER);

    ResponseEntity<FcmUserDto> responseEntity =
        restTemplate.exchange(
            createURLWithPort(
                port,
                ProjectEndpointAuthTest.PROJECT_PATH + DEFAULT_PROJECT + USER_PATH + "/sub-1"),
            HttpMethod.GET,
            userDtoHttpEntity,
            FcmUserDto.class);

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }

  @Test
  @Order(3)
  public void viewUsersInProject() {
    HttpEntity<FcmUsers> userDtoHttpEntity = new HttpEntity<>(null, AUTH_HEADER);

    ResponseEntity<FcmUsers> responseEntity =
        restTemplate.exchange(
            createURLWithPort(
                port, ProjectEndpointAuthTest.PROJECT_PATH + DEFAULT_PROJECT + USER_PATH),
            HttpMethod.GET,
            userDtoHttpEntity,
            FcmUsers.class);

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }

  @Test
  @Order(4)
  public void forbiddenViewUsersInOtherProject() {
    HttpEntity<FcmUsers> userDtoHttpEntity = new HttpEntity<>(null, AUTH_HEADER);

    ResponseEntity<FcmUsers> responseEntity =
        restTemplate.exchange(
            createURLWithPort(port, ProjectEndpointAuthTest.PROJECT_PATH + "/test" + USER_PATH),
            HttpMethod.GET,
            userDtoHttpEntity,
            FcmUsers.class);

    assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
  }

  @Test
  @Order(5)
  public void forbiddenViewAllUsers() {
    HttpEntity<FcmUsers> userDtoHttpEntity = new HttpEntity<>(null, AUTH_HEADER);

    ResponseEntity<FcmUsers> responseEntity =
        restTemplate.exchange(
            createURLWithPort(port, USER_PATH), HttpMethod.GET, userDtoHttpEntity, FcmUsers.class);

    assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
  }
}
