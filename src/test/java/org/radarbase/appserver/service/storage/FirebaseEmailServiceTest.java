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

package org.radarbase.appserver.service.storage;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.radarbase.appserver.controller.model.SendEmailRequest;
import org.radarbase.appserver.service.FirebaseEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(
    classes = {FirebaseEmailService.class},
    properties = {
        "send-email.enabled=true",
        "email.type=firebase",
    }
)
class FirebaseEmailServiceTest {

    @Autowired
    private FirebaseEmailService firebaseEmailService;

    private SendEmailRequest validEmailRequest = new SendEmailRequest(
        "subject", "message", "to", "cc", "bcc"
    );
    private CollectionReference mailCollection;
    private MockedStatic<FirestoreOptions> firestoreOptionsStatic;
    private ApiFuture firebaseApiFuture;

    @BeforeEach
    public void setUp() throws Exception {
        firestoreOptionsStatic = Mockito.mockStatic(FirestoreOptions.class);
        Firestore firestore = Mockito.mock(Firestore.class);
        FirestoreOptions firestoreOptionsInstance = Mockito.mock(FirestoreOptions.class);
        when(firestoreOptionsInstance.getService()).thenReturn(firestore);
        firestoreOptionsStatic.when(FirestoreOptions::getDefaultInstance).thenReturn(firestoreOptionsInstance);

        mailCollection = mock(CollectionReference.class);
        when(firestore.collection("mail")).thenReturn(mailCollection);

        firebaseApiFuture = mock(ApiFuture.class);
        when(mailCollection.add(anyMap())).thenReturn(firebaseApiFuture);
        when(firebaseApiFuture.get()).thenReturn(null);
    }

    @AfterEach
    public void tearDown() {
        firestoreOptionsStatic.close();
    }

    @Test
    void testArguments() {
        assertDoesNotThrow(() -> firebaseEmailService.send(validEmailRequest));
        assertDoesNotThrow(() -> firebaseEmailService.send(new SendEmailRequest("subject", "message", "to", null, null)));
        assertThrows(Exception.class, () -> firebaseEmailService.send(new SendEmailRequest(null, "message", "to", "cc", "bcc")));
        assertThrows(Exception.class, () -> firebaseEmailService.send(new SendEmailRequest("subject", null, "to", "cc", "bcc")));
        assertThrows(Exception.class, () -> firebaseEmailService.send(new SendEmailRequest("subject", "message", null, "cc", "bcc")));
    }

    @Test
    void testSendEmail() throws Exception {
        CompletableFuture<Boolean> future = firebaseEmailService.send(validEmailRequest);
        Boolean success = future.get();
        assertTrue(success);
        verify(mailCollection, times(1)).add(anyMap());
    }

    @Test
    void testFailSendEmail() throws Exception {
        when(firebaseApiFuture.get()).thenThrow(new InterruptedException());
        CompletableFuture<Boolean> future = firebaseEmailService.send(validEmailRequest);
        Boolean success = future.get();
        assertTrue(!success);
    }

}