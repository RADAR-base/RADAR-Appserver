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

import java.util.concurrent.atomic.AtomicReference

/**
 * A thread-safe wrapper for an atomic reference that ensures the value is non-nullable.
 * This class provides atomic operations for accessing and updating the value.
 *
 * @param T the type of the value, constrained to be non-nullable.
 * @param initialValue the initial value to be set in the atomic reference.
 */
class NonNullAtomicReference<T : Any>(initialValue: T) {

    private val reference = AtomicReference(initialValue)

    /**
     * Retrieves the current non-null value stored in the atomic reference.
     *
     * @return the current value of type T
     */
    fun get(): T = reference.get()

    /**
     * Sets the given value to the reference, ensuring it is not null.
     *
     * @param newValue the non-null value to set to the reference
     * @throws IllegalArgumentException if the provided value is null
     */
    fun set(newValue: T) {
        requireNotNull(newValue) { "Cannot set null value to NonNullableAtomicReference" }
        reference.set(newValue)
    }

    /**
     * Atomically sets the value to the given update value if the current value equals the expected value.
     *
     * @param expect the expected current value
     * @param update the new value to set if the current value matches the expected value
     * @return `true` if the value was successfully updated, `false` otherwise
     */
    fun compareAndSet(expect: T, update: T): Boolean {
        requireNotNull(update) { "Cannot set null value to NonNullableAtomicReference" }
        return reference.compareAndSet(expect, update)
    }
}
