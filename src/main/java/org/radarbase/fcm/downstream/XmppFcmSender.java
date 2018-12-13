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

package org.radarbase.fcm.downstream;

import org.radarbase.fcm.common.CcsClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.xmpp.XmppHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/**
 * A {@link FcmSender} for sending downstream messages to devices using FCM XMPP protocol.
 * This uses an outbound-adapter configured in classpath to send messages.
 *
 * @author yatharthranjan
 */
@Component
public class XmppFcmSender implements CcsClient, FcmSender {

    @Autowired
    private MessageChannel xmppOutbound;

    private static final String XMPP_TO_FCM_DEFAULT = "devices@gcm.googleapis.com";

    @Override
    public void send(Message<?> message) {

        Message outMessage;
        if(!message.getHeaders().containsKey(XmppHeaders.TO)) {
            outMessage = MessageBuilder.fromMessage(message)
                    .setHeaderIfAbsent(XmppHeaders.TO, XMPP_TO_FCM_DEFAULT).build();
        } else {
            outMessage = message;
        }
        xmppOutbound.send(outMessage);
    }

    @Override
    public boolean doesProvideDeliveryReceipt() {
        return true;
    }
}
