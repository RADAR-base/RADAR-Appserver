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

package org.radarbase.appserver.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.ref.SoftReference
import java.time.Duration
import java.time.Instant

/**
 * A generic caching function wrapper that caches computation results for a specified duration.
 *
 * Provides a mechanism to cache expensive function computations, retry computations on failure,
 * and limit the number of cache entries. If the cached result is expired or unavailable, the computation
 * is re-executed using the underlying function.
 *
 * @param I The type of the input to the function.
 * @param O The type of the output from the function.
 * @property function The function whose results should be cached.
 * @property cacheTime The duration for which cached results remain valid.
 * @property retryTime The duration to wait before retrying a computation after a failure.
 * @*/
class CachedFunction<I : Any, O : Any>(
    private val function: CustomThrowingFunction<I, O>,
    val cacheTime: Duration,
    val retryTime: Duration,
    val maxEntries: Int = 0,
) : CustomThrowingFunction<I, O> {


    private val cachedMap: MutableMap<I, LockedResult> =
        object : LinkedHashMap<I, LockedResult>(16, 0.75f, false) {
            /**
             * Determines whether the eldest entry in the map should be removed based on the current size and
             * the maximum number of entries allowed.
             *
             * @param eldest the eldest entry in the map being considered for removal
             * @return true if the size of the map exceeds the maximum allowed entries and the eldest entry
             * should be removed; false otherwise
             */
            override fun removeEldestEntry(eldest: Map.Entry<I, LockedResult>): Boolean {
                return size > maxEntries
            }
        }

    /**
     * Computes or retrieves a cached value associated with the given key.
     * If the value is not present in the cache, it computes the value and stores it in the cache.
     * This method ensures thread-safety and handles exceptions during value computation.
     *
     * @param key the input key used to fetch or compute the cached value
     * @return the cached or newly computed output value associated with the key
     * @throws Exception if computation of the value fails
     */
    @Throws(Exception::class)
    override fun applyWithException(key: I): O {
        return synchronized(cachedMap) {
            cachedMap.getOrPut(key) {
                LockedResult(key)
            }
        }.getOrCompute()
    }

    /**
     * Represents a thread-safe container for caching and computing results associated with a specific input.
     *
     * This class ensures that the result is computed only when necessary, and cached results are reused
     * when they are still valid (not expired). If an error occurs during computation, the error is cached
     * with a retry time, allowing controlled re-computation.
     *
     * @param I the type of the input parameter used for computation.
     * @param O the type of the computed result.
     * @property input The input parameter for which the result is being cached or computed.
     * @property reference A soft reference to a previously computed result. This may expire and be cleared
     *                     by the garbage collector if memory is needed, or the result may become invalid based
     *                     on its expiration.
     */
    private inner class LockedResult(
        @field:Transient
        private val input: I,
        @field:Transient
        private var reference: SoftReference<Result<O>> = SoftReference(null),
    ) {
        /**
         * Retrieves a cached or precomputed result if available and valid. Otherwise, computes a new result,
         * caches it, and returns it. If the computation fails, an exception is thrown and the failure is cached
         * for a retry duration.
         *
         * @return The computed or cached result of type `O`.
         * @throws Exception If an error occurs during the computation process.
         */
        @Throws(Exception::class)
        @Synchronized
        fun getOrCompute(): O {
            val result: Result<O>? = reference.get()

            if (result != null && !result.isExpired()) {
                when(result) {
                    is Result.Success -> return result.value
                    is Result.Failure -> throw result.exception
                }
            }

            try {
                logger.debug("Computing result for input {}", input)
                val computedResult: O = function.applyWithException(input)
                reference = SoftReference(Result.Success(computedResult, Instant.now().plus(cacheTime)))
                return computedResult
            } catch (ex: Exception) {
                logger.error("Failed to compute result for input {}", input, ex)
                reference = SoftReference(Result.Failure(Instant.now().plus(retryTime), ex))
                throw ex
            }
        }
    }

    /**
     * Represents the result of an operation that can succeed with a value or fail with an exception.
     * This sealed class allows modeling of both success and failure outcomes for an operation.
     *
     * @param T the type of the successful result's value
     */
    sealed class Result<out T : Any> {

        abstract val expiration: Instant

        /**
         * Represents a successful result in an operation.
         *
         * @param T The type of the value associated with the success result.
         * @property value The value associated with the successful result.
         * @property expiration The time in the future when this result should no longer be considered valid.
         */
        data class Success<T : Any>(val value: T, override val expiration: Instant) : Result<T>()

        /**
         * Represents a failure result in an operation.
         *
         * This class is a specialized type of [Result] that contains information
         * about an exception occurring during the execution of an operation. It allows
         * tracking both the exception details and the expiration time for the result.
         *
         * @property expiration the expiration time for the failure result
         * @property exception the exception that caused the failure
         */
        data class Failure(override val expiration: Instant, val exception: Exception) : Result<Nothing>()

        /**
         * Checks if the result has expired based on the current time.
         *
         * @return true if the current time is after the expiration time, false otherwise
         */
        fun isExpired(): Boolean = Instant.now().isAfter(expiration)
    }

    companion object {

        private val logger: Logger = LoggerFactory.getLogger(CachedFunction::class.java)

    }
}