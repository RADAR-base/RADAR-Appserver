package org.radarbase.appserver.controller.model;

public record SendEmailRequest(String subject, String message, String to, String cc, String bcc) {
}