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
import org.radarbase.appserver.service.GithubClient;
import org.radarbase.appserver.service.GithubService;
import org.radarbase.appserver.util.CachedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
@SuppressWarnings("PMD.DataflowAnomalyAnalysis")
public class GithubProtocolFetcherStrategy implements ProtocolFetcherStrategy {
    private final transient UserRepository userRepository;
    private final transient ProjectRepository projectRepository;

    private static final String GITHUB_API_URI = "https://api.github.com/repos/";
    private final transient String protocolRepo;
    private final transient String protocolFileName;
    private final transient String protocolBranch;
    private final transient ObjectMapper objectMapper;
    private final transient ObjectMapper localMapper;
    // Keeps a cache of github URI's associated with protocol for each project
    private final transient CachedMap<String, URI> projectProtocolUriMap;

    private final transient GithubService githubService;

    @SneakyThrows
    @Autowired
    public GithubProtocolFetcherStrategy(
            @Value("${radar.questionnaire.protocol.github.repo.path}") String protocolRepo,
            @Value("${radar.questionnaire.protocol.github.file.name}") String protocolFileName,
            @Value("${radar.questionnaire.protocol.github.branch}") String protocolBranch,
            ObjectMapper objectMapper,
            UserRepository userRepository,
            ProjectRepository projectRepository,
            GithubService githubService) {
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
                new CachedMap<>(this::getProtocolDirectories, Duration.ofHours(3), Duration.ofMinutes(4));
        this.objectMapper = objectMapper;
        this.localMapper = this.objectMapper.copy();
        this.localMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.githubService = githubService;
    }

    @Override
    public synchronized Map<String, Protocol> fetchProtocols() {
        List<User> users = this.userRepository.findAll();

        Set<String> protocolPaths = getProtocolPaths();
        if (protocolPaths == null) {
            return Map.of();
        }

        Map<String, Protocol> subjectProtocolMap = users.parallelStream()
                .map(u -> this.fetchProtocolForSingleUser(u, u.getProject().getProjectId(), protocolPaths))
                .filter(c -> c.getProtocol() != null)
                .collect(Collectors.toMap(ProtocolCacheEntry::getId, ProtocolCacheEntry::getProtocol));

        log.info("Refreshed Protocols from Github");
        return subjectProtocolMap;
    }

    private ProtocolCacheEntry fetchProtocolForSingleUser(User u, String projectId, Set<String> protocolPaths) {
        Map<String, String> attributes = u.getAttributes() != null ? u.getAttributes() : Map.of();
        Map<String, String> pathMap = protocolPaths.stream()
                .filter(k -> k.contains(projectId))
                .map(p -> {
                    Map<String, String> path = this.convertPathToAttributeMap(p, projectId);
                    return Maps.difference(attributes, path).entriesInCommon();
                })
                .max(Comparator.comparingInt(Map::size))
                .orElse(Collections.emptyMap());

        try {
            String attributePath = this.convertAttributeMapToPath(pathMap, projectId);
            if (projectProtocolUriMap.get().containsKey(attributePath)) {
                URI uri = projectProtocolUriMap.get(attributePath);
                return new ProtocolCacheEntry(u.getSubjectId(), getProtocolFromUrl(uri));
            } else {
                return new ProtocolCacheEntry(u.getSubjectId(), null);
            }
        } catch (IOException | InterruptedException | ResponseStatusException e) {
            return new ProtocolCacheEntry(u.getSubjectId(), null);
        }
    }

    @Override
    public Map<String, Protocol> fetchProtocolsPerProject() {
        Set<String> protocolPaths = getProtocolPaths();

        if (protocolPaths == null) {
            return Map.of();
        }

        Map<String, Protocol> projectProtocolMap = projectRepository.findAll()
                .parallelStream()
                .map(project -> {
                    String projectId = project.getProjectId();
                    Protocol protocol = protocolPaths.stream()
                            .filter(k -> k.contains(projectId))
                            .findFirst()
                            .map(path -> {
                                try {
                                    URI uri = projectProtocolUriMap.get(path);
                                    return getProtocolFromUrl(uri);
                                } catch (IOException | InterruptedException
                                         | ResponseStatusException e) {
                                    return null;
                                }
                            }).orElse(null);
                    return new ProtocolCacheEntry(projectId, protocol);
                })
                .collect(Collectors.toMap(ProtocolCacheEntry::getId, ProtocolCacheEntry::getProtocol));

        log.info("Refreshed Protocols from Github");
        return projectProtocolMap;
    }

    private Set<String> getProtocolPaths() {
        Map<String, URI> uriMap;
        try {
            uriMap = projectProtocolUriMap.get();
        } catch (IOException e) {
            // Failed to get the Uri Map. try using the cached value
            uriMap = projectProtocolUriMap.getCache();
        }
        return uriMap != null ? uriMap.keySet() : null;
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
            String content = githubService.getGithubContentWithoutCache(GITHUB_API_URI + protocolRepo + "/branches/" + protocolBranch);
            ObjectNode result = getArrayNode(content);
            String treeSha = result.findValue("tree").findValue("sha").asText();
            String treeContent = githubService.getGithubContent(GITHUB_API_URI + protocolRepo + "/git/trees/" + treeSha + "?recursive=true");

            JsonNode tree = getArrayNode(treeContent).get("tree");
            for (JsonNode jsonNode : tree) {
                String path = jsonNode.get("path").asText();
                if (path.contains(this.protocolFileName)) {
                    protocolUriMap.put(
                            path,
                            URI.create(jsonNode.get("url").asText()));
                }
            }
        } catch (InterruptedException | ResponseStatusException e) {
            throw new IOException("Failed to retrieve protocols URIs from github", e);
        }
        return protocolUriMap;
    }

    private Protocol getProtocolFromUrl(URI uri) throws IOException, InterruptedException {
        String contentString = githubService.getGithubContent(uri.toString());
        GithubContent content = localMapper.readValue(contentString, GithubContent.class);
        return localMapper.readValue(content.getContent(), Protocol.class);
    }

    @SneakyThrows
    private ObjectNode getArrayNode(String json) {
        try (JsonParser parserProtocol = objectMapper.getFactory().createParser(json)) {
            return objectMapper.readTree(parserProtocol);
        }
    }
}
