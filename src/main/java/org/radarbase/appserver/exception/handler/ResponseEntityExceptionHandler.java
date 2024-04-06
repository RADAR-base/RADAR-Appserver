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

import java.time.Instant;

@ControllerAdvice
public class ResponseEntityExceptionHandler {

    @ExceptionHandler(InvalidProjectDetailsException.class)
    public final ResponseEntity<ErrorDetails> handleInvalidProjectDetailsException(Exception ex, WebRequest request) throws Exception {
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

    @ExceptionHandler(AlreadyExistsException.class)
    public final ResponseEntity<ErrorDetails> handleAlreadyExistsException(Exception ex, WebRequest request) throws Exception {
        HttpStatus status = ex.getClass().getAnnotation(ResponseStatus.class).value();
        ErrorDetails error = new ErrorDetails(Instant.now(), status.value(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(NotFoundException.class)
    public final ResponseEntity<ErrorDetails> handleNotFoundException(Exception ex, WebRequest request) throws Exception {
        HttpStatus status = ex.getClass().getAnnotation(ResponseStatus.class).value();
        ErrorDetails error = new ErrorDetails(Instant.now(), status.value(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(InvalidNotificationDetailsException.class)
    public final ResponseEntity<ErrorDetails> handleInvalidNotificationDetailsException(Exception ex, WebRequest request) {
        HttpStatus status = ex.getClass().getAnnotation(ResponseStatus.class).value();
        ErrorDetails error = new ErrorDetails(Instant.now(), status.value(), ex.getMessage(), request.getDescription(false));
        return new ResponseEntity<>(error, status);
    }

    @ExceptionHandler(InvalidUserDetailsException.class)
    public final ResponseEntity<ErrorDetails> handleInvalidUserDetailsException(Exception ex, WebRequest request) {
        HttpStatus status = ex.getClass().getAnnotation(ResponseStatus.class).value();
        String cause = ex.getCause() != null ? ex.getCause().getMessage() : null;
        ErrorDetails error;
        if (cause != null) {
            error = new ErrorDetailsWithCause(Instant.now(), status.value(), cause, ex.getMessage(), request.getDescription(false));
        } else {
            error = new ErrorDetails(Instant.now(), status.value(), ex.getMessage(), request.getDescription(false));
        }
        return new ResponseEntity<>(error, status);
    }
}
