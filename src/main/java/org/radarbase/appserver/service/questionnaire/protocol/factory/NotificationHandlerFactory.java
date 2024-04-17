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

package org.radarbase.appserver.service.questionnaire.protocol.factory;


import org.radarbase.appserver.service.FcmNotificationService;
import org.radarbase.appserver.service.questionnaire.protocol.ProtocolHandler;
import org.radarbase.appserver.service.questionnaire.protocol.DisabledNotificationHandler;
import org.radarbase.appserver.service.questionnaire.protocol.SimpleNotificationHandler;
import org.radarbase.appserver.dto.protocol.NotificationProtocol;
import java.io.IOException;

public class NotificationHandlerFactory {

    public static ProtocolHandler getNotificationHandler(NotificationProtocol protocol) throws IOException {
        switch (protocol.getMode()) {
            case STANDARD:
                return new SimpleNotificationHandler();
            case DISABLED:
                return new DisabledNotificationHandler();
            case COMBINED:
                throw new IOException("Combined Notification Protocol Mode is not supported yet");
            default:
                return new SimpleNotificationHandler();
        }
    }

}
