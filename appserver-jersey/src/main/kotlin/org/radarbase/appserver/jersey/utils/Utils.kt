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

package org.radarbase.appserver.jersey.utils

import org.radarbase.appserver.jersey.dto.ProjectDto
import org.radarbase.appserver.jersey.exception.InvalidProjectDetailsException
import org.radarbase.jersey.exception.HttpNotFoundException
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Throws [HttpNotFoundException] with the result of calling [messageProvider] if the value is null.
 * Otherwise, returns the not null value.
 */
@OptIn(ExperimentalContracts::class)
inline fun <T : Any> checkPresence(value: T?, code: String, messageProvider: () -> String): T {
    contract {
        returns() implies (value != null)
    }

    if (value == null) {
        throw HttpNotFoundException(code,messageProvider())
    } else {
        return value
    }
}

/**
 * Validates a condition for the given project details and throws [InvalidProjectDetailsException]
 * if the condition is met (Project details are invalid).
 *
 * @param projectDTO the [ProjectDto] containing details of the project being validated
 * @param invalidation a lambda returning `true` if the project details are invalid
 * @param messageProvider a lambda providing the error message for the exception
 * @throws InvalidProjectDetailsException if the validation fails
 */
inline fun checkInvalidProjectDetails(
    projectDTO: ProjectDto,
    invalidation: () -> Boolean,
    messageProvider: () -> String,
) {
    if (invalidation()) {
        throw InvalidProjectDetailsException(
            messageProvider(),
        )
    }
}

/**
 * Validates a condition for the given details and throws a specified
 * RuntimeException if the condition is met (User details are invalid).
 *
 * @param invalidation a lambda returning `true` if the user details are invalid
 * @param messageProvider a lambda providing the error message for the exception
 * @throws E runtime exception if the validation fails
 *
 */
inline fun <reified E : Exception> checkInvalidDetails(
    invalidation: () -> Boolean,
    messageProvider: () -> String,
) {
    if (invalidation()) {
        throw E::class.java.getDeclaredConstructor(
            String::class.java,
        ).newInstance(messageProvider())
    }
}
