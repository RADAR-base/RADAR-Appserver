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

