package org.radarbase.appserver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.EXPECTATION_FAILED)
public class InvalidPathDetailsException extends IllegalArgumentException {
    private static final long serialVersionUID = -793674245766939L;

    public InvalidPathDetailsException(String message) {
        super(message);
    }

    public InvalidPathDetailsException(String message, Object object) {
        super(message + " " + object.toString());
    }
}
