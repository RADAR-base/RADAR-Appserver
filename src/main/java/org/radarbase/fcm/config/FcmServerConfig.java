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

package org.radarbase.fcm.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Loads Configuration required to connect to the FCM server.
 *
 * @author yatharthranjan
 */
@Data
@ConfigurationProperties(value = "fcmserver", ignoreUnknownFields = false)
public class FcmServerConfig {

  @JsonProperty("senderid")
  private String senderId;

  @JsonProperty("serverkey")
  private String serverKey;

  @JsonProperty("fcmsender")
  private String fcmsender;

  @JsonProperty("host")
  private String host;

  @JsonProperty("port")
  private Integer port;
}
