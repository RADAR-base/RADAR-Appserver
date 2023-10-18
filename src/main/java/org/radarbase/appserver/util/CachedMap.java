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

package org.radarbase.appserver.util;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Map that caches the result of a list for a limited time.
 *
 * <p>This class is thread-safe if given retriever is thread-safe.
 */
public class CachedMap<S, T> {

  private final transient ThrowingSupplier<? extends Map<S, T>> retriever;
  private final transient Duration invalidateAfter;
  private final transient Duration retryAfter;

  private final transient AtomicReference<Result<S, T>> cache;

  /**
   * Map that retrieves data from a supplier and converts that to a map with given key extractor.
   * Given retriever and key extractor should be thread-safe to make this class thread-safe.
   *
   * @param retriever supplier of data.
   * @param invalidateAfter invalidate the set of valid results after this duration.
   * @param retryAfter retry on a missing key after this duration.
   */
  public CachedMap(
      ThrowingSupplier<? extends Map<S, T>> retriever,
      Duration invalidateAfter,
      Duration retryAfter) {
    this.retriever = retriever;
    this.invalidateAfter = invalidateAfter;
    this.retryAfter = retryAfter;
    this.cache = new AtomicReference<>(new Result<>(Map.of(), Instant.MIN));
  }

  /**
   * Get the cached map, or retrieve a new one if the current one is old.
   *
   * @return map of data
   * @throws IOException if the data could not be retrieved.
   */
  public Map<S, T> get() throws IOException {
    return get(false);
  }

  /**
   * Get the cached map, or retrieve a new one if the current one is old.
   *
   * @param forceRefresh if true, the cache will be refreshed even if it is recent.
   * @return map of data
   * @throws IOException if the data could not be retrieved.
   */
  public Map<S, T> get(boolean forceRefresh) throws IOException {
    if (!forceRefresh) {
      Result<S, T> existingResult = cache.get();
      if (!existingResult.isStale(invalidateAfter)) {
        return existingResult.result;
      }
    }
    Map<S, T> result = retriever.get();
    cache.set(new Result<>(result));
    return result;
  }

  /**
   * Get the cached map. Does not refresh the map even if the data is old.
   *
   * @return map of data
   */
  public Map<S, T> getCache() {
    return cache.get().result;
  }

  /**
   * Get a key from the map. If the key is missing, it will check whether
   * the cache may be updated. If so, it will fetch the cache again and look the key up.
   *
   * @param key key of the value to find.
   * @return element or null if it is not found
   * @throws IOException if the cache cannot be refreshed.
   */
  public T get(S key) throws IOException {
    Result<S, T> result = cache.get();
    T value = result.result.get(key);
    if (value == null && result.isStale(retryAfter)) {
      return get(true).get(key);
    } else {
      return value;
    }
  }

  /**
   * Supplier that may throw an CustomExceptionHandler. Otherwise similar to {@link
   * java.util.function.Supplier}.
   */
  @FunctionalInterface
  public interface ThrowingSupplier<T> {

    T get() throws IOException;
  }

  private static class Result<S, T> {
    final transient Map<S, T> result;
    final transient Temporal fetchTime;

    private Result(Map<S, T> result) {
      this(result, Instant.now());
    }

    private Result(Map<S, T> result, Temporal fetchTime) {
      this.result = result;
      this.fetchTime = fetchTime;
    }

    boolean isStale(Duration freshDuration) {
      return Duration.between(this.fetchTime, Instant.now()).compareTo(freshDuration) > 0;
    }
  }
}
