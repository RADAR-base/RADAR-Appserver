package org.radarbase.appserver.exception.handler;

import org.radarbase.appserver.exception.*;
import org.radarbase.appserver.exception.entity.ErrorDetails;
import org.radarbase.appserver.exception.entity.ErrorDetailsWithCause;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import radar.spring.auth.exception.AuthorizationFailedException;
import radar.spring.auth.exception.ResourceForbiddenException;

import java.time.Instant;

@ControllerAdvice
public class ResponseEntityExceptionHandler {

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<ErrorDetails> handleUnhandledException(Exception ex, WebRequest request) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorDetails error;
        if (ex.getCause() == null) {
           error = new ErrorDetails(Instant.now(), status.value(), ex.getMessage(), request.getDescription(false));
        } else {
            error = new ErrorDetailsWithCause(Instant.now(), status.value(), ex.getCause().getMessage(), ex.getMessage(), request.getDescription(false));
        }
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(AuthorizationFailedException.class)
    public final ResponseEntity<ErrorDetails> handleAuthorizationFailedException(Exception ex, WebRequest request) throws Exception {
        return handleEntityWithCause(ex, request);
    }

    @ExceptionHandler(ResourceForbiddenException.class)
    public final ResponseEntity<ErrorDetails> handleResourceForbiddenException(Exception ex, WebRequest request) throws Exception {
        return handleEntityWithCause(ex, request);
    }

    @ExceptionHandler(InvalidProjectDetailsException.class)
    public final ResponseEntity<ErrorDetails> handleInvalidProjectDetailsException(Exception ex, WebRequest request) throws Exception {
        return handleEntityWithCause(ex, request);
    }

    @ExceptionHandler(AlreadyExistsException.class)
    public final ResponseEntity<ErrorDetails> handleAlreadyExistsException(Exception ex, WebRequest request) throws Exception {
        return handleEntityWithoutCause(ex, request);
    }

    @ExceptionHandler(NotFoundException.class)
    public final ResponseEntity<ErrorDetails> handleNotFoundException(Exception ex, WebRequest request) throws Exception {
        return handleEntityWithoutCause(ex, request);
    }

    @ExceptionHandler(InvalidNotificationDetailsException.class)
    public final ResponseEntity<ErrorDetails> handleInvalidNotificationDetailsException(Exception ex, WebRequest request) {
        return handleEntityWithoutCause(ex, request);
    }

    @ExceptionHandler(InvalidUserDetailsException.class)
    public final ResponseEntity<ErrorDetails> handleInvalidUserDetailsException(Exception ex, WebRequest request) {
        return handleEntityWithCause(ex, request);
    }

    public ResponseEntity<ErrorDetails> handleEntityWithCause(Exception ex, WebRequest request) {
        String cause = ex.getCause() != null ? ex.getCause().getMessage() : null;
        HttpStatus status = ex.getClass().getAnnotation(ResponseStatus.class).value();
        ErrorDetails error;
        if (cause != null) {
            error = new ErrorDetailsWithCause(Instant.now(), status.value(), cause, ex.getMessage(), request.getDescription(false));
        } else {
            error = new ErrorDetails(Instant.now(), status.value(), ex.getMessage(), request.getDescription(false));
        }
        return new ResponseEntity<>(error, status);
    }

    public ResponseEntity<ErrorDetails> handleEntityWithoutCause(Exception ex, WebRequest request) {
        HttpStatus status = ex.getClass().getAnnotation(ResponseStatus.class).value();
        ErrorDetails error = new ErrorDetails(Instant.now(), status.value(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(error, status);
    }
}
