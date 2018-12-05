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

import com.fasterxml.jackson.databind.JsonNode;
import org.radarbase.fcm.dto.FcmNotificationDto;
import org.radarbase.fcm.upstream.UpstreamMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FcmMessageReceiverService implements UpstreamMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(FcmMessageReceiverService.class);

    @Override
    public void handleUpstreamMessage(JsonNode jsonMessage) {
        logger.debug("Normal Message: {}", jsonMessage.toString());

        Optional<JsonNode> action = Optional.ofNullable(jsonMessage.get("action"));

        if(!action.isPresent()) {
            logger.warn("No Action provided");
            throw new IllegalStateException("Action must not be null! Options: 'ECHO', 'SCHEDULE', 'CANCEL'");
        }

        switch (Action.valueOf(action.get().asText())) {
            case ECHO:
                logger.info("Got an ECHO request");
                break;

            case SCHEDULE:
                logger.info("Got a SCHEDULE Request");
                break;

            case CANCEL:
                logger.info("Got a CANCEL Request");
                break;
        }

    }

    @Override
    public void handleAckReceipt(JsonNode jsonMessage) {
        logger.debug("Ack Receipt: {}", jsonMessage.toString());
    }

    @Override
    public void handleNackReceipt(JsonNode jsonMessage) {
        logger.debug("Nack Receipt: {}", jsonMessage.toString());
    }

    @Override
    public void handleStatusReceipt(JsonNode jsonMessage) {
        logger.debug("Status Receipt: {}", jsonMessage.toString());
    }

    @Override
    public void handleControlMessage(JsonNode jsonMessage) {
        logger.debug("Control Message: {}", jsonMessage.toString());
    }

    @Override
    public void handleOthers(JsonNode jsonMessage) {
        logger.debug("Message Type not recognised {}", jsonMessage.toString());
    }


    private FcmNotificationDto notificationDtoMapper(JsonNode jsonMessage) {
        return null;
    }
}
