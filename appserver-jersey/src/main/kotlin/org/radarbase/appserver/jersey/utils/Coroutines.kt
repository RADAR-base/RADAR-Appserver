/*
 * Copyright 2025 King's College London
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.radarbase.appserver.jersey.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlin.coroutines.CoroutineContext

/**
 * Launches one coroutine per element in this iterable, applying [transform] in parallel,
 * and returns a list of all results once every coroutine completes.
 *
 * This is useful for CPU‑ or I/O‑bound operations where you want to concurrently process
 * each item without blocking the caller thread.
 *
 * @receiver The [Iterable] of input elements to transform.
 * @param context The [CoroutineContext] to launch each coroutine in. Defaults to [Dispatchers.Default].
 * @param transform A suspending function to apply to each element.
 * @return A [List] of transformed results, in the same order as the original iterable.
 */
suspend inline fun <T, R> Iterable<T>.mapParallel(
    context: CoroutineContext,
    crossinline transform: suspend (T) -> R,
): List<R> = coroutineScope {
    map { t ->
        async(context) { transform(t) }
    }.awaitAll()
}

/**
 * Like flatMap but runs each transform in parallel.
 */
suspend inline fun <T, R> Iterable<T>.flatMapParallel(
    context: CoroutineContext = Dispatchers.Default,
    crossinline transform: suspend (T) -> List<R>,
): List<R> = coroutineScope {
    map { t ->
        async(context) { transform(t) }
    }.awaitAll()
        .flatten()
}
