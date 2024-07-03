package org.radarbase.appserver.exception;

public class FileStorageException extends RuntimeException {
    private static final long serialVersionUID = -793674245766939L;

    public FileStorageException(String message) {
        super(message);
    }

    public FileStorageException(String message, Object object) {
        super(message + " " + object.toString());
    }
}
