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

package org.radarbase.fcm.upstream;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.radarbase.fcm.config.ReconnectionEnabledXmppConnectionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LoggingErrorHandlingStrategy implements ErrorHandlingStrategy {

  @Autowired private ReconnectionEnabledXmppConnectionFactoryBean connectionFactoryBean;

  @Override
  public void handleError(String error, JsonNode message) {
    // TODO Create Enum for the Error Codes
    switch (error) {
      case "INVALID_JSON":
      case "BAD_REGISTRATION":
      case "BAD_ACK":
      case "TOPICS_MESSAGE_RATE_EXCEEDED":
      case "DEVICE_MESSAGE_RATE_EXCEEDED":
        log.info("Device error: {} -> {}", message.get("error"), message.get("error_description"));
        break;
      case "SERVICE_UNAVAILABLE":
      case "INTERNAL_SERVER_ERROR":
        log.info("Server error: {} -> {}", message.get("error"), message.get("error_description"));
        break;
      case "CONNECTION_DRAINING":
        log.info("Connection draining from Nack ...");
        connectionFactoryBean.setIsConnectionDraining(true);
        break;
      case "DEVICE_UNREGISTERED":
        log.info("Received unknown FCM Error Code: {}", error);
        break;
      default:
        log.info("Received unknown FCM Error Code: {}", error);
        break;
    }
  }
}
