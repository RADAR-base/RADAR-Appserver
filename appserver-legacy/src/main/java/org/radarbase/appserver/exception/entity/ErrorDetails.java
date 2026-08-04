package org.radarbase.appserver.exception.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ErrorDetails {
    Instant timestamp;
    int status;
    String message;
    String path;
}
