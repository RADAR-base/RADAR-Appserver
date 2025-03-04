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

/**
 * Functional interface that represents a function which can throw checked exceptions.
 *
 * This can be used to define lambda expressions or method references that throw exceptions
 * and handle them explicitly where required.
 *
 * @param T the type of the input to the function
 * @param R the type of the result of the function
 */
fun interface CustomThrowingFunction<in T, out R> {
    /**
     * Applies the given transformation logic on the input of type T and returns a result of type R.
     *
     * @param t The input parameter of type T on which the operation is to be applied.
     * @return The result of the operation as an instance of type R.
     * @throws Exception If an exception occurs during the application of the transformation.
     */
    @Throws(Exception::class)
    fun applyWithException(t: T): R
}