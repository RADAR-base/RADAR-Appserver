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
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.dto.protocol.Protocol;
import org.radarbase.appserver.util.CachedMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @see <a href="https://github.com/RADAR-base/RADAR-aRMT-protocols">aRMT Protocols</a>
 * @author yatharthranjan
 */
@Slf4j
@Service
public class DefaultProtocolGenerator implements ProtocolGenerator {

  // Keeps a cache of Protocol for each project
  private transient CachedMap<String, Protocol> cachedProtocolMap;

  private static final Duration CACHE_INVALIDATE_DEFAULT = Duration.ofHours(1);
  private static final Duration CACHE_RETRY_DEFAULT = Duration.ofHours(2);

  private final transient ProtocolFetcherStrategy protocolFetcherStrategy;

  @Autowired
  public DefaultProtocolGenerator(ProtocolFetcherStrategy protocolFetcherStrategy) {
    this.protocolFetcherStrategy = protocolFetcherStrategy;
  }

  @Override
  public void init() {
    cachedProtocolMap =
        new CachedMap<>(
            protocolFetcherStrategy::fetchProtocols, CACHE_INVALIDATE_DEFAULT, CACHE_RETRY_DEFAULT);
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
      return cachedProtocolMap.get(projectId);
    } catch (IOException ex) {
      log.warn(
          "Cannot retrieve Protocols for project {} : {}, Using cached values.", projectId, ex);
      return cachedProtocolMap.getCache().get(projectId);
    }
  }
}
