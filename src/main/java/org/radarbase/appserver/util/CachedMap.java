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

/**
 * Map that caches the result of a list for a limited time.
 *
 * <p>This class is thread-safe if given retriever and key extractors are thread-safe.
 */
public class CachedMap<S, T> {

    private final ThrowingSupplier<? extends Map<S,T>> retriever;
    private final Duration invalidateAfter;
    private final Duration retryAfter;
    private Temporal lastFetch;
    private Map<S, T> cache;

    /**
     * Map that retrieves data from a supplier and converts that to a map with given key extractor.
     * Given retriever and key extractor should be thread-safe to make this class thread-safe.
     *
     * @param retriever supplier of data.
     * @param invalidateAfter invalidate the set of valid results after this duration.
     * @param retryAfter retry on a missing key after this duration.
     */
    public CachedMap(ThrowingSupplier<? extends Map<S, T>> retriever,
                     Duration invalidateAfter, Duration retryAfter) {
        this.retriever = retriever;
        this.invalidateAfter = invalidateAfter;
        this.retryAfter = retryAfter;
        this.lastFetch = Instant.MIN;
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
            synchronized (this) {
                if (cache != null
                        && !isThresholdPassed(lastFetch, invalidateAfter)) {
                    return cache;
                }
            }
        }
        Map<S, T> result = retriever.get();

        synchronized (this) {
            cache = result;
            lastFetch = Instant.now();
            return cache;
        }
    }


    /**
     * Get the cached map. Does not refresh the map even if the data is old.
     *
     * @return map of data
     */
    public Map<S, T> getCache() {
        return cache;
    }


    /**
     * Get a key from the map. If the key is missing, it will check with {@link #mayRetry()} whether
     * the cache may be updated. If so, it will fetch the cache again and look the key up.
     *
     * @param key key of the value to find.
     * @return element
     * @throws IOException if the cache cannot be refreshed.
     * @throws NoSuchElementException if the element is not found.
     */
    public T get(S key) throws IOException, NoSuchElementException {
        T value = get().get(key);
        if (value == null) {
            if (mayRetry()) {
                value = get(true).get(key);
            }
            if (value == null) {
                throw new NoSuchElementException("Cannot find element for key " + key);
            }
        }
        return value;
    }

    /**
     * Whether the cache may be refreshed.
     */
    public synchronized boolean mayRetry() {
        return isThresholdPassed(lastFetch, retryAfter);
    }

    /**
     * Supplier that may throw an exception. Otherwise similar to {@link
     * java.util.function.Supplier}.
     */
    @FunctionalInterface
    public interface ThrowingSupplier<T> {

        T get() throws IOException;
    }

    /**
     * Whether a given temporal threshold is passed, compared to given time.
     */
    public static boolean isThresholdPassed(Temporal time, Duration duration) {
        return Duration.between(time, Instant.now()).compareTo(duration) > 0;
    }
}
