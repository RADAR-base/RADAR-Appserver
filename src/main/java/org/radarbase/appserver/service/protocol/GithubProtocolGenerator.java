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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.Request;
import org.radarbase.appserver.dto.protocol.Protocol;
import org.radarbase.appserver.util.CachedMap;
import org.radarbase.fcm.common.ObjectMapperFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriBuilder;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @see <a href="https://github.com/RADAR-base/RADAR-aRMT-protocols">aRMT Protocols</a>
 *
 * @author yatharthranjan
 */
@Slf4j
@Service
public class GithubProtocolGenerator implements ProtocolGenerator {

    private final String protocolRepo;
    private final String protocolFileName;
    private final String protocolBranch;

    private CachedMap<String, Protocol> cachedMap;
    private HttpClient client;

    @Autowired
    private ObjectMapperFactory objectMapperFactory;

    private static final Duration CACHE_INVALIDATE_DEFAULT = Duration.ofMinutes(1);
    private static final Duration CACHE_RETRY_DEFAULT = Duration.ofHours(1);
    private static final String GITHUB_API_URI = "https://api.github.com/repos/";
    private static final String GITHUB_API_ACCEPT_HEADER = "application/vnd.github.v3+json";


    @Autowired
    public GithubProtocolGenerator(@Value("${radar.questionnaire.protocol.github.repo.path}") String protocolRepo,
                                   @Value("${radar.questionnaire.protocol.github.file.name}") String protocolFileName,
                                   String protocolBranch) {
        this.protocolRepo = protocolRepo;
        this.protocolFileName = protocolFileName;
        this.protocolBranch = protocolBranch;
    }

    @Override
    public void init() {
        if (protocolRepo == null || protocolRepo.isEmpty() ||
                protocolFileName == null || protocolFileName.isEmpty()) {
            throw new IllegalArgumentException("Protocol Repo And File name needs to be configured.");
        }
        client = HttpClient.newHttpClient();
        cachedMap = new CachedMap<>(this::refreshProtocols, CACHE_INVALIDATE_DEFAULT, CACHE_RETRY_DEFAULT);

        log.debug("initialized Github Protocol generator");
    }

    @Override
    public Map<String, Protocol> getAllProtocols() {
        try {
            return cachedMap.get();
        } catch (IOException ex) {
            log.error("Cannot retrieve Protocols: {}", ex);
            return null;
        }
    }

    @Override
    public Protocol getProtocol(String projectId) {

        try {
            return cachedMap.get(projectId);
        } catch (IOException ex) {
            log.error("Cannot retrieve Protocols for project {} : {}",
                    projectId, ex);
            return null;
        }
    }

    @SneakyThrows
    private synchronized Map<String, Protocol> refreshProtocols() {

        final Map<String, Protocol> projectProtocolMap = new HashMap<>();

        Map<String, URI> protocolUriMap = getProtocolDirectories();

        for (Map.Entry<String, URI> entry : protocolUriMap.entrySet()) {
            HttpResponse responsep = client.send(getRequest(entry.getValue()), HttpResponse.BodyHandlers.ofString());
            if(isSuccessfulResponse(responsep)) {
                for (JsonNode jsonNode : getArrayNode(responsep.body().toString())) {
                    if (jsonNode.get("name").asText().equals(this.protocolFileName)) {
                        projectProtocolMap.put(entry.getKey(), getProtocolFromUrl(jsonNode.get("download_url").asText()));
                    }
                }
            }
        }
        log.info("Refreshed Protocols from Github");

        return projectProtocolMap;
    }

    private Map<String, URI> getProtocolDirectories() throws Exception {

        final ObjectMapper mapper = objectMapperFactory.getObject();
        Map<String, URI> protocolUriMap = new HashMap<>();

        HttpResponse response = client.send(getRequest(URI.create(GITHUB_API_URI + protocolRepo + "/contents")),
                HttpResponse.BodyHandlers.ofString());

        if (isSuccessfulResponse(response)) {
            for (JsonNode jsonNode : getArrayNode(response.body().toString())) {
                String type = jsonNode.get("type").asText();
                if (type.equals("dir")) {
                    protocolUriMap.put(jsonNode.get("name").asText(), URI.create(jsonNode.get("_links").get("self").asText()));
                }
            }
        }
        return protocolUriMap;
    }

    private Protocol getProtocolFromUrl(String url) throws Exception {
        URI uri = URI.create(url);
        HttpResponse response = client.send(getRequest(uri),
                HttpResponse.BodyHandlers.ofString());
        if(isSuccessfulResponse(response)) {
            final ObjectMapper mapper = objectMapperFactory.getObject()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(uri.toURL(), Protocol.class);
        } else {
            log.error("Error getting Protocol from URL {} : {}", url, response);
            throw new IllegalStateException("Protocol could not be retrieved");
        }
    }

    private HttpRequest getRequest(URI uri) {
        return  HttpRequest.newBuilder(uri)
                .header("Accept", GITHUB_API_ACCEPT_HEADER)
                .GET()
                .timeout(Duration.ofSeconds(10))
                .build();
    }

    private ArrayNode getArrayNode(String json) throws Exception {
        final ObjectMapper mapper = objectMapperFactory.getObject();

        JsonParser parserProtocol = mapper.getFactory().createParser(json);
        return mapper.readTree(parserProtocol);
    }

    public static boolean isSuccessfulResponse(HttpResponse response) {
        return response.statusCode() >= 200 && response.statusCode() < 300;
    }
}
