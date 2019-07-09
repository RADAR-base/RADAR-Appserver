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

package org.radarbase.fcm.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map;
import lombok.experimental.SuperBuilder;

/** @author yatharthranjan */
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuperBuilder
@SuppressFBWarnings("URF_UNREAD_FIELD")
public class FcmNotificationMessage extends FcmDownstreamMessage {

  // TODO Add specific Notification model and data model classes instead of using Maps.

  @JsonProperty private Map<String, Object> notification;

  @JsonProperty private Map<String, String> data;
}
