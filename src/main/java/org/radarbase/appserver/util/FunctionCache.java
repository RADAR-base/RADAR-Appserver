package org.radarbase.appserver.util;

import org.springframework.util.function.ThrowingFunction;

import java.lang.ref.SoftReference;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class FunctionCache<K, V> {
    private final Duration cacheTime;

    private final Duration retryTime;

    private final int maxSize;

    private final Map<K, SoftReference<Result<V>>> cachedMap;
    private final ThrowingFunction<K, V> function;

    public FunctionCache(ThrowingFunction<K, V> function,
            Duration cacheTime,
            Duration retryTime,
            int maxSize) {
        this.cacheTime = cacheTime;
        this.retryTime = retryTime;
        this.maxSize = maxSize;
        this.cachedMap = new LinkedHashMap<>(16, 0.75f, false);
        this.function = function;
    }

    public V getOrThrow(K input) throws Exception {
        SoftReference<Result<V>> localRef;
        synchronized (cachedMap) {
            localRef = cachedMap.get(input);
        }
        Result<V> result = localRef != null ? localRef.get() : null;
        if (result != null && result.isValid()) {
            return result.getOrThrow();
        }

        try {
            V content = function.applyWithException(input);
            putCache(input, new Result<>(cacheTime, content, null));
            return content;
        } catch (Exception ex) {
            synchronized (cachedMap) {
                SoftReference<Result<V>> exRef = cachedMap.get(input);
                Result<V> exResult = exRef != null ? exRef.get() : null;
                if (exResult == null || exResult.isBadResult()) {
                    putCache(input, new Result<>(retryTime, null, ex));
                    throw ex;
                } else {
                    return exResult.getOrThrow();
                }
            }
        }
    }

    private void putCache(K input, Result<V> result) {
        synchronized (cachedMap) {
            cachedMap.put(input, new SoftReference<>(result));
            int toRemove = cachedMap.size() - maxSize;
            if (toRemove > 0) {
                Iterator<?> iter = cachedMap.entrySet().iterator();
                for (int i = 0; i < toRemove; i++) {
                    iter.next();
                    iter.remove();
                    toRemove--;
                }
            }
        }
    }

    private static class Result<T> {
        private final Instant expiration;
        private final T value;

        private final Exception exception;

        Result(Duration expiryDuration, T value, Exception exception) {
            expiration = Instant.now().plus(expiryDuration);
            this.value = value;
            this.exception = exception;
        }

        T getOrThrow() throws Exception {
            if (exception != null) {
                throw exception;
            } else {
                return value;
            }
        }

        boolean isBadResult() {
            return exception != null || !isValid();
        }

        boolean isValid() {
            return Instant.now().isBefore(expiration);
        }
    }
}