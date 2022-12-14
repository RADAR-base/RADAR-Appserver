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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.dto.protocol.GithubContent;
import org.radarbase.appserver.dto.protocol.Protocol;
import org.radarbase.appserver.dto.protocol.ProtocolCacheEntry;
import org.radarbase.appserver.entity.Project;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.repository.ProjectRepository;
import org.radarbase.appserver.repository.UserRepository;
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
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class GithubProtocolFetcherStrategy implements ProtocolFetcherStrategy {
    private final transient UserRepository userRepository;
    private final transient ProjectRepository projectRepository;

    private static final String GITHUB_API_URI = "https://api.github.com/repos/";
    private static final String GITHUB_API_ACCEPT_HEADER = "application/vnd.github.v3+json";
    private final transient String protocolRepo;
    private final transient String protocolFileName;
    private final transient String protocolBranch;
    private final transient ObjectMapper objectMapper;
    // Keeps a cache of github URI's associated with protocol for each project
    private final transient CachedMap<String, URI> projectProtocolUriMap;
    private final transient HttpClient client;

    @Value("${security.github.client.token}")
    private transient String githubToken;

    @SneakyThrows
    @Autowired
    public GithubProtocolFetcherStrategy(
            @Value("${radar.questionnaire.protocol.github.repo.path}") String protocolRepo,
            @Value("${radar.questionnaire.protocol.github.file.name}") String protocolFileName,
            @Value("${radar.questionnaire.protocol.github.branch}") String protocolBranch,
            ObjectMapper objectMapper,
            UserRepository userRepository,
            ProjectRepository projectRepository) {
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
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
    }

    private static boolean isSuccessfulResponse(HttpResponse response) {
        return response.statusCode() >= 200 && response.statusCode() < 300;
    }

    @Override
    public synchronized Map<String, Protocol> fetchProtocols() throws IOException {
        Map<String, Protocol> subjectProtocolMap = new HashMap<>();
        List<User> users = this.userRepository.findAll();

        Map<String, URI> protocolUriMap;
        try {
            protocolUriMap = projectProtocolUriMap.get();
        } catch (IOException e) {
            // Failed to get the Uri Map. try using the cached value
            protocolUriMap = projectProtocolUriMap.getCache();
        }

        if (protocolUriMap == null) {
            return subjectProtocolMap;
        }

        Set<String> protocolPaths = protocolUriMap.keySet();
        subjectProtocolMap = users.parallelStream()
                .map(u -> {
                    ProtocolCacheEntry entry = this.fetchProtocolForSingleUser(u, u.getProject().getProjectId(), protocolPaths);
                    return entry;
                })
                .filter(c -> c.getProtocol() != null)
                .collect(Collectors.toMap(p -> p.getId(), p -> p.getProtocol()));

        log.info("Refreshed Protocols from Github");
        return subjectProtocolMap;
    }

    private ProtocolCacheEntry fetchProtocolForSingleUser(User u, String projectId, Set<String> protocolPaths) {
        Map<String, String> attributes = u.getAttributes();
        Map<String, String> pathMap = protocolPaths.stream().filter(k -> k.contains(projectId))
                .map(p -> {
                    Map<String, String> path = this.convertPathToAttributeMap(p, projectId);
                    return Maps.difference(attributes, path).entriesInCommon();
                }).max(Comparator.comparingInt(Map::size)).orElse(Collections.emptyMap());
        try {
            URI uri = projectProtocolUriMap.get(this.convertAttributeMapToPath(pathMap, projectId));
            if (uri == null) return new ProtocolCacheEntry(u.getSubjectId(), null);

            Protocol protocol = getProtocolFromUrl(uri);
            return new ProtocolCacheEntry(u.getSubjectId(), protocol);
        } catch (IOException | InterruptedException | ResponseStatusException e) {
            return new ProtocolCacheEntry(u.getSubjectId(), null);
        }
    }

    @Override
    public synchronized Map<String, Protocol> fetchProtocolsPerProject() throws IOException {
        Map<String, Protocol> projectProtocolMap = new HashMap<>();
        List<Project> projects = this.projectRepository.findAll();

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

        Set<String> protocolPaths = protocolUriMap.keySet();
        projectProtocolMap = projects.parallelStream()
                .map(project -> {
                    String projectId = project.getProjectId();
                    String path = protocolPaths.stream().filter(k -> k.contains(projectId)).findFirst().get();
                    try {
                        URI uri = projectProtocolUriMap.get(path);
                        Protocol protocol = getProtocolFromUrl(uri);
                        return new ProtocolCacheEntry(projectId, protocol);
                    } catch (IOException | InterruptedException | ResponseStatusException e) {
                        return new ProtocolCacheEntry(projectId, null);
                    }
                }).collect(Collectors.toMap(p -> p.getId(), p -> p.getProtocol()));

        log.info("Refreshed Protocols from Github");
        return projectProtocolMap;
    }

    public Map<String, String> convertPathToAttributeMap(String path, String projectId) {
        String[] parts = path.split("/");
        String key = "";
        Map<String, String> pathMap = new HashMap<>();

        for (String t : parts) {
            if (t.equals(projectId) || t.equals(this.protocolFileName)) continue;
            if (key.isEmpty()) key = t;
            else {
                pathMap.put(key, t);
                key = "";
            }
        }
        return pathMap;
    }

    public String convertAttributeMapToPath(Map<String, String> pathMap, String projectId) {
        StringBuilder path = new StringBuilder().append(projectId).append("/");
        for (String key : pathMap.keySet()) {
            String value = pathMap.get(key);
            path.append(key).append("/").append(value).append("/");
        }
        path.append(this.protocolFileName);
        return path.toString();
    }

    @Synchronized
    private Map<String, URI> getProtocolDirectories() throws IOException {

        Map<String, URI> protocolUriMap = new HashMap<>();

        try {
            HttpResponse response =
                    client.send(
                            getRequest(
                                    URI.create(GITHUB_API_URI + protocolRepo + "/branches/" + protocolBranch)),
                            HttpResponse.BodyHandlers.ofString());
            if (isSuccessfulResponse(response)) {
                ObjectNode result = getArrayNode(response.body().toString());
                String treeSha = result.findValue("tree").findValue("sha").asText();
                URI treeUri = URI.create(GITHUB_API_URI + protocolRepo + "/git/trees/" + treeSha + "?recursive=true");
                HttpResponse treeResponse = client.send(getRequest(treeUri), HttpResponse.BodyHandlers.ofString());

               if (isSuccessfulResponse(treeResponse)) {
                   JsonNode tree = getArrayNode(treeResponse.body().toString()).get("tree");
                   for (JsonNode jsonNode : tree) {
                      String path = jsonNode.get("path").asText();
                      if (path.contains(this.protocolFileName)) {
                          protocolUriMap.put(
                                  path,
                                  URI.create(jsonNode.get("url").asText()));
                   }
                   }
               }
            }
          else {
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

    private Protocol getProtocolFromUrl(URI uri) throws IOException, InterruptedException {
        HttpResponse response = client.send(getRequest(uri), HttpResponse.BodyHandlers.ofString());
        if (isSuccessfulResponse(response)) {
            final ObjectMapper mapper =
                    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            GithubContent content = mapper.readValue(uri.toURL(), GithubContent.class);
            return mapper.readValue(content.getContent(), Protocol.class);
        } else {
            log.error("Error getting Protocol from URL {} : {}", uri.toString(), response);
            throw new ResponseStatusException(
                    HttpStatus.valueOf(response.statusCode()), "Protocol could not be retrieved");
        }
    }

    private HttpRequest getRequest(URI uri) {
        HttpRequest.Builder request = HttpRequest.newBuilder(uri)
                .header("Accept", GITHUB_API_ACCEPT_HEADER)
                .header("Authorization", "Bearer " + this.githubToken)
                .GET()
                .timeout(Duration.ofSeconds(10));

        return request.build();
    }

    @SneakyThrows
    private ObjectNode getArrayNode(String json) {
        try (JsonParser parserProtocol = objectMapper.getFactory().createParser(json)) {
            return objectMapper.readTree(parserProtocol);
          }
    }
}
