package org.radarbase.appserver.jersey.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * Acquires a lock on the given [Mutex], allowing **reentrant locking** within the same coroutine.
 *
 * This function ensures that if a coroutine already holds the given [Mutex],
 * it can safely re-enter the lock without deadlocking. This is achieved by
 * storing a context element in the coroutine's [CoroutineContext] when the
 * lock is first acquired, and checking for that marker in any nested calls.
 *
 * ### Behavior:
 * - On first call in a coroutine, the [Mutex] is locked using [withLock], and
 *   a marker is added to the coroutine context.
 * - On nested calls with the same [Mutex], the marker is detected and the block
 *   executes immediately without re-locking.
 *
 * ### Example:
 * ```
 * val mutex = Mutex()
 *
 * suspend fun doSomething() = mutex.withReentrantLock {
 *     println("First level")
 *     mutex.withReentrantLock {
 *         println("Second level")
 *     }
 * }
 * ```
 *
 * @param block The suspending block of code to execute under the lock.
 * @return The result of the [block].
 */
suspend fun <T> Mutex.withReentrantLock(block: suspend () -> T): T {
    val key = ReentrantMutexContextKey(this)

    if (coroutineContext[key] != null) {
        return block()
    }

    return withContext(ReentrantMutexContextElement(key)) {
        withLock {
            block()
        }
    }
}

data class ReentrantMutexContextKey(val mutex: Mutex) : CoroutineContext.Key<ReentrantMutexContextElement>

class ReentrantMutexContextElement(
    override val key: ReentrantMutexContextKey,
) : CoroutineContext.Element
