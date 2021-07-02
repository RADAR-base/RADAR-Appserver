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

package org.radarbase.appserver.service.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class GithubClient {

    private static final String GITHUB_API_URI = "api.github.com";
    private static final String GITHUB_API_ACCEPT_HEADER = "application/vnd.github.v3+json";
    private static final String LOCATION_HEADER = "location";
    private final transient ObjectMapper objectMapper;
    private final transient HttpClient client;

    @Value("${security.github.client.token}")
    private transient String githubToken;

    @SneakyThrows
    @Autowired
    public GithubClient(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).connectTimeout(Duration.ofSeconds(10)).build();
    }

    private static boolean isSuccessfulResponse(HttpResponse response) {
        return response.statusCode() >= 200 && response.statusCode() < 300;
    }

    public String getGithubContent(String url) throws IOException, InterruptedException {
        URI uri = URI.create(url);
        if (!this.isValidGithubUri(uri)) {
            throw new MalformedURLException("Invalid Github url.");
        }
        HttpResponse response = client.send(getRequest(uri), HttpResponse.BodyHandlers.ofString());
        if (isSuccessfulResponse(response)) {
            return response.body().toString();
        } else {
            log.error("Error getting Github content from URL {} : {}", url, response);
            throw new ResponseStatusException(
                    HttpStatus.valueOf(response.statusCode()), "Github content could not be retrieved");
        }
    }

    public boolean isValidGithubUri(URI uri) {
        return uri.getHost().contains(GITHUB_API_URI);
    }

    private HttpRequest getRequest(URI uri) {
        HttpRequest.Builder request = HttpRequest.newBuilder(uri)
                .header("Accept", GITHUB_API_ACCEPT_HEADER)
                .GET()
                .timeout(Duration.ofSeconds(10));
        if (githubToken != null && !githubToken.isEmpty()) {
            request.header("Authorization", "Bearer " + githubToken);
        }
        return request.build();
    }
}
