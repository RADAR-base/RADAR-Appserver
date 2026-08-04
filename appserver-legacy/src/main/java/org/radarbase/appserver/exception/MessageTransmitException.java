package org.radarbase.appserver.exception;

public class MessageTransmitException extends Exception {
    private static final long serialVersionUID = -281834508766939L;

    public MessageTransmitException(String message) {
        super(message);
    }
    public MessageTransmitException(String message, Throwable e) {
        super(message, e);
    }
}
