/*
 *
 *  *
 *  *  * Copyright 2018 King's College London
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  *   http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *  *
 *  *
 *
 */

package org.radarbase.appserver.service.fcm;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.radarbase.appserver.service.FcmNotificationService;
import org.radarbase.appserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataJpaTest
public class FcmMessageReceiverTest {

  @Qualifier("getMessageReceiver")
  @Autowired private transient FcmMessageReceiverService messageReceiverService;

  @Test
  public void checkScheduleNotificationRequest() {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode rootNode = mapper.createObjectNode();

    rootNode.put("from", "xyz");

    ObjectNode data = mapper.createObjectNode();
    data.put("action", "SCHEDULE");
    data.put("projectId", "radar");
    data.put("notificationTitle", "title");
    data.put("notificationMessage", "body");
    data.put("time", Instant.now().plus(Duration.ofHours(1)).toEpochMilli());

    rootNode.set("data", data);

    // String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(rootNode);

    assertDoesNotThrow(() -> messageReceiverService.handleUpstreamMessage(rootNode));
  }

  @TestConfiguration
  public static class MessageReceiverConfig {

    @MockBean private transient FcmNotificationService notificationService;
    @MockBean private transient UserService userService;
    @Autowired private transient ApplicationEventPublisher notificationStateEventPublisher;
    @MockBean private transient ScheduleNotificationHandler scheduleNotificationHandler;

    @Bean
    public FcmMessageReceiverService getMessageReceiver() {
      return new FcmMessageReceiverService(
          notificationService,
          userService,
          notificationStateEventPublisher,
          scheduleNotificationHandler);
    }
  }
}
