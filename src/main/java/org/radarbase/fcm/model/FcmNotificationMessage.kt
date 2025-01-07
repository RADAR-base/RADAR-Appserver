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
package org.radarbase.fcm.model

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import lombok.Getter
import lombok.experimental.SuperBuilder

/** @author yatharthranjan
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class FcmNotificationMessage : FcmDownstreamMessage() {

    @JsonProperty
    var notification: Map<String, Any>? = null

    @JsonProperty
    var data: Map<String, String>? = null

    class Builder : FcmDownstreamMessage.Builder<Builder>() {
        private var notification: Map<String, Any>? = null
        private var data: Map<String, String>? = null

        fun notification(notification: Map<String, Any>?) = apply {
            this.notification = notification
        }

        fun data(data: Map<String, String>?) = apply {
            this.data = data
        }

        override fun build(): FcmNotificationMessage {
            val message = FcmNotificationMessage()
            applyTo(message)
            message.notification = this.notification
            message.data = this.data
            return message
        }
    }
}
