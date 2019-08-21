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

/**
 * Enum to denote each action for the requests made via the {@link
 * org.radarbase.fcm.upstream.XmppFcmReceiver}.
 *
 * @see FcmMessageReceiverService#handleUpstreamMessage(JsonNode)
 * @author yatharthranjan
 */
enum Action {
  SCHEDULE,
  CANCEL,
  ECHO
}
