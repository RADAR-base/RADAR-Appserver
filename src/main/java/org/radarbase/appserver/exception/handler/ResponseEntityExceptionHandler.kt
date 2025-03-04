package org.radarbase.appserver.exception.handler

import org.radarbase.appserver.exception.*
import org.radarbase.appserver.exception.entity.ErrorDetails
import org.radarbase.appserver.exception.entity.ErrorDetailsWithCause
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.context.request.WebRequest
import radar.spring.auth.exception.AuthorizationFailedException
import radar.spring.auth.exception.ResourceForbiddenException
import java.time.Instant

@ControllerAdvice
class ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception::class)
    fun handleUnhandledException(ex: Exception, request: WebRequest): ResponseEntity<ErrorDetails> {
        val status = HttpStatus.INTERNAL_SERVER_ERROR
        val error = if (ex.cause == null) {
            ErrorDetails(Instant.now(), status.value(), ex.message, request.getDescription(false))
        } else {
            ErrorDetailsWithCause(
                Instant.now(),
                status.value(),
                ex.cause!!.message,
                ex.message,
                request.getDescription(false)
            )
        }
        return ResponseEntity(error, status)
    }

    @ExceptionHandler(AuthorizationFailedException::class)
    @Throws(Exception::class)
    fun handleAuthorizationFailedException(ex: Exception, request: WebRequest): ResponseEntity<ErrorDetails> {
        return handleEntityWithCause(ex, request)
    }

    @ExceptionHandler(ResourceForbiddenException::class)
    @Throws(Exception::class)
    fun handleResourceForbiddenException(ex: Exception, request: WebRequest): ResponseEntity<ErrorDetails> {
        return handleEntityWithCause(ex, request)
    }

    @ExceptionHandler(InvalidProjectDetailsException::class)
    @Throws(Exception::class)
    fun handleInvalidProjectDetailsException(ex: Exception, request: WebRequest): ResponseEntity<ErrorDetails> {
        return handleEntityWithCause(ex, request)
    }

    @ExceptionHandler(AlreadyExistsException::class)
    @Throws(Exception::class)
    fun handleAlreadyExistsException(ex: Exception, request: WebRequest): ResponseEntity<ErrorDetails> {
        return handleEntityWithoutCause(ex, request)
    }

    @ExceptionHandler(NotFoundException::class)
    @Throws(Exception::class)
    fun handleNotFoundException(ex: Exception, request: WebRequest): ResponseEntity<ErrorDetails> {
        return handleEntityWithoutCause(ex, request)
    }

    @ExceptionHandler(InvalidNotificationDetailsException::class)
    fun handleInvalidNotificationDetailsException(ex: Exception, request: WebRequest): ResponseEntity<ErrorDetails> {
        return handleEntityWithoutCause(ex, request)
    }

    @ExceptionHandler(InvalidUserDetailsException::class)
    fun handleInvalidUserDetailsException(ex: Exception, request: WebRequest): ResponseEntity<ErrorDetails> {
        return handleEntityWithCause(ex, request)
    }

    @ExceptionHandler(InvalidFileDetailsException::class)
    fun handleInvalidFileDetailsException(ex: Exception, request: WebRequest): ResponseEntity<ErrorDetails> {
        return handleEntityWithCause(ex, request)
    }

    @ExceptionHandler(InvalidPathDetailsException::class)
    fun handleInvalidPathDetailsException(ex: Exception, request: WebRequest): ResponseEntity<ErrorDetails> {
        return handleEntityWithCause(ex, request)
    }

    @ExceptionHandler(FileStorageException::class)
    fun handleFileStorageException(ex: Exception, request: WebRequest): ResponseEntity<ErrorDetails> {
        return handleEntityWithCause(ex, request)
    }

    fun handleEntityWithCause(ex: Exception, request: WebRequest): ResponseEntity<ErrorDetails> {
        val cause = ex.cause?.message
        val status = ex.javaClass.getAnnotation(ResponseStatus::class.java).value
        val error = if (cause != null) {
            ErrorDetailsWithCause(
                Instant.now(),
                status.value(),
                cause,
                ex.message,
                request.getDescription(false)
            )
        } else {
            ErrorDetails(
                Instant.now(),
                status.value(),
                ex.message,
                request.getDescription(false)
            )
        }
        return ResponseEntity(error, status)
    }

    fun handleEntityWithoutCause(ex: Exception, request: WebRequest): ResponseEntity<ErrorDetails> {
        val status = ex.javaClass.getAnnotation(ResponseStatus::class.java).value
        val error = ErrorDetails(
            Instant.now(),
            status.value(),
            ex.message,
            request.getDescription(false)
        )
        return ResponseEntity(error, status)
    }
}