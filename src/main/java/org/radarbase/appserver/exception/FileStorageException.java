package org.radarbase.appserver.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class FileStorageException extends RuntimeException {
    private static final long serialVersionUID = -793674245766939L;

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Object object) {
        super(message + " " + object.toString());
    }
}
