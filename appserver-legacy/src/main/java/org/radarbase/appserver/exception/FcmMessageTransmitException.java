package org.radarbase.appserver.exception;

public class FcmMessageTransmitException extends MessageTransmitException {

    private static final long serialVersionUID = -923871442166939L;

    public FcmMessageTransmitException(String message) {
        super(message);
    }
    public FcmMessageTransmitException(String message, Throwable e) {
        super(message, e);
    }
}
