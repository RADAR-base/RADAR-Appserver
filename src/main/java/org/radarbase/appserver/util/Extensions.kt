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

import kotlin.reflect.KProperty1

/**
 * Compares the current object instance with another object to determine equality based on specified fields.
 *
 * @param other the object to compare with the current instance.
 * @param fields the properties of the current object to check for equality.
 * @return true if the specified fields of both objects are equal; false otherwise.
 */
internal inline fun <reified T : Any> T.equalTo(other: Any?, vararg fields: KProperty1<T, *>): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as T
    return fields.all { field -> field.get(this) == field.get(other) }
}

/**
 * Generates a string representation of the calling object by including its class name and the
 * values of the specified properties.
 *
 * @param T the type of the object
 * @param fields the properties of the object to include in the string representation
 * @return a formatted string representation of the object and its specified properties
 */
internal inline fun <reified T : Any> T.stringRepresentation(vararg fields: KProperty1<T, *>): String {
    return "${this::class.simpleName}(${fields.joinToString(", ") { field ->
        val propertyName = field.name
        "$propertyName=${field.get(this)}"
    }})"
}
