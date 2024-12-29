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
 * @param validation a lambda returning `true` if the project details are invalid
 * @param messageProvider a lambda providing the error message for the exception
 * @throws InvalidProjectDetailsException if the validation fails
 */
inline fun checkValidProjectDetails(
    projectDTO: ProjectDTO,
    validation: () -> Boolean,
    messageProvider: () -> String
) {
    if (validation()) {
        throw InvalidProjectDetailsException(projectDTO, IllegalArgumentException(messageProvider()))
    }
}