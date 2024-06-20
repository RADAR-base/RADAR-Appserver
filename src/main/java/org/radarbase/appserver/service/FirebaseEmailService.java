/*
 *
 *  *  Copyright 2024 The Hyve
 *  *
 *  *  Licensed under the Apache License, Version 2.0 (the "License");
 *  *  you may not use this file except in compliance with the License.
 *  *  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing, software
 *  *  distributed under the License is distributed on an "AS IS" BASIS,
 *  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  See the License for the specific language governing permissions and
 *  *  limitations under the License.
 *
 */

package org.radarbase.appserver.service;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import lombok.extern.slf4j.Slf4j;
import org.radarbase.appserver.controller.model.SendEmailRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Slf4j
@Service
@ConditionalOnExpression("${send-email.enabled:false} and 'firebase' == '${send-email.type:}'")
public class FirebaseEmailService implements EmailService {

    @Override
    @Async
    public CompletableFuture<Boolean> send(SendEmailRequest emailRequest) {

        Assert.notNull(emailRequest, "Send email request cannot be null");
        Assert.isTrue(!emailRequest.to().isEmpty(), "Send email request must have a recipient");
        Assert.isTrue(!emailRequest.subject().isEmpty(), "Send email request must have a subject");
        Assert.isTrue(!emailRequest.message().isEmpty(), "Send email request must have a message");
        try {
            // Sending emails using Firebase works by placing a document in the Firebase Firestore.
            // Firebase will then trigger a cloud function that sends the email.
            Firestore firestore = FirestoreOptions.getDefaultInstance().getService();
            CollectionReference mailCollection = firestore.collection("mail");
            ApiFuture<DocumentReference> apiFuture = mailCollection.add(createEmailMap(emailRequest));
            apiFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            Thread.currentThread().interrupt();
            log.error("Error sending email", e);
            return CompletableFuture.completedFuture(false);
        }
        log.debug("Email sent successfully");
        return CompletableFuture.completedFuture(true);
    }

    // The 'from' email address is configured in Firebase.
    private Map<String, Object> createEmailMap(SendEmailRequest emailRequest) {
        Map<String, String> messageMap = new HashMap<>();
        messageMap.put("subject", emailRequest.subject());
        messageMap.put("html", emailRequest.message());
        Map<String, Object> emailMap = new HashMap<>();
        emailMap.put("to", emailRequest.to());
        emailMap.put("cc", emailRequest.cc());
        emailMap.put("bcc", emailRequest.bcc());
        emailMap.put("message", messageMap);
        return emailMap;
    }

}