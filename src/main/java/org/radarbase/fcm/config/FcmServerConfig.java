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
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Objects;

/**
 * Loads Configuration required to connect to the FCM server.
 *
 * @author yatharthranjan
 */
@ConfigurationProperties(value = "fcmserver", ignoreUnknownFields = false)
public class FcmServerConfig {

    @JsonProperty("senderid")
    private String senderId;

    @JsonProperty("serverkey")
    private String serverKey;

    @JsonProperty("fcmsender")
    private String fcmsender;

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getServerKey() {
        return serverKey;
    }

    public void setServerKey(String serverKey) {
        this.serverKey = serverKey;
    }

    public String getFcmsender() {
        return fcmsender;
    }

    public void setFcmsender(String fcmsender) {
        this.fcmsender = fcmsender;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FcmServerConfig)) return false;
        FcmServerConfig that = (FcmServerConfig) o;
        return Objects.equals(getSenderId(), that.getSenderId()) &&
                Objects.equals(getServerKey(), that.getServerKey()) &&
                Objects.equals(getFcmsender(), that.getFcmsender());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSenderId(), getServerKey(), getFcmsender());
    }
}
