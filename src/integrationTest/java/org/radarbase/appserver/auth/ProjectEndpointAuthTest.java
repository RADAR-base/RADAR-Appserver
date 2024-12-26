///*
// *
// *  *
// *  *  * Copyright 2018 King's College London
// *  *  *
// *  *  * Licensed under the Apache License, Version 2.0 (the "License");
// *  *  * you may not use this file except in compliance with the License.
// *  *  * You may obtain a copy of the License at
// *  *  *
// *  *  *   http://www.apache.org/licenses/LICENSE-2.0
// *  *  *
// *  *  * Unless required by applicable law or agreed to in writing, software
// *  *  * distributed under the License is distributed on an "AS IS" BASIS,
// *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// *  *  * See the License for the specific language governing permissions and
// *  *  * limitations under the License.
// *  *  *
// *  *
// *
// */
//
//package org.radarbase.appserver.auth;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
//import org.junit.jupiter.api.Order;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestMethodOrder;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.radarbase.appserver.auth.common.MPOAuthHelper;
//import org.radarbase.appserver.auth.common.OAuthHelper;
//import org.radarbase.appserver.dto.ProjectDto;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.web.client.TestRestTemplate;
//import org.springframework.boot.test.web.server.LocalServerPort;
//import org.springframework.http.HttpEntity;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.HttpMethod;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.web.client.ResourceAccessException;
//
//@ExtendWith(SpringExtension.class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@TestMethodOrder(OrderAnnotation.class)
//@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
//public class ProjectEndpointAuthTest {
//
//  public static final String PROJECT_PATH = "/projects";
//  private static final HttpHeaders HEADERS = new HttpHeaders();
//  private static HttpHeaders AUTH_HEADER;
//  private final transient TestRestTemplate restTemplate = new TestRestTemplate();
//  @LocalServerPort private transient int port;
//
//  @BeforeAll
//  static void init() {
//    OAuthHelper oAuthHelper = new MPOAuthHelper();
//    AUTH_HEADER = new HttpHeaders();
//    AUTH_HEADER.setBearerAuth(oAuthHelper.getAccessToken());
//  }
//
//  public static String createURLWithPort(int port, String uri) {
//    return "http://localhost:" + port + uri;
//  }
//
//  @Test
//  public void unauthorisedCreateProject() {
//
//    ProjectDto projectDto = new ProjectDto().setProjectId("radar");
//    HttpEntity<ProjectDto> projectEntity = new HttpEntity<>(projectDto, HEADERS);
//
//    ResponseEntity<ProjectDto> responseEntity = null;
//    try {
//      responseEntity =
//          restTemplate.exchange(
//              createURLWithPort(port, PROJECT_PATH),
//              HttpMethod.POST,
//              projectEntity,
//              ProjectDto.class);
//    } catch (ResourceAccessException e) {
//      assertEquals(responseEntity, null);
//    }
//  }
//
//  @Test
//  public void unauthorisedViewProjects() {
//
//    HttpEntity<ProjectDto> projectEntity = new HttpEntity<>(null, HEADERS);
//
//    ResponseEntity<ProjectDto> responseEntity =
//        restTemplate.exchange(
//            createURLWithPort(port, PROJECT_PATH), HttpMethod.GET, projectEntity, ProjectDto.class);
//    assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
//  }
//
//  @Test
//  public void unauthorisedViewSingleProject() {
//
//    HttpEntity<ProjectDto> projectEntity = new HttpEntity<>(null, HEADERS);
//
//    ResponseEntity<ProjectDto> responseEntity =
//        restTemplate.exchange(
//            createURLWithPort(port, "/projects/radar"),
//            HttpMethod.GET,
//            projectEntity,
//            ProjectDto.class);
//    assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
//  }
//
//  @Test
//  public void forbiddenViewProjects() {
//    HttpEntity<ProjectDto> projectEntity = new HttpEntity<>(null, AUTH_HEADER);
//
//    ResponseEntity<ProjectDto> responseEntity =
//        restTemplate.exchange(
//            createURLWithPort(port, PROJECT_PATH), HttpMethod.GET, projectEntity, ProjectDto.class);
//
//    // Only Admins can view the list of all projects
//    assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
//  }
//
//  @Test
//  @Order(1)
//  public void createSingleProjectWithAuth() {
//    ProjectDto projectDto = new ProjectDto().setProjectId("radar");
//    HttpEntity<ProjectDto> projectEntity = new HttpEntity<>(projectDto, AUTH_HEADER);
//
//    ResponseEntity<ProjectDto> responseEntity =
//        restTemplate.exchange(
//            createURLWithPort(port, PROJECT_PATH),
//            HttpMethod.POST,
//            projectEntity,
//            ProjectDto.class);
//
//    if (responseEntity.getStatusCode().equals(HttpStatus.EXPECTATION_FAILED)) {
//      // The auth was successful but expectation failed if the project already exits.
//      // Since this is just an auth test we can return.
//      return;
//    }
//    assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
//  }
//
//  @Test
//  @Order(2)
//  public void getSingleProjectWithAuth() {
//    HttpEntity<ProjectDto> projectEntity = new HttpEntity<>(null, AUTH_HEADER);
//
//    ResponseEntity<ProjectDto> responseEntity =
//        restTemplate.exchange(
//            createURLWithPort(port, "/projects/radar"),
//            HttpMethod.GET,
//            projectEntity,
//            ProjectDto.class);
//
//    assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//  }
//
//  @Test
//  @Order(3)
//  public void getForbiddenProjectWithAuth() {
//    HttpEntity<ProjectDto> projectEntity = new HttpEntity<>(null, AUTH_HEADER);
//
//    ResponseEntity<ProjectDto> responseEntity =
//        restTemplate.exchange(
//            createURLWithPort(port, "/projects/test"),
//            HttpMethod.GET,
//            projectEntity,
//            ProjectDto.class);
//
//    // Access denied as the user has only access to the project that it is part of.
//    assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
//  }
//}