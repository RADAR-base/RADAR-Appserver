package org.radarbase.appserver.exception;

public class EmailMessageTransmitException extends MessageTransmitException {
    public EmailMessageTransmitException(String message) {
        super(message);
    }
    public EmailMessageTransmitException(String message, Throwable e) {
        super(message, e);
    }
}
