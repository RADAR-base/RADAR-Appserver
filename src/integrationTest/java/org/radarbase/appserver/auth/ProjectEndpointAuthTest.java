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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.runner.RunWith;
import org.radarbase.appserver.auth.common.MPOAuthHelper;
import org.radarbase.appserver.auth.common.OAuthHelper;
import org.radarbase.appserver.dto.ProjectDto;
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
public class ProjectEndpointAuthTest {

  private static final HttpHeaders HEADERS = new HttpHeaders();
  private static HttpHeaders AUTH_HEADER;
  private static String ACCESS_TOKEN;
  private static OAuthHelper oAuthHelper;
  TestRestTemplate restTemplate = new TestRestTemplate();
  @LocalServerPort private int port;

  @BeforeAll
  static void init() {
    oAuthHelper = new MPOAuthHelper();
    AUTH_HEADER = new HttpHeaders();
    AUTH_HEADER.setBearerAuth(oAuthHelper.getAccessToken());
  }

  @Test
  public void unauthorisedCreateProject() {

    ProjectDto projectDto = new ProjectDto().setProjectId("radar");
    HttpEntity<ProjectDto> projectEntity = new HttpEntity<>(projectDto, HEADERS);

    ResponseEntity<ProjectDto> responseEntity =
        restTemplate.exchange(
            createURLWithPort("/projects"), HttpMethod.POST, projectEntity, ProjectDto.class);
    assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
  }

  @Test
  public void unauthorisedViewProjects() {

    HttpEntity<ProjectDto> projectEntity = new HttpEntity<>(null, HEADERS);

    ResponseEntity<ProjectDto> responseEntity =
        restTemplate.exchange(
            createURLWithPort("/projects"), HttpMethod.GET, projectEntity, ProjectDto.class);
    assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
  }

  @Test
  public void unauthorisedViewSingleProject() {

    HttpEntity<ProjectDto> projectEntity = new HttpEntity<>(null, HEADERS);

    ResponseEntity<ProjectDto> responseEntity =
        restTemplate.exchange(
            createURLWithPort("/projects/radar"), HttpMethod.GET, projectEntity, ProjectDto.class);
    assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
  }

  @Test
  public void forbiddenViewProjects() {
    HttpEntity<ProjectDto> projectEntity = new HttpEntity<>(null, AUTH_HEADER);

    ResponseEntity<ProjectDto> responseEntity =
        restTemplate.exchange(
            createURLWithPort("/projects"), HttpMethod.GET, projectEntity, ProjectDto.class);

    // Only Admins can view the list of all projects
    assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
  }

  @Test
  @Order(1)
  public void createSingleProjectWithAuth() {
    ProjectDto projectDto = new ProjectDto().setProjectId("radar");
    HttpEntity<ProjectDto> projectEntity = new HttpEntity<>(projectDto, AUTH_HEADER);

    ResponseEntity<ProjectDto> responseEntity =
        restTemplate.exchange(
            createURLWithPort("/projects"), HttpMethod.POST, projectEntity, ProjectDto.class);

    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
  }

  @Test
  @Order(2)
  public void getSingleProjectWithAuth() {
    HttpEntity<ProjectDto> projectEntity = new HttpEntity<>(null, AUTH_HEADER);

    ResponseEntity<ProjectDto> responseEntity =
        restTemplate.exchange(
            createURLWithPort("/projects/radar"), HttpMethod.GET, projectEntity, ProjectDto.class);

    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
  }

  @Test
  @Order(3)
  public void getForbiddenProjectWithAuth() {
    HttpEntity<ProjectDto> projectEntity = new HttpEntity<>(null, AUTH_HEADER);

    ResponseEntity<ProjectDto> responseEntity =
        restTemplate.exchange(
            createURLWithPort("/projects/test"), HttpMethod.GET, projectEntity, ProjectDto.class);

    // Access denied as the user has only access to the project that it is part of.
    assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
  }

  private String createURLWithPort(String uri) {
    return "http://localhost:" + port + uri;
  }
}
