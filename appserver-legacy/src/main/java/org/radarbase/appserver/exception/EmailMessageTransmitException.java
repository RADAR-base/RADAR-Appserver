package org.radarbase.appserver.exception;

public class EmailMessageTransmitException extends MessageTransmitException {

    private static final long serialVersionUID = -1927189245766939L;

    public EmailMessageTransmitException(String message) {
        super(message);
    }
    public EmailMessageTransmitException(String message, Throwable e) {
        super(message, e);
    }
}
