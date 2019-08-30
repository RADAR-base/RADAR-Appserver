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

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
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
import org.radarbase.appserver.dto.fcm.FcmNotificationDto;
import org.radarbase.appserver.dto.fcm.FcmNotifications;
import org.radarbase.appserver.dto.fcm.FcmUserDto;
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
public class NotificationEndpointAuthTest {
  private static final HttpHeaders HEADERS = new HttpHeaders();
  private static final String NOTIFICATION_PATH = "/messaging/notifications";
  private static final String DEFAULT_USER = "/sub-1";
  private static HttpHeaders AUTH_HEADER;
  private static TestRestTemplate restTemplate = new TestRestTemplate();
  private final transient FcmNotificationDto fcmNotificationDto =
      new FcmNotificationDto()
          .setScheduledTime(Instant.now().plus(Duration.ofSeconds(100)))
          .setBody("Test Body")
          .setSourceId("test-source")
          .setTitle("Test Title")
          .setTtlSeconds(86400)
          .setFcmMessageId("123455")
          .setAdditionalData(new HashMap<>())
          .setAppPackage("armt")
          .setSourceType("armt")
          .setType("ESM");
  @LocalServerPort private transient int port;

  @BeforeAll
  static void init() {
    OAuthHelper oAuthHelper = new MPOAuthHelper();
    AUTH_HEADER = new HttpHeaders();
    AUTH_HEADER.setBearerAuth(oAuthHelper.getAccessToken());
  }

  @BeforeEach
  public void createUserAndProject() {
    ProjectDto projectDto = new ProjectDto().setProjectId("radar");
    HttpEntity<ProjectDto> projectEntity = new HttpEntity<>(projectDto, AUTH_HEADER);

    restTemplate.exchange(
        "http://localhost:" + port + ProjectEndpointAuthTest.PROJECT_PATH,
        HttpMethod.POST,
        projectEntity,
        ProjectDto.class);

    final FcmUserDto userDto =
        new FcmUserDto()
            .setProjectId("radar")
            .setLanguage("en")
            .setEnrolmentDate(Instant.now())
            .setFcmToken("xxx")
            .setSubjectId("sub-1");

    HttpEntity<FcmUserDto> userDtoHttpEntity = new HttpEntity<>(userDto, AUTH_HEADER);

    restTemplate.exchange(
        createURLWithPort(
            port,
            ProjectEndpointAuthTest.PROJECT_PATH
                + UserEndpointAuthTest.DEFAULT_PROJECT
                + UserEndpointAuthTest.USER_PATH),
        HttpMethod.POST,
        userDtoHttpEntity,
        FcmUserDto.class);
  }

  @Test
  public void unauthorisedViewNotificationsForUser() {
    HttpEntity<FcmNotifications> notificationDtoHttpEntity = new HttpEntity<>(null, HEADERS);

    ResponseEntity<FcmNotifications> notificationDtoResponseEntity =
        restTemplate.exchange(
            createURLWithPort(
                port,
                ProjectEndpointAuthTest.PROJECT_PATH
                    + UserEndpointAuthTest.DEFAULT_PROJECT
                    + UserEndpointAuthTest.USER_PATH
                    + DEFAULT_USER
                    + "/messaging/notifications"),
            HttpMethod.GET,
            notificationDtoHttpEntity,
            FcmNotifications.class);

    assertEquals(HttpStatus.UNAUTHORIZED, notificationDtoResponseEntity.getStatusCode());
  }

  @Test
  public void unauthorisedViewNotificationsForProject() {
    HttpEntity<FcmNotifications> notificationDtoHttpEntity = new HttpEntity<>(null, HEADERS);

    ResponseEntity<FcmNotifications> notificationDtoResponseEntity =
        restTemplate.exchange(
            createURLWithPort(
                port,
                ProjectEndpointAuthTest.PROJECT_PATH
                    + UserEndpointAuthTest.DEFAULT_PROJECT
                    + "/messaging/notifications"),
            HttpMethod.GET,
            notificationDtoHttpEntity,
            FcmNotifications.class);

    assertEquals(HttpStatus.UNAUTHORIZED, notificationDtoResponseEntity.getStatusCode());
  }

  @Test
  public void unauthorisedCreateNotificationsForUser() {
    HttpEntity<FcmNotificationDto> notificationDtoHttpEntity =
        new HttpEntity<>(fcmNotificationDto, HEADERS);

    ResponseEntity<FcmNotificationDto> notificationDtoResponseEntity =
        restTemplate.exchange(
            createURLWithPort(
                port,
                ProjectEndpointAuthTest.PROJECT_PATH
                    + UserEndpointAuthTest.DEFAULT_PROJECT
                    + UserEndpointAuthTest.USER_PATH
                    + DEFAULT_USER
                    + NOTIFICATION_PATH),
            HttpMethod.POST,
            notificationDtoHttpEntity,
            FcmNotificationDto.class);

    assertEquals(HttpStatus.UNAUTHORIZED, notificationDtoResponseEntity.getStatusCode());
  }

  @Test
  @Order(1)
  public void createNotificationForUser() {
    HttpEntity<FcmNotificationDto> notificationDtoHttpEntity =
        new HttpEntity<>(fcmNotificationDto, AUTH_HEADER);

    ResponseEntity<FcmNotificationDto> notificationDtoResponseEntity =
        restTemplate.exchange(
            createURLWithPort(
                port,
                ProjectEndpointAuthTest.PROJECT_PATH
                    + UserEndpointAuthTest.DEFAULT_PROJECT
                    + UserEndpointAuthTest.USER_PATH
                    + DEFAULT_USER
                    + NOTIFICATION_PATH),
            HttpMethod.POST,
            notificationDtoHttpEntity,
            FcmNotificationDto.class);

    assertEquals(HttpStatus.CREATED, notificationDtoResponseEntity.getStatusCode());
  }

  @Test
  @Order(2)
  public void createBatchNotificationsForUser() {
    HttpEntity<FcmNotifications> notificationDtoHttpEntity =
        new HttpEntity<>(
            new FcmNotifications()
                .setNotifications(
                    List.of(fcmNotificationDto.setTitle("new title").setFcmMessageId("xxxyyyy"))),
            AUTH_HEADER);

    ResponseEntity<FcmNotifications> notificationDtoResponseEntity =
        restTemplate.exchange(
            createURLWithPort(
                port,
                ProjectEndpointAuthTest.PROJECT_PATH
                    + UserEndpointAuthTest.DEFAULT_PROJECT
                    + UserEndpointAuthTest.USER_PATH
                    + DEFAULT_USER
                    + NOTIFICATION_PATH
                    + "/batch"),
            HttpMethod.POST,
            notificationDtoHttpEntity,
            FcmNotifications.class);

    assertEquals(HttpStatus.OK, notificationDtoResponseEntity.getStatusCode());
  }

  @Test
  public void viewNotificationsForUser() {
    HttpEntity<FcmNotifications> notificationDtoHttpEntity = new HttpEntity<>(null, AUTH_HEADER);

    ResponseEntity<FcmNotifications> notificationDtoResponseEntity =
        restTemplate.exchange(
            createURLWithPort(
                port,
                ProjectEndpointAuthTest.PROJECT_PATH
                    + UserEndpointAuthTest.DEFAULT_PROJECT
                    + UserEndpointAuthTest.USER_PATH
                    + DEFAULT_USER
                    + NOTIFICATION_PATH),
            HttpMethod.GET,
            notificationDtoHttpEntity,
            FcmNotifications.class);

    assertEquals(HttpStatus.OK, notificationDtoResponseEntity.getStatusCode());
  }

  @Test
  public void viewNotificationsForProject() {
    HttpEntity<FcmNotifications> notificationDtoHttpEntity = new HttpEntity<>(null, AUTH_HEADER);

    ResponseEntity<FcmNotifications> notificationDtoResponseEntity =
        restTemplate.exchange(
            createURLWithPort(
                port,
                ProjectEndpointAuthTest.PROJECT_PATH
                    + UserEndpointAuthTest.DEFAULT_PROJECT
                    + NOTIFICATION_PATH),
            HttpMethod.GET,
            notificationDtoHttpEntity,
            FcmNotifications.class);

    assertEquals(HttpStatus.OK, notificationDtoResponseEntity.getStatusCode());
  }

  @Test
  public void forbiddenViewNotificationsForOtherUser() {
    HttpEntity<FcmNotifications> notificationDtoHttpEntity = new HttpEntity<>(null, AUTH_HEADER);

    ResponseEntity<FcmNotifications> notificationDtoResponseEntity =
        restTemplate.exchange(
            createURLWithPort(
                port,
                ProjectEndpointAuthTest.PROJECT_PATH
                    + UserEndpointAuthTest.DEFAULT_PROJECT
                    + UserEndpointAuthTest.USER_PATH
                    + "/sub-2"
                    + NOTIFICATION_PATH),
            HttpMethod.GET,
            notificationDtoHttpEntity,
            FcmNotifications.class);

    assertEquals(HttpStatus.FORBIDDEN, notificationDtoResponseEntity.getStatusCode());
  }

  @Test
  public void forbiddenViewNotificationsForOtherProject() {
    HttpEntity<FcmNotifications> notificationDtoHttpEntity = new HttpEntity<>(null, HEADERS);

    ResponseEntity<FcmNotifications> notificationDtoResponseEntity =
        restTemplate.exchange(
            createURLWithPort(
                port, ProjectEndpointAuthTest.PROJECT_PATH + "/test" + NOTIFICATION_PATH),
            HttpMethod.GET,
            notificationDtoHttpEntity,
            FcmNotifications.class);

    assertEquals(HttpStatus.UNAUTHORIZED, notificationDtoResponseEntity.getStatusCode());
  }
}
