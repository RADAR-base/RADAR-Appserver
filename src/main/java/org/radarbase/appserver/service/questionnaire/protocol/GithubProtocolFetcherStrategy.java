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

package org.radarbase.appserver.service.questionnaire.protocol;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.google.common.net.HttpHeaders;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.radarbase.appserver.dto.protocol.Protocol;
import org.radarbase.appserver.util.CachedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class GithubProtocolFetcherStrategy implements ProtocolFetcherStrategy {

    private static final String GITHUB_API_URI = "https://api.github.com/repos/";
    private static final String GITHUB_API_ACCEPT_HEADER = "application/vnd.github.v3+json";
    private final transient String protocolRepo;
    private final transient String protocolFileName;
    private final transient String protocolBranch;
    private final transient ObjectMapper objectMapper;
    // Keeps a cache of github URI's associated with protocol for each project
    private final transient CachedMap<String, URI> projectProtocolUriMap;
    private final transient HttpClient client;

    @SneakyThrows
    @Autowired
    public GithubProtocolFetcherStrategy(
            @Value("${radar.questionnaire.protocol.github.repo.path}") String protocolRepo,
            @Value("${radar.questionnaire.protocol.github.file.name}") String protocolFileName,
            @Value("${radar.questionnaire.protocol.github.branch}") String protocolBranch,
            ObjectMapper objectMapper) {

        if (protocolRepo == null
                || protocolRepo.isEmpty()
                || protocolFileName == null
                || protocolFileName.isEmpty()) {
            throw new IllegalArgumentException("Protocol Repo And File name needs to be configured.");
        }

        this.protocolRepo = protocolRepo;
        this.protocolFileName = protocolFileName;
        this.protocolBranch = protocolBranch;
        projectProtocolUriMap =
                new CachedMap<>(this::getProtocolDirectories, Duration.ofHours(3), Duration.ofHours(4));
        this.objectMapper = objectMapper;
        client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    private static boolean isSuccessfulResponse(HttpResponse response) {
        return response.statusCode() >= 200 && response.statusCode() < 300;
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    @Override
    public synchronized Map<String, Protocol> fetchProtocols() throws IOException {
        final Map<String, Protocol> projectProtocolMap = new HashMap<>();
        Map<String, URI> protocolUriMap;
        try {
            protocolUriMap = projectProtocolUriMap.get();
        } catch (IOException e) {
            // Failed to get the Uri Map. try using the cached value
            protocolUriMap = projectProtocolUriMap.getCache();
        }

        if (protocolUriMap == null) {
            return projectProtocolMap;
        }
        try {
            for (Map.Entry<String, URI> entry : protocolUriMap.entrySet()) {
                HttpResponse responsep =
                        client.send(getRequest(entry.getValue()), HttpResponse.BodyHandlers.ofString());
                if (isSuccessfulResponse(responsep)) {
                    for (JsonNode jsonNode : getArrayNode(responsep.body().toString())) {
                        if (jsonNode.get("name").asText().equals(this.protocolFileName)) {
                            projectProtocolMap.put(
                                    entry.getKey(), getProtocolFromUrl(jsonNode.get("download_url").asText()));
                        }
                    }
                } else {
                    log.warn("Failed to retrieve protocols from github.");
                    throw new ResponseStatusException(
                            HttpStatus.valueOf(responsep.statusCode()),
                            "Failed to retrieve protocols from github.");
                }
            }
        } catch (InterruptedException | ResponseStatusException e) {
            throw new IOException("Failed to get Protocols from Github", e);
        }

        log.info("Refreshed Protocols from Github");

        return projectProtocolMap;
    }

    @SuppressWarnings("PMD.DataflowAnomalyAnalysis")
    @Synchronized
    private Map<String, URI> getProtocolDirectories() throws IOException {

        Map<String, URI> protocolUriMap = new HashMap<>();

        try {
            HttpResponse response =
                    client.send(
                            getRequest(
                                    URI.create(GITHUB_API_URI + protocolRepo + "/contents?ref=" + protocolBranch)),
                            HttpResponse.BodyHandlers.ofString());

            if (isSuccessfulResponse(response)) {
                for (JsonNode jsonNode : getArrayNode(response.body().toString())) {
                    String type = jsonNode.get("type").asText();
                    if ("dir".equals(type)) {
                        protocolUriMap.put(
                                jsonNode.get("name").asText(),
                                URI.create(jsonNode.get("_links").get("self").asText()));
                    }
                }
            } else {
                log.warn("Failed to retrieve protocols URIs from github: {}.", response);
                throw new ResponseStatusException(
                        HttpStatus.valueOf(response.statusCode()),
                        "Failed to retrieve protocols URIs from github.");
            }

        } catch (InterruptedException | ResponseStatusException e) {
            throw new IOException("Failed to retrieve protocols URIs from github", e);
        }
        return protocolUriMap;
    }

    private Protocol getProtocolFromUrl(String url) throws IOException, InterruptedException {
        URI uri = URI.create(url);
        HttpResponse response = client.send(getRequest(uri), HttpResponse.BodyHandlers.ofString());
        if (isSuccessfulResponse(response)) {
            final ObjectMapper mapper =
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(uri.toURL(), Protocol.class);
        } else {
            log.error("Error getting Protocol from URL {} : {}", url, response);
            throw new ResponseStatusException(
                    HttpStatus.valueOf(response.statusCode()), "Protocol could not be retrieved");
        }
    }

    private HttpRequest getRequest(URI uri) {
        return HttpRequest.newBuilder(uri)
                .header("Accept", GITHUB_API_ACCEPT_HEADER)
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();
    }

    @SneakyThrows
    private ArrayNode getArrayNode(String json) {

        JsonParser parserProtocol = objectMapper.getFactory().createParser(json);
        return objectMapper.readTree(parserProtocol);
    }
}
