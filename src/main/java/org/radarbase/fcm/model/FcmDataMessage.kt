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
import com.google.firebase.messaging.AndroidConfig
import com.google.firebase.messaging.ApnsConfig
import com.google.firebase.messaging.Aps
import com.google.firebase.messaging.WebpushConfig
import java.time.Duration
import java.time.Instant
import javax.validation.constraints.NotEmpty

/**
 * @author yatharthranjan
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
class FcmDataMessage(
    @JsonProperty
    override val to: @NotEmpty String,

    @JsonProperty
    override val condition: String? = null,

    @JsonProperty("message_id")
    override val messageId: @NotEmpty String,

    @JsonProperty("collapse_key")
    override val collapseKey: String? = null,

    @JsonProperty
    override val priority: String? = null,

    @JsonProperty("content_available")
    override val contentAvailable: Boolean? = null,

    @JsonProperty("mutable_content")
    override val mutableContent: Boolean? = null,

    @JsonProperty("time_to_live")
    override val timeToLive: Int = 2419200, // 4 weeks

    @JsonProperty("delivery_receipt_requested")
    override val deliveryReceiptRequested: Boolean = false,

    @JsonProperty("dry_run")
    override val dryRun: Boolean = false,

    @JsonProperty
    override val data: Map<String, String?>? = null,
) : FcmDownstreamMessage {

    private val ttl = timeToLive * 1000L

    override fun getAndroidConfig(): AndroidConfig = AndroidConfig.builder().apply {
        collapseKey?.let { setCollapseKey(collapseKey) }
        setPriority(AndroidConfig.Priority.valueOf(priority ?: "HIGH"))
        setTtl(ttl)
        data?.let { putAllData(data) }
    }.build()

    override fun getApnsConfig(): ApnsConfig {
        val config = ApnsConfig.builder()
        collapseKey?.let {
            config.putHeader("apns-collapse-id", collapseKey)
        }

        // The date at which the notification is no longer valid. This value is a UNIX epoch
        // expressed in seconds (UTC).
        return config.apply {

            putHeader(
                "apns-expiration", (
                        Instant.now()
                            .plus(Duration.ofSeconds(timeToLive.toLong()))
                            .toEpochMilli() / 1000).toString()
            )
            data?.let { putAllCustomData(data) }
            setAps(Aps.builder().setContentAvailable(true).setSound("default").build())
            putHeader("apns-push-type", "background") // No alert is shown
            putHeader("apns-priority", "5") // 5 required in case of background type
        }.build()
    }

    override fun getWebPushConfig(): WebpushConfig {
        TODO("Not yet implemented")
    }
}