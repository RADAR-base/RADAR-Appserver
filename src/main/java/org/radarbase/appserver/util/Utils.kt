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

import org.radarbase.appserver.dto.ProjectDTO
import org.radarbase.appserver.exception.InvalidProjectDetailsException
import org.radarbase.appserver.exception.NotFoundException

/**
 * Throws [NotFoundException] with the result of calling [messageProvider] if the value is null.
 * Otherwise, returns the not null value.
 */
inline fun <T : Any> checkPresence(value: T?, messageProvider: () -> String): T {
    if (value == null) {
        throw NotFoundException(messageProvider())
    } else {
        return value
    }
}

/**
 * Validates a condition for the given project details and throws [InvalidProjectDetailsException]
 * if the condition is met (Project details are invalid).
 *
 * @param projectDTO the [ProjectDTO] containing details of the project being validated
 * @param invalidation a lambda returning `true` if the project details are invalid
 * @param messageProvider a lambda providing the error message for the exception
 * @throws InvalidProjectDetailsException if the validation fails
 */
inline fun checkInvalidProjectDetails(
    projectDTO: ProjectDTO,
    invalidation: () -> Boolean,
    messageProvider: () -> String
) {
    if (invalidation()) {
        throw InvalidProjectDetailsException(projectDTO, IllegalArgumentException(messageProvider()))
    }
}