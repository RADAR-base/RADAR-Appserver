package org.radarbase.appserver.util;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.function.ThrowingFunction;

import java.lang.ref.SoftReference;
import java.time.Duration;
import java.time.Instant;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public class CachedFunction<K, V> implements ThrowingFunction<K, V> {
    private transient final Duration cacheTime;

    private transient final Duration retryTime;

    private transient final int maxSize;

    private transient final Map<K, LockedResult> cachedMap;

    private transient final ThrowingFunction<K, V> function;

    public CachedFunction(ThrowingFunction<K, V> function,
            Duration cacheTime,
            Duration retryTime,
            int maxSize) {
        this.cacheTime = cacheTime;
        this.retryTime = retryTime;
        this.maxSize = maxSize;
        this.cachedMap = new LinkedHashMap<>(16, 0.75f, false);
        this.function = function;
    }

    @NotNull
    public V applyWithException(@NotNull K input) throws Exception {
        LockedResult lockedResult;
        synchronized (cachedMap) {
            lockedResult = cachedMap.get(input);
            if (lockedResult == null) {
                lockedResult = new LockedResult(input);
                cachedMap.put(input, lockedResult);
                checkMaxSize();
            }
        }

        return lockedResult.getOrCompute();
    }

    private void checkMaxSize() {
        int toRemove = cachedMap.size() - maxSize;
        if (toRemove > 0) {
            Iterator<?> iter = cachedMap.entrySet().iterator();
            for (int i = 0; i < toRemove; i++) {
                iter.next();
                iter.remove();
            }
        }
    }

    private class LockedResult {
        private final transient K input;
        private transient SoftReference<Result<V>> reference;

        private LockedResult(K input) {
            this.input = input;
            reference = new SoftReference<>(null);
        }

        synchronized V getOrCompute() throws Exception {
            Result<V> result = reference.get();
            if (result != null && !result.isExpired()) {
                return result.getOrThrow();
            }
            try {
                log.debug("Recomputing {} in cache", input);
                V content = function.applyWithException(input);
                reference = new SoftReference<>(new Result<>(cacheTime, content, null));
                return content;
            } catch (Exception ex) {
                reference = new SoftReference<>(new Result<>(retryTime, null, ex));
                throw ex;
            }
        }
    }

    private static class Result<T> {
        private transient final Instant expiration;
        private transient final T value;

        private transient final Exception exception;

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
            return exception != null || isExpired();
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiration);
        }
    }
}
