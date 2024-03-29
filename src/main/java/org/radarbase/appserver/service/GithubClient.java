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

import jakarta.annotation.Nonnull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class GithubClient {
    private static final String GITHUB_API_URI = "api.github.com";
    private static final String GITHUB_API_ACCEPT_HEADER = "application/vnd.github.v3+json";

    @Nonnull
    private final transient String authorizationHeader;

    private transient final Duration httpTimeout;
    private transient final HttpClient client;

    @Value("${security.github.client.maxContentLength:1000000}")
    private transient int maxContentLength;

    @SneakyThrows
    @Autowired
    public GithubClient(
            @Value("${security.github.client.timeout:10}") int httpTimeout,
            @Value("${security.github.client.token:}") String githubToken) {
        this.authorizationHeader = githubToken != null ? "Bearer " + githubToken.trim() : "";
        this.httpTimeout = Duration.ofSeconds(httpTimeout);
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(this.httpTimeout)
                .build();
    }

    public String getGithubContent(String url) throws IOException, InterruptedException {
        log.debug("Fetching Github URL {}", url);
        URI uri = URI.create(url);
        HttpResponse<InputStream> response = makeRequest(uri);

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            checkContentLengthHeader(response);

            try (InputStream inputStream = response.body()) {
                byte[] bytes = inputStream.readNBytes(maxContentLength + 1);
                checkContentLength(bytes.length);
                return new String(bytes);
            }
        } else {
            log.error("Error getting Github content from URL {} : {}", url, response);
            throw new ResponseStatusException(
                    HttpStatus.valueOf(response.statusCode()), "Github content could not be retrieved");
        }
    }

    private HttpResponse<InputStream> makeRequest(URI uri) throws InterruptedException {
        try {
            return client.send(getRequest(uri), HttpResponse.BodyHandlers.ofInputStream());
        } catch (IOException ex) {
            log.error("Failed to retrieve data from github {}: {}", uri, ex.toString());
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Github responded with an error.");
        }
    }

    private void checkContentLengthHeader(HttpResponse<?> response) {
        response.headers().firstValue("Content-Length")
                .map((l) -> {
                    try {
                        return Integer.valueOf(l);
                    } catch (NumberFormatException ex) {
                        return null;
                    }
                })
                .ifPresent(this::checkContentLength);
    }

    private void checkContentLength(int contentLength) {
        if (contentLength > maxContentLength) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Github content is too large");
        }
    }

    public URI getValidGithubUri(URI uri) throws IOException {
        if (uri.getHost().equalsIgnoreCase(GITHUB_API_URI)
                && uri.getScheme().equalsIgnoreCase("https")
                && (uri.getPort() == -1 || uri.getPort() == 443)) {
                    return UriComponentsBuilder.newInstance()
                        .scheme("https").host(uri.getHost()).path(uri.getPath()).query(uri.getQuery()).build().toUri();
                }
        else {
            throw new MalformedURLException("Invalid Github url.");
        }
    }

    private HttpRequest getRequest(URI uri) throws IOException {
        URI validUri = this.getValidGithubUri(uri);
        HttpRequest.Builder request = HttpRequest.newBuilder(validUri)
                .header("Accept", GITHUB_API_ACCEPT_HEADER)
                .GET()
                .timeout(httpTimeout);
        if (!authorizationHeader.isEmpty()) {
            request.header("Authorization", authorizationHeader);
        }
        return request.build();
    }
}
