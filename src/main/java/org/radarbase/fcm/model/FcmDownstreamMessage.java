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
import javax.validation.constraints.NotEmpty;
import lombok.experimental.SuperBuilder;

/** @author yatharthranjan */
@JsonInclude(JsonInclude.Include.NON_NULL)
@SuperBuilder
public class FcmDownstreamMessage implements FcmMessage {

  @JsonProperty @NotEmpty private String to;

  @JsonProperty private String condition;

  @NotEmpty
  @JsonProperty("message_id")
  private String messageId;

  @JsonProperty("collapse_key")
  private String collapseKey;

  @JsonProperty private String priority;

  @JsonProperty("content_available")
  private Boolean contentAvailable;

  @JsonProperty("mutable_content")
  private Boolean mutableContent;

  @JsonProperty("time_to_live")
  private Integer timeToLive;

  @JsonProperty("delivery_receipt_requested")
  private Boolean deliveryReceiptRequested;

  @JsonProperty("dry_run")
  private Boolean dryRun;
}
