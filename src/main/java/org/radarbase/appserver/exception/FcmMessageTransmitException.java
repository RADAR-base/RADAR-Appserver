package org.radarbase.appserver.exception;

public class FcmMessageTransmitException extends MessageTransmitException {
    public FcmMessageTransmitException(String message) {
        super(message);
    }
    public FcmMessageTransmitException(String message, Throwable e) {
        super(message, e);
    }
}
