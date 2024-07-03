package org.radarbase.appserver.exception;

public class InvalidPathDetailsException extends IllegalArgumentException {
    private static final long serialVersionUID = -793674245766939L;

    public InvalidPathDetailsException(String message) {
        super(message);
    }

    public InvalidPathDetailsException(String message, Object object) {
        super(message + " " + object.toString());
    }
}
