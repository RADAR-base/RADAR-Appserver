package org.radarbase.appserver.exception.entity;

import lombok.Getter;

import java.time.Instant;

@Getter
public class ErrorDetailsWithCause extends ErrorDetails {
    String cause;
    public ErrorDetailsWithCause(Instant timestamp, int status, String cause, String message, String path) {
        super(timestamp, status, message, path);
        this.cause = cause;
    }
}
