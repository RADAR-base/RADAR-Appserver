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

package org.radarbase.appserver.auth.common;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class MPOAuthHelper implements OAuthHelper {
  private static final ObjectMapper mapper = new ObjectMapper();
  private static final String ACCESS_TOKEN;
  private static final String MP_URL = "http://localhost:8081";
  private static final String MP_CLIENT = "ManagementPortalapp";
  private static final String REST_CLIENT = "pRMT";
  private static final String USER = "sub-1";
  private static final String ADMIN_USER = "admin";
  private static final String ADMIN_PASSWORD = "admin";
  private static final String MpPairUri =
      UriComponentsBuilder.fromHttpUrl(MP_URL)
          .path("api")
          .path("/")
          .path("oauth-clients")
          .path("/")
          .path("pair")
          .queryParam("clientId", REST_CLIENT)
          .queryParam("login", USER)
          .toUriString();

  private static final String MpTokenUri =
      UriComponentsBuilder.fromHttpUrl(MP_URL).path("oauth").path("/").path("token").toUriString();

  static {
    // Get valid token from Management Portal
    final RestTemplate restTemplate = new RestTemplate();

    MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
    map.add("username", ADMIN_USER);
    map.add("password", ADMIN_PASSWORD);
    map.add("grant_type", "password");

    HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setBasicAuth(MP_CLIENT, "");

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, httpHeaders);
    ResponseEntity<String> response =
        restTemplate.exchange(MpTokenUri, HttpMethod.POST, request, String.class);
    String adminAccessToken = getProperty(response, "access_token");

    httpHeaders = new HttpHeaders();
    httpHeaders.setBearerAuth(adminAccessToken);

    request = new HttpEntity<>(null, httpHeaders);
    response = restTemplate.exchange(MpPairUri, HttpMethod.GET, request, String.class);
    String tokenUrl = getProperty(response, "tokenUrl");

    response = restTemplate.exchange(tokenUrl, HttpMethod.GET, request, String.class);

    String refreshToken = getProperty(response, "refreshToken");

    httpHeaders = new HttpHeaders();
    httpHeaders.setBasicAuth(REST_CLIENT, "");
    httpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    map = new LinkedMultiValueMap<>();
    map.add("refresh_token", refreshToken);
    map.add("grant_type", "refresh_token");

    request = new HttpEntity<>(map, httpHeaders);
    response = restTemplate.exchange(MpTokenUri, HttpMethod.POST, request, String.class);

    ACCESS_TOKEN = getProperty(response, "access_token");
  }

  private static String getProperty(ResponseEntity<String> response, String property) {
    if (response.getStatusCode().isError()) {
      throw new IllegalStateException("The request was not successful: " + response.toString());
    }
    JsonNode root;
    try {
      root = mapper.readTree(response.getBody());
    } catch (IOException exc) {
      throw new IllegalStateException(
          "The property " + property + " could not be retrieved from response " + response);
    }
    JsonNode propertyNode = root.get(property);

    if (propertyNode != null) {
      return propertyNode.asText();
    } else {
      throw new IllegalStateException("Property not found in the response");
    }
  }

  public String getAccessToken() {
    return ACCESS_TOKEN;
  }
}
