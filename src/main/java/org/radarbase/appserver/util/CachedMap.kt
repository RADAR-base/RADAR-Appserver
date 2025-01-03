package org.radarbase.appserver.util

import java.io.IOException
import java.time.Duration
import java.time.Instant

/**
 * Map that caches the result of a list for a limited time.
 *
 * This class is thread-safe if the given supplier is thread-safe.
 */
class CachedMap<S: Any, T: Any> (
    private val supplier: ThrowingSupplier<Map<S, T>>,
    private val invalidateAfter: Duration,
    private val retryAfter: Duration
) {

    /**
     * Cache holding the result. Initialized with an empty map and a minimum timestamp.
     */
    private val cache = NonNullableAtomicReference(Result(emptyMap<S, T>(), Instant.MIN))

    /**
     * Get the cached map, or retrieve a new one if the current one is old.
     *
     * @return map of data
     * @throws IOException if the data could not be retrieved.
     */
    @Throws(IOException::class)
    fun get(): Map<S, T> = get(forceRefresh = false)

    /**
     * Get the cached map, or retrieve a new one if the current one is old.
     *
     * @param forceRefresh if true, the cache will be refreshed even if it is recent.
     * @return map of data
     * @throws IOException if the data could not be retrieved.
     */
    @Throws(IOException::class)
    fun get(forceRefresh: Boolean): Map<S, T> {
        val currentResult: Result<S, T> = cache.get()
        if (!forceRefresh && !currentResult.isStale(invalidateAfter)) {
            return currentResult.map
        }

        val newResult: Map<S, T> = supplier.get()
        cache.set(Result(newResult))
        return newResult
    }

    /**
     * Get the cached map. Does not refresh the map even if the data is old.
     *
     * @return map of data
     */
    fun getCache(): Map<S, T> = cache.get().map

    /**
     * Get a key from the map. If the key is missing, it will check whether
     * the cache may be updated. If so, it will fetch the cache again and look the key up.
     *
     * @param key key of the value to find.
     * @return element or null if it is not found
     * @throws IOException if the cache cannot be refreshed.
     */
    @Throws(IOException::class)
    operator fun get(key: S): T? {
        val currentResult: Result<S, T> = cache.get()
        val value: T? = currentResult.map[key]
        return if (value == null && currentResult.isStale(retryAfter)) {
            get(true)[key]
        } else {
            value
        }
    }

    /**
     * Supplier that may throw an IOException, Otherwise similar to [java.util.function.Supplier].
     */
    fun interface ThrowingSupplier<T: Any> {
        @Throws(IOException::class)
        fun get(): T
    }

    /**
     * Result container holding the cached map and its fetch time.
     */
    private data class Result<S, T>(
        val map: Map<S, T>,
        private val fetchTime: Instant = Instant.now()
    ) {
        fun isStale(freshDuration: Duration): Boolean =
            Duration.between(fetchTime, Instant.now()) > freshDuration
    }
}
