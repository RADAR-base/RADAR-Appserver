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

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.dto.protocol.Protocol;
import org.radarbase.appserver.entity.User;
import org.radarbase.appserver.util.CachedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * @author yatharthranjan
 * @see <a href="https://github.com/RADAR-base/RADAR-aRMT-protocols">aRMT Protocols</a>
 */
@Slf4j
@Service
@Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
public class DefaultProtocolGenerator implements ProtocolGenerator {

    // Keeps a cache of Protocol for each project
    private transient CachedMap<String, Protocol> cachedProtocolMap;
    private transient CachedMap<String, Protocol> cachedProjectProtocolMap;

    private static final Duration CACHE_INVALIDATE_DEFAULT = Duration.ofHours(1);
    private static final Duration CACHE_RETRY_DEFAULT = Duration.ofHours(2);

    private final transient ProtocolFetcherStrategy protocolFetcherStrategy;

    @Autowired
    public DefaultProtocolGenerator(ProtocolFetcherStrategy protocolFetcherStrategy) {
        this.protocolFetcherStrategy = protocolFetcherStrategy;
        this.init();
    }

    @Override
    public void init() {
        cachedProtocolMap =
                new CachedMap<>(
                        protocolFetcherStrategy::fetchProtocols, CACHE_INVALIDATE_DEFAULT, CACHE_RETRY_DEFAULT);
        cachedProjectProtocolMap =
                new CachedMap<>(
                        protocolFetcherStrategy::fetchProtocolsPerProject, CACHE_INVALIDATE_DEFAULT, CACHE_RETRY_DEFAULT);
        log.debug("initialized Github Protocol generator");
    }

    @Override
    public @NonNull Map<String, Protocol> getAllProtocols() {
        try {
            return cachedProtocolMap.get();
        } catch (IOException ex) {
            log.warn("Cannot retrieve Protocols, using cached values if available.", ex);
            return cachedProtocolMap.getCache();
        }
    }

    @Override
    public Protocol getProtocol(String projectId) {

        try {
            return cachedProjectProtocolMap.get(projectId);
        } catch (IOException ex) {
            log.warn(
                    "Cannot retrieve Protocols for project {} : {}, Using cached values.", projectId, ex);
            return cachedProjectProtocolMap.get(true).get(projectId);
        }
    }

    private @NonNull Protocol forceGetProtocolForSubject(String subjectId) {
        try {
            cachedProtocolMap.get(true);
            return cachedProtocolMap.get(subjectId);
        } catch (IOException ex) {
            log.warn("Cannot retrieve Protocols, using cached values if available.", ex);
            return cachedProtocolMap.getCache().get(subjectId);
        }
    }

    @Override
    public Protocol getProtocolForSubject(String subjectId) {
        try {
            return cachedProtocolMap.get(subjectId);
        } catch (IOException ex) {
            log.warn(
                    "Cannot retrieve Protocols for subject {} : {}, Using cached values.", subjectId, ex);
            return cachedProtocolMap.getCache().get(subjectId);
        } catch(NoSuchElementException ex) {
            log.warn("Subject does not exist in map. Fetching..");
            return forceGetProtocolForSubject(subjectId);
        }
    }
}
