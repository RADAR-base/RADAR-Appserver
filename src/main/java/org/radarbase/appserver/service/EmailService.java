package org.radarbase.appserver.service;

import org.radarbase.appserver.controller.model.SendEmailRequest;

import java.util.concurrent.CompletableFuture;

public interface EmailService {
    CompletableFuture<Boolean> send(SendEmailRequest sendEmailRequest);
}